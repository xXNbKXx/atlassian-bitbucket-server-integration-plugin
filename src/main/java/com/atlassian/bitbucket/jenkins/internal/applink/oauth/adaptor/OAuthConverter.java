package com.atlassian.bitbucket.jenkins.internal.applink.oauth.adaptor;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.common.Consumer;
import net.oauth.OAuth;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthServiceProvider;
import net.oauth.signature.RSA_SHA1;

public class OAuthConverter {

    public OAuthConsumer toOAuthConsumer(Consumer consumer) {
        String callback = consumer.getCallback() != null ? consumer.getCallback().toString() : null;
        OAuthServiceProvider serviceProvider = null;
        OAuthConsumer oauthConsumer = new OAuthConsumer(callback,
                consumer.getKey(),
                consumer.getConsumerSecret().orElse(null),
                serviceProvider);
        oauthConsumer.setProperty(ConsumerProperty.NAME, consumer.getName());
        oauthConsumer.setProperty(ConsumerProperty.DESCRIPTION, consumer.getDescription());
        if (consumer.getSignatureMethod() == Consumer.SignatureMethod.RSA_SHA1) {
            oauthConsumer.setProperty(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.RSA_SHA1);
            oauthConsumer.setProperty(RSA_SHA1.PUBLIC_KEY, consumer.getPublicKey());
        } else {
            oauthConsumer.setProperty(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.HMAC_SHA1);
        }
        return oauthConsumer;
    }

    public static final class ConsumerProperty {

        public static final String NAME = "name";
        public static final String DESCRIPTION = "description";
    }
}
