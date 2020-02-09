package com.atlassian.bitbucket.jenkins.internal.applink.oauth.adaptor;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.common.Consumer;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.ServiceProviderToken;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.Token;
import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.signature.RSA_SHA1;

import java.util.Map;

import static java.util.Objects.requireNonNull;

public class OAuthConverter {

    public static OAuthAccessor asOAuthAccessor(ServiceProviderToken token) {
        requireNonNull(token, "token");

        OAuthAccessor accessor = new OAuthAccessor(OAuthConverter.toOAuthConsumer(token.getConsumer()));
        setTokenData(accessor, token);
        return accessor;
    }

    public static OAuthConsumer toOAuthConsumer(Consumer consumer) {
        String callback = consumer.getCallback() != null ? consumer.getCallback().toString() : null;
        OAuthConsumer oauthConsumer = new OAuthConsumer(callback,
                consumer.getKey(),
                consumer.getConsumerSecret().orElse(null),
                null);
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

    private static void setTokenData(OAuthAccessor accessor, ServiceProviderToken token) {
        setCommonTokenData(accessor, token);
        if (token.isRequestToken()) {
            if (token.getAuthorization() == ServiceProviderToken.Authorization.AUTHORIZED) {
                accessor.setProperty(AccessorProperty.USER, token.getUser());
                accessor.setProperty(AccessorProperty.AUTHORIZED, true);
            } else if (token.getAuthorization() == ServiceProviderToken.Authorization.DENIED) {
                accessor.setProperty(AccessorProperty.USER, token.getUser());
                accessor.setProperty(AccessorProperty.AUTHORIZED, false);
            }
        } else {
            accessor.accessToken = token.getToken();
            accessor.setProperty(AccessorProperty.USER, token.getUser());
            accessor.setProperty(AccessorProperty.AUTHORIZED, true);
        }
        accessor.tokenSecret = token.getTokenSecret();
        accessor.setProperty(AccessorProperty.CREATION_TIME, token.getCreationTime());
    }

    private static void setCommonTokenData(OAuthAccessor accessor, Token token) {
        if (token.isRequestToken()) {
            accessor.requestToken = token.getToken();
        } else {
            accessor.accessToken = token.getToken();
        }
        accessor.tokenSecret = token.getTokenSecret();
        for (Map.Entry<String, String> property : token.getProperties().entrySet()) {
            accessor.setProperty(property.getKey(), property.getValue());
        }
    }

    private static final class ConsumerProperty {

        private static final String NAME = "name";
        private static final String DESCRIPTION = "description";
    }

    private static final class AccessorProperty {

        private static final String USER = "user";
        private static final String AUTHORIZED = "authorized";
        private static final String VERIFIER = "verifier";
        private static final String CALLBACK = "callback";
        private static final String CREATION_TIME = "creationTime";
    }
}
