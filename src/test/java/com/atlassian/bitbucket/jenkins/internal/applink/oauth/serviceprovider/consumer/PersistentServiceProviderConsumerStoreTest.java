package com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.consumer;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.RSAKeys;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.consumer.Consumer.SignatureMethod;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.exception.StoreException;
import hudson.XmlFile;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.consumer.Consumer.SignatureMethod.HMAC_SHA1;
import static com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.consumer.Consumer.SignatureMethod.RSA_SHA1;
import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;

@RunWith(MockitoJUnitRunner.class)
public class PersistentServiceProviderConsumerStoreTest {

    private static final KeyPair KEY_PAIR1;
    private static final KeyPair KEY_PAIR2;
    private static final Consumer RSA_CONSUMER;
    private static final Consumer HMAC_CONSUMER;
    private static final Consumer HMAC_CONSUMER_NO_PUBLIC_KEY;

    static {
        try {
            KEY_PAIR1 = RSAKeys.generateKeyPair();
            KEY_PAIR2 = RSAKeys.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        RSA_CONSUMER = Consumer.key("consumer-rsa")
                .name("Consumer using RSA")
                .description("This is a Consumer using RSA signature method")
                .signatureMethod(RSA_SHA1)
                .publicKey(KEY_PAIR1.getPublic())
                .callback(URI.create("http://consumer/callback1"))
                .build();

        HMAC_CONSUMER = Consumer.key("consumer-hmac")
                .name("Consumer using HMAC")
                .description("This is a Consumer using HMAC signature method")
                .signatureMethod(HMAC_SHA1)
                .publicKey(KEY_PAIR2.getPublic())
                .callback(URI.create("http://consumer/callback2"))
                .build();

        HMAC_CONSUMER_NO_PUBLIC_KEY = Consumer.key("consumer-hmac-no-pub-key")
                .name("Consumer using HMAC")
                .description("This is a Consumer using HMAC signature method but no public key")
                .signatureMethod(HMAC_SHA1)
                .callback(URI.create("http://consumer/callback"))
                .build();
    }

    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    private File consumersXmlFile;

    private PersistentServiceProviderConsumerStore consumerStore;

    private static ConsumerMatcher matches(Consumer consumer) {
        return new ConsumerMatcher(consumer);
    }

    @Before
    public void setup() throws IOException {
        consumersXmlFile = tempFolder.newFile("oauth-tokens.xml");
        consumerStore = new TestConsumerStore(consumersXmlFile);
        consumerStore.consumerEntryMap = new HashMap<>();
        consumerStore.consumerEntryMap.put(RSA_CONSUMER.getKey(), RSA_CONSUMER);
        consumerStore.consumerEntryMap.put(HMAC_CONSUMER.getKey(), HMAC_CONSUMER);
        consumerStore.consumerEntryMap.put(HMAC_CONSUMER_NO_PUBLIC_KEY.getKey(), HMAC_CONSUMER_NO_PUBLIC_KEY);

        // Save the consumers to disk (temp XML file)
        consumerStore.save();

        // clear the consumer map so they are loaded from disk (temp XML file) next time 'load()' is called
        consumerStore.consumerEntryMap = null;
    }

    @Test
    public void testLoad() {
        consumerStore.load();

        assertThat(consumerStore.consumerEntryMap, allOf(aMapWithSize(3),
                hasEntry(is(RSA_CONSUMER.getKey()), matches(RSA_CONSUMER)),
                hasEntry(is(HMAC_CONSUMER.getKey()), matches(HMAC_CONSUMER)),
                hasEntry(is(HMAC_CONSUMER_NO_PUBLIC_KEY.getKey()), matches(HMAC_CONSUMER_NO_PUBLIC_KEY))
        ));
    }

    @Test
    public void testAdd() {
        Consumer newConsumer = Consumer.key("new-consumer-rsa")
                .name("Another Consumer using RSA")
                .description("This is another Consumer also using RSA signature method")
                .signatureMethod(RSA_SHA1)
                .publicKey(KEY_PAIR2.getPublic())
                .callback(URI.create("http://consumer/callback3"))
                .build();

        consumerStore.add(newConsumer);

        assertThat(consumerStore.consumerEntryMap, allOf(aMapWithSize(4),
                hasEntry(is(RSA_CONSUMER.getKey()), matches(RSA_CONSUMER)),
                hasEntry(is(HMAC_CONSUMER.getKey()), matches(HMAC_CONSUMER)),
                hasEntry(is(HMAC_CONSUMER_NO_PUBLIC_KEY.getKey()), matches(HMAC_CONSUMER_NO_PUBLIC_KEY)),
                hasEntry(is(newConsumer.getKey()), matches(newConsumer))
        ));
    }

    @Test(expected = StoreException.class)
    public void testAddExistingThrowsException() {
        consumerStore.add(RSA_CONSUMER);
    }

    @Test(expected = NullPointerException.class)
    public void testAddNullInputThrowsException() {
        consumerStore.add(null);
    }

    @Test
    public void testGet() {
        assertThat(consumerStore.get(RSA_CONSUMER.getKey()), optionalWithValue(matches(RSA_CONSUMER)));
        assertThat(consumerStore.get(HMAC_CONSUMER.getKey()), optionalWithValue(matches(HMAC_CONSUMER)));
        assertThat(consumerStore.get(HMAC_CONSUMER_NO_PUBLIC_KEY.getKey()),
                optionalWithValue(matches(HMAC_CONSUMER_NO_PUBLIC_KEY)));
        assertThat(consumerStore.get("non-existent-consumer"), emptyOptional());
    }

    @Test(expected = NullPointerException.class)
    public void testGetNullInputThrowsException() {
        consumerStore.get(null);
    }

    @Test
    public void testDelete() {
        consumerStore.delete(HMAC_CONSUMER.getKey());

        assertThat(consumerStore.get(HMAC_CONSUMER.getKey()), emptyOptional());
        assertThat(consumerStore.consumerEntryMap, allOf(aMapWithSize(2),
                hasEntry(is(RSA_CONSUMER.getKey()), matches(RSA_CONSUMER)),
                hasEntry(is(HMAC_CONSUMER_NO_PUBLIC_KEY.getKey()), matches(HMAC_CONSUMER_NO_PUBLIC_KEY))
        ));
    }

    @Test
    public void testDeleteNonExistentConsumer() {
        consumerStore.delete("non-existent-consumer");

        assertThat(consumerStore.get("non-existent-consumer"), emptyOptional());
        assertThat(consumerStore.consumerEntryMap, allOf(aMapWithSize(3),
                hasEntry(is(RSA_CONSUMER.getKey()), matches(RSA_CONSUMER)),
                hasEntry(is(HMAC_CONSUMER.getKey()), matches(HMAC_CONSUMER)),
                hasEntry(is(HMAC_CONSUMER_NO_PUBLIC_KEY.getKey()), matches(HMAC_CONSUMER_NO_PUBLIC_KEY))
        ));
    }

    @Test(expected = NullPointerException.class)
    public void testDeleteNullInputThrowsException() {
        consumerStore.delete(null);
    }

    private static class ConsumerMatcher extends TypeSafeDiagnosingMatcher<Consumer> {

        private final Consumer expected;

        private Matcher<String> key;
        private Matcher<String> name;
        private Matcher<String> desc;
        private Matcher<URI> callback;
        private Matcher<Optional<String>> secret;
        private Matcher<SignatureMethod> signatureMethod;
        private Matcher<Optional<PublicKey>> publicKey;

        private ConsumerMatcher(Consumer expected) {
            this.expected = expected;
            if (expected != null) {
                key = is(expected.getKey());
                name = is(expected.getName());
                desc = is(expected.getDescription());
                callback = is(expected.getCallback());
                secret = is(expected.getConsumerSecret());
                signatureMethod = is(expected.getSignatureMethod());
                publicKey = is(expected.getPublicKey());
            }
        }

        @Override
        protected boolean matchesSafely(Consumer actual, Description description) {
            if (actual == expected) {
                return true;
            }
            if (expected == null) {
                description.appendText("is not null");
                return false;
            }
            if (actual == null) {
                description.appendText("is null");
                return false;
            }
            List<String> mismatchFields = new ArrayList<>();
            List<Matcher<?>> mismatchMatchers = new ArrayList<>();
            List<Object> mismatchValues = new ArrayList<>();
            if (!key.matches(actual.getKey())) {
                mismatchFields.add("key");
                mismatchMatchers.add(key);
                mismatchValues.add(actual.getKey());
            }
            if (!name.matches(actual.getName())) {
                mismatchFields.add("name");
                mismatchMatchers.add(name);
                mismatchValues.add(actual.getName());
            }
            if (!desc.matches(actual.getDescription())) {
                mismatchFields.add("description");
                mismatchMatchers.add(desc);
                mismatchValues.add(actual.getDescription());
            }
            if (!callback.matches(actual.getCallback())) {
                mismatchFields.add("callback");
                mismatchMatchers.add(callback);
                mismatchValues.add(actual.getCallback());
            }
            if (!secret.matches(actual.getConsumerSecret())) {
                mismatchFields.add("consumerSecret");
                mismatchMatchers.add(secret);
                mismatchValues.add(actual.getConsumerSecret());
            }
            if (!signatureMethod.matches(actual.getSignatureMethod())) {
                mismatchFields.add("signatureMethod");
                mismatchMatchers.add(signatureMethod);
                mismatchValues.add(actual.getSignatureMethod());
            }
            if (!publicKey.matches(actual.getPublicKey())) {
                mismatchFields.add("publicKey");
                mismatchMatchers.add(publicKey);
                mismatchValues.add(actual.getPublicKey());
            }
            if (!mismatchFields.isEmpty()) {
                description.appendText("Consumer attributes [")
                        .appendText(String.join(", ", mismatchFields))
                        .appendText("] with expectations ")
                        .appendList("[", ", ", "]", mismatchMatchers)
                        .appendText(" instead had values ")
                        .appendValueList("[", ", ", "]", mismatchValues);
                return false;
            }
            return true;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("is ").appendValue(expected);
        }
    }

    /*
     * The reason for this class (as opposed to a mock/spy) is that Jenkins' (XStream2) XML (un)marshalling machinery
     * doesn't like mock/spy instances and throws an error when it tries to marshal the mock/spy instance inside the
     * 'save()' method by serializing to XML file: 'getConfigFile().write(this)'
     */
    private static class TestConsumerStore extends PersistentServiceProviderConsumerStore {

        private final transient File tokensXmlFile;

        private TestConsumerStore(File tokensXmlFile) {
            this.tokensXmlFile = tokensXmlFile;
        }

        @Override
        protected XmlFile getConfigFile() {
            return new XmlFile(CONSUMERS, tokensXmlFile);
        }
    }
}
