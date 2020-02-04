package com.atlassian.bitbucket.jenkins.internal.applink.oauth.adaptor;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.common.Consumer;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.ServiceProviderToken;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.Tokens;
import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthServiceProvider;
import net.oauth.signature.RSA_SHA1;

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

    private static void setTokenData(OAuthAccessor accessor, ServiceProviderToken token) {
        Tokens.setCommonTokenData(accessor, token);
        if (token.isRequestToken()) {
            if (token.getAuthorization() == ServiceProviderToken.Authorization.AUTHORIZED) {
                accessor.setProperty(Tokens.AccessorProperty.USER, token.getUser());
                accessor.setProperty(Tokens.AccessorProperty.AUTHORIZED, true);
            } else if (token.getAuthorization() == ServiceProviderToken.Authorization.DENIED) {
                accessor.setProperty(Tokens.AccessorProperty.USER, token.getUser());
                accessor.setProperty(Tokens.AccessorProperty.AUTHORIZED, false);
            }
        } else {
            accessor.accessToken = token.getToken();
            accessor.setProperty(Tokens.AccessorProperty.USER, token.getUser());
            accessor.setProperty(Tokens.AccessorProperty.AUTHORIZED, true);
        }
        accessor.tokenSecret = token.getTokenSecret();
        accessor.setProperty(Tokens.AccessorProperty.CREATION_TIME, token.getCreationTime());
    }

    public static final class ConsumerProperty {

        public static final String NAME = "name";
        public static final String DESCRIPTION = "description";
    }
}
