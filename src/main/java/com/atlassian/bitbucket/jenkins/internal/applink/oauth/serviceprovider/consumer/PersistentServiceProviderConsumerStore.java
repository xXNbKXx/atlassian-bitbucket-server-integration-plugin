package com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.consumer;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.consumer.Consumer.SignatureMethod;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.exception.StoreException;
import com.google.common.annotations.VisibleForTesting;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.extended.NamedMapConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import hudson.BulkChange;
import hudson.CopyOnWrite;
import hudson.XmlFile;
import hudson.model.Saveable;
import hudson.model.listeners.SaveableListener;
import hudson.util.CopyOnWriteMap;
import hudson.util.XStream2;
import jenkins.model.Jenkins;
import jenkins.util.io.OnMaster;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.logging.Logger;

import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * todo
 */
public class PersistentServiceProviderConsumerStore implements ServiceProviderConsumerStore, Saveable, OnMaster {

    @VisibleForTesting
    static final transient XStream2 CONSUMERS;

    private static final Logger log = Logger.getLogger(PersistentServiceProviderConsumerStore.class.getName());

    static {
        CONSUMERS = new XStream2();
        CONSUMERS.registerConverter(new NamedMapConverter(CONSUMERS.getMapper(), "oauth-consumer",
                "consumer-key", String.class, "consumer-details", Consumer.class), 100);
    }

    @CopyOnWrite
    @VisibleForTesting
    volatile Map<String, Consumer> consumerEntryMap;

    public PersistentServiceProviderConsumerStore() {
        CONSUMERS.registerConverter(new ConsumerConverter());
    }

    @Override
    public void add(Consumer consumer) {
        requireNonNull(consumer, "consumer");
        load();
        if (consumerEntryMap.containsKey(consumer.getKey())) {
            log.warning(() -> String.format("Consumer with key '%s' already exists.", consumer.getKey()));
            throw new StoreException("Consumer already exists");
        }
        consumerEntryMap.put(consumer.getKey(), consumer);
        save();
    }

    @Override
    public Optional<Consumer> get(String key) {
        requireNonNull(key, "key");
        load();
        return ofNullable(consumerEntryMap.get(key));
    }

    @Override
    public void delete(String key) {
        requireNonNull(key, "key");
        load();
        if (consumerEntryMap.remove(key) != null) {
            save();
        }
    }

    public synchronized void load() {
        if (consumerEntryMap != null) {
            return;
        }

        XmlFile configFile = getConfigFile();
        if (configFile.exists()) {
            try {
                configFile.unmarshal(this);
            } catch (IOException e) {
                log.log(SEVERE, "Failed to load OAuth consumers from disk", e);
                throw new StoreException("Failed to load OAuth consumers");
            }
        }
        // consumerEntryMap will be unmarshalled as a HashMap if the config file exists, otherwise will be null. Either
        // way, we convert it to the Jenkins-provided concurrent copy-on-write Map that will be copied on each save and
        // written to disk, until the next Jenkins restart
        consumerEntryMap = new CopyOnWriteMap.Hash<>(consumerEntryMap != null ? consumerEntryMap : emptyMap());
    }

    @Override
    public synchronized void save() {
        if (BulkChange.contains(this)) {
            return;
        }

        try {
            getConfigFile().write(this);
        } catch (IOException e) {
            log.log(SEVERE, "Failed to persist OAuth consumers to disk", e);
            throw new StoreException("Failed to persist OAuth consumers");
        }
        SaveableListener.fireOnChange(this, getConfigFile());
    }

    /**
     * The file where {@link Consumer consumers} are saved
     */
    @VisibleForTesting
    protected XmlFile getConfigFile() {
        // TODO: use a more specific directory than root - https://bulldog.internal.atlassian.com/browse/BBSDEV-21416
        return new XmlFile(CONSUMERS, new File(Jenkins.get().getRootDir(), "oauth-consumers.xml"));
    }

    private static final class ConsumerConverter implements Converter {

        private static final String CONSUMER_KEY = "key";
        private static final String CONSUMER_NAME = "name";
        private static final String CONSUMER_DESCRIPTION = "description";
        private static final String CALLBACK_URL = "callback-url";
        private static final String CONSUMER_SECRET = "secret";
        private static final String PUBLIC_KEY = "public-key";
        private static final String PUBLIC_KEY_ENCODED = "key";
        private static final String PUBLIC_KEY_ALGORITHM = "algorithm";
        private static final String PUBLIC_KEY_FORMAT = "format";
        private static final String SIGNATURE_METHOD = "signature-method";

        private static HierarchicalStreamWriter addNode(HierarchicalStreamWriter writer, String name, Object value) {
            writer.startNode(name);
            writer.setValue(Objects.toString(value));
            writer.endNode();
            return writer;
        }

        private static PublicKey unmarshalPublicKey(HierarchicalStreamReader reader) {
            String publicKeyEncoded = null;
            String publicKeyAlgorithm = null;
            String publicKeyFormat = null;
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                String nodeName = reader.getNodeName();
                String nodeValue = reader.getValue();
                switch (nodeName) {
                    case PUBLIC_KEY_ENCODED:
                        publicKeyEncoded = nodeValue;
                        break;
                    case PUBLIC_KEY_ALGORITHM:
                        publicKeyAlgorithm = nodeValue;
                        break;
                    case PUBLIC_KEY_FORMAT:
                        publicKeyFormat = nodeValue;
                }
                reader.moveUp();
            }

