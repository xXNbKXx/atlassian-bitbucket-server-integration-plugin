package com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider;

import net.oauth.OAuthAccessor;

import java.util.Map;

/**
 * A utility class for converting between Atlassian {@link Token}s and the OAuth.net library {@link OAuthAccessor}s.
 */
public final class Tokens {

    private Tokens() {
    }

    public static final class AccessorProperty {

        public static final String USER = "user";
        public static final String AUTHORIZED = "authorized";
        public static final String VERIFIER = "verifier";
        public static final String CALLBACK = "callback";
        public static final String CREATION_TIME = "creationTime";
    }

    public static void setCommonTokenData(OAuthAccessor accessor, Token token) {
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
}
