package com.atlassian.bitbucket.jenkins.internal.applink.oauth.util;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.adaptor.OAuthConverter.ConsumerProperty;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.common.Consumer;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.common.Consumer.SignatureMethod;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.keys.RSAKeys;
import net.oauth.OAuth;
import net.oauth.OAuthConsumer;
import net.oauth.signature.RSA_SHA1;

import java.net.URI;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.List;

import static java.util.Arrays.asList;

public class TestData {

    public static final String USERNAME = "bob";
    public static final String LONG_USERNAME = repeat("bob", 20);
    public static final Principal USER = new Principal() {
        public String getName() {
            return USERNAME;
        }
    };
    public static final Principal USER_WITH_LONG_USERNAME = new Principal() {
        public String getName() {
            return LONG_USERNAME;
        }
    };

    public static final KeyPair KEYS;

    static {
        try {
            KEYS = RSAKeys.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static final String HMAC_SECRET = "secret";

    public static final class Consumers {

        public static final Consumer RSA_CONSUMER = Consumer.key("consumer-rsa")
                .name("Consumer using RSA")
                .description("description")
                .signatureMethod(SignatureMethod.RSA_SHA1)
                .publicKey(KEYS.getPublic())
                .callback(URI.create("http://consumer/callback"))
                .build();

        public static final Consumer RSA_CONSUMER_WITH_NULL_DESCRIPTION = Consumer.key("consumer-rsa-null-description")
                .name("Consumer using RSA")
                .description(null)
                .signatureMethod(SignatureMethod.RSA_SHA1)
                .publicKey(KEYS.getPublic())
                .callback(URI.create("http://consumer/callback"))
                .build();

        public static final Consumer RSA_CONSUMER_WITH_EMPTY_DESCRIPTION = Consumer.key("consumer-rsa-null-description")
                .name("Consumer using RSA")
                .description("")
                .signatureMethod(SignatureMethod.RSA_SHA1)
                .publicKey(KEYS.getPublic())
                .callback(URI.create("http://consumer/callback"))
                .build();

        public static final Consumer RSA_CONSUMER_WITH_2LO = Consumer.key("consumer-rsa-with-2lo")
                .name("Consumer using RSA")
                .description("description")
                .signatureMethod(SignatureMethod.RSA_SHA1)
                .publicKey(KEYS.getPublic())
                .callback(URI.create("http://consumer/callback"))
                .build();

        public static final Consumer RSA_CONSUMER_WITH_2LO_ONLY = Consumer.key("consumer-rsa-with-2lo-only")
                .name("Consumer using RSA")
                .description("description")
                .signatureMethod(SignatureMethod.RSA_SHA1)
                .publicKey(KEYS.getPublic())
                .callback(URI.create("http://consumer/callback"))
                .build();

        public static final Consumer RSA_CONSUMER_WITH_2LO_BUT_NO_EXECUTING_USER =
                Consumer.key("consumer-rsa-with-2lo-but-no-executing-user")
                        .name("Consumer using RSA")
                        .description("description")
                        .signatureMethod(SignatureMethod.RSA_SHA1)
                        .publicKey(KEYS.getPublic())
                        .callback(URI.create("http://consumer/callback"))
                        .build();

        public static final Consumer RSA_CONSUMER_WITH_2LO_BUT_BLANK_EXECUTING_USER =
                Consumer.key("consumer-rsa-with-2lo-but-blank-executing-user")
                        .name("Consumer using RSA")
                        .description("description")
                        .signatureMethod(SignatureMethod.RSA_SHA1)
                        .publicKey(KEYS.getPublic())
                        .callback(URI.create("http://consumer/callback"))
                        .build();

        public static final Consumer RSA_CONSUMER_WITH_2LO_IMPERSONATION =
                Consumer.key("consumer-rsa-with-2lo-impersonation")
                        .name("Consumer using RSA")
                        .description("description")
                        .signatureMethod(SignatureMethod.RSA_SHA1)
                        .publicKey(KEYS.getPublic())
                        .callback(URI.create("http://consumer/callback"))
                        .build();

        public static final Consumer RSA_CONSUMER_WITH_LONG_KEY = Consumer.key(repeat("consumer-rsa", 5))
                .name("Consumer using RSA")
                .description("description")
                .signatureMethod(SignatureMethod.RSA_SHA1)
                .publicKey(KEYS.getPublic())
                .callback(URI.create("http://consumer/callback"))
                .build();

        public static final Consumer HMAC_CONSUMER = Consumer.key("consumer-hmac")
                .name("Consumer using HMAC")
                .description("description")
                .signatureMethod(SignatureMethod.HMAC_SHA1)
                .callback(URI.create("http://consumer/callback"))
                .build();

        public static final Consumer HMAC_CONSUMER_WITH_LONG_KEY = Consumer.key(repeat("consumer-hmac", 5))
                .name("Consumer using HMAC")
                .description("description")
                .signatureMethod(SignatureMethod.HMAC_SHA1)
                .callback(URI.create("http://consumer/callback"))
                .build();
    }

    public static final class OAuthConsumers {

        public static final OAuthConsumer
                RSA_OAUTH_CONSUMER =
                new OAuthConsumer("http://consumer/callback", "consumer-rsa", null, null);
        public static final OAuthConsumer
                RSA_OAUTH_CONSUMER_WITH_PRIVATE_KEY =
                new OAuthConsumer("http://consumer/callback", "consumer-rsa", null, null);
        public static final OAuthConsumer
                HMAC_OAUTH_CONSUMER =
                new OAuthConsumer("http://consumer/callback", "consumer-hmac", null, null);
        public static final OAuthConsumer
                HMAC_OAUTH_CONSUMER_WITH_SECRET =
                new OAuthConsumer("http://consumer/callback", "consumer-hmac", HMAC_SECRET, null);

        static {
            // RSA Consumers setup
            RSA_OAUTH_CONSUMER.setProperty(ConsumerProperty.NAME, "Consumer using RSA");
            RSA_OAUTH_CONSUMER_WITH_PRIVATE_KEY.setProperty(ConsumerProperty.NAME, "Consumer using RSA");

            RSA_OAUTH_CONSUMER.setProperty(ConsumerProperty.DESCRIPTION, "description");
            RSA_OAUTH_CONSUMER_WITH_PRIVATE_KEY.setProperty(ConsumerProperty.DESCRIPTION, "description");

            RSA_OAUTH_CONSUMER.setProperty(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.RSA_SHA1);
            RSA_OAUTH_CONSUMER_WITH_PRIVATE_KEY.setProperty(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.RSA_SHA1);

            RSA_OAUTH_CONSUMER.setProperty(RSA_SHA1.PUBLIC_KEY, KEYS.getPublic());
            RSA_OAUTH_CONSUMER_WITH_PRIVATE_KEY.setProperty(RSA_SHA1.PUBLIC_KEY, KEYS.getPublic());

            RSA_OAUTH_CONSUMER_WITH_PRIVATE_KEY.setProperty(RSA_SHA1.PRIVATE_KEY, KEYS.getPrivate());

            // HMAC Consumers setup
            HMAC_OAUTH_CONSUMER.setProperty(ConsumerProperty.NAME, "Consumer using HMAC");
            HMAC_OAUTH_CONSUMER_WITH_SECRET.setProperty(ConsumerProperty.NAME, "Consumer using HMAC");

            HMAC_OAUTH_CONSUMER.setProperty(ConsumerProperty.DESCRIPTION, "description");
            HMAC_OAUTH_CONSUMER_WITH_SECRET.setProperty(ConsumerProperty.DESCRIPTION, "description");

            HMAC_OAUTH_CONSUMER.setProperty(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.HMAC_SHA1);
            HMAC_OAUTH_CONSUMER_WITH_SECRET.setProperty(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.HMAC_SHA1);
        }
    }

    public static final class OAuthParameters {

        public static final OAuth.Parameter[] array = new OAuth.Parameter[]{
                new OAuth.Parameter("param1", "value1"),
                new OAuth.Parameter("param2", "value2"),
                new OAuth.Parameter("param3", "value3")
        };

        public static final List<OAuth.Parameter> list = asList(array);
    }

    public static String repeat(String str, int times) {
        StringBuilder sb = new StringBuilder(str.length() * times);
        for (int i = 0; i < times; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
}