            // Although public key is optional, if the consumer entry has a 'public-key' node, then it's expected to
            // contain these nodes with valid values: public key value (Base64 encoded), 'algorithm', and 'format'
            if (isBlank(publicKeyEncoded) || isBlank(publicKeyAlgorithm) || isBlank(publicKeyFormat)) {
                Set<String> missingItems = new HashSet<>(3);
                if (isBlank(publicKeyEncoded)) {
                    missingItems.add(PUBLIC_KEY_ENCODED);
                }
                if (isBlank(publicKeyAlgorithm)) {
                    missingItems.add(PUBLIC_KEY_ALGORITHM);
                }
                if (isBlank(publicKeyFormat)) {
                    missingItems.add(PUBLIC_KEY_FORMAT);
                }
                log.warning(() -> String.format(
                        "Failed to unmarshal OAuth consumer: '%s' node is missing valid values for the following " +
                                "child nodes: [%s]", PUBLIC_KEY, String.join(",", missingItems)));
                throw new StoreException("Failed to unmarshal OAuth consumer");
            }
            KeyFactory keyFactory;
            try {
                keyFactory = KeyFactory.getInstance(publicKeyAlgorithm);
            } catch (NoSuchAlgorithmException e) {
                log.log(WARNING, "Failed to unmarshal OAuth consumer", e);
                throw new StoreException("Failed to unmarshal OAuth consumer");
            }
            byte[] publicKeyDecoded = Base64.getDecoder().decode(publicKeyEncoded);
            KeySpec keySpec;
            if ("X.509".equals(publicKeyFormat)) {
                keySpec = new X509EncodedKeySpec(publicKeyDecoded);
            } else if ("PKCS#8".equals(publicKeyFormat)) {
                keySpec = new PKCS8EncodedKeySpec(publicKeyDecoded);
            } else {
                log.warning("Unknown public key format: " + publicKeyFormat);
                throw new StoreException("Failed to unmarshal OAuth consumer");
            }
            PublicKey publicKey;
            try {
                publicKey = keyFactory.generatePublic(keySpec);
            } catch (InvalidKeySpecException e) {
                log.log(WARNING, "Failed to unmarshal OAuth consumer", e);
                throw new StoreException("Failed to unmarshal OAuth consumer");
            }
            return publicKey;
        }

        private static void addPublicKeyNode(HierarchicalStreamWriter writer, PublicKey pubKey) {
            writer.startNode(PUBLIC_KEY);
            addNode(writer, PUBLIC_KEY_ENCODED, Base64.getEncoder().encodeToString(pubKey.getEncoded()));
            addNode(writer, PUBLIC_KEY_ALGORITHM, pubKey.getAlgorithm());
            addNode(writer, PUBLIC_KEY_FORMAT, pubKey.getFormat());
            writer.endNode();
        }

        @Override
        public boolean canConvert(Class type) {
            return Consumer.class == type;
        }

        @Override
        public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
            if (!(source instanceof Consumer)) {
                log.warning(() ->
                        String.format("Source must be of type '%s', but it's ", Consumer.class.getName()) +
                                (source != null ? String.format("of type '%s' instead", source.getClass().getName()) :
                                        "null"));
                throw new StoreException("Failed to unmarshal OAuth consumer");
            }
            Consumer consumer = (Consumer) source;
            addNode(writer, CONSUMER_KEY, consumer.getKey());
            addNode(writer, CONSUMER_NAME, consumer.getName());
            addNode(writer, CONSUMER_DESCRIPTION, consumer.getDescription());
            addNode(writer, CALLBACK_URL, consumer.getCallback());
            addNode(writer, SIGNATURE_METHOD, consumer.getSignatureMethod());
            consumer.getConsumerSecret().ifPresent(secret -> addNode(writer, CONSUMER_SECRET, secret));
            consumer.getPublicKey().ifPresent(pubKey -> addPublicKeyNode(writer, pubKey));
        }

        @Override
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            String consumerKey = null;
            String consumerName = null;
            String consumerDescription = null;
            String callbackUrl = null;
            String consumerSecret = null;
            String signatureMethod = null;
            PublicKey publicKey = null;
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                String nodeName = reader.getNodeName();
                String nodeValue = reader.getValue();
                switch (nodeName) {
                    case CONSUMER_KEY:
                        consumerKey = nodeValue;
                        break;
                    case CONSUMER_NAME:
                        consumerName = nodeValue;
                        break;
                    case CONSUMER_DESCRIPTION:
                        consumerDescription = nodeValue;
                        break;
                    case CALLBACK_URL:
                        callbackUrl = nodeValue;
                        break;
                    case CONSUMER_SECRET:
                        consumerSecret = nodeValue;
                        break;
                    case SIGNATURE_METHOD:
                        signatureMethod = nodeValue;
                        break;
                    case PUBLIC_KEY:
                        publicKey = unmarshalPublicKey(reader);
                }
                reader.moveUp();
            }

            Consumer.Builder builder = new Consumer.Builder(consumerKey);
            if (isNotBlank(callbackUrl)) {
                builder.callback(URI.create(callbackUrl));
            }
            if (isNotBlank(consumerSecret)) {
                builder.consumerSecret(consumerSecret);
            }
            if (isNotBlank(signatureMethod)) {
                builder.signatureMethod(SignatureMethod.valueOf(signatureMethod));
            }
            if (isNotBlank(consumerName)) {
                builder.name(consumerName);
            }
            if (isNotBlank(consumerDescription)) {
                builder.description(consumerDescription);
            }
            if (publicKey != null) {
                builder.publicKey(publicKey);
            }
            return builder.build();
        }
    }
}
