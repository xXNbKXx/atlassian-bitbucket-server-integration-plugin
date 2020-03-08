package com.atlassian.bitbucket.jenkins.internal.applink.oauth.config;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.token.ServiceProviderToken;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.token.ServiceProviderTokenStore;
import hudson.Extension;
import hudson.model.UserProperty;
import jenkins.model.Jenkins;
import org.joda.time.DateTime;

import javax.inject.Inject;
import java.net.URI;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * UI property for viewing and revoking individual user access tokens. Not that despite the property will appear in
 * the user config xmls, this class is *not* data bound, and the access tokens are accessed from elsewhere.
 */
@Extension
public class AccessTokenConfiguration extends UserProperty {

    @Inject
    Clock clock;
    @Inject
    ServiceProviderTokenStore tokenStore;

    @SuppressWarnings("unused") // Stapler
    public List<DisplayAccessToken> getTokens() {
        List<DisplayAccessToken> tokenList = new ArrayList<>();
        tokenStore.getAccessTokensForUser(Jenkins.getAuthentication().getName())
                .forEach(token -> tokenList.add(new DisplayAccessToken(token, clock)));

        return tokenList;
    }

    /**
     * A display class to convert a ServiceProviderToken into a viewable entity in Jelly. Modelled off
     * {@link jenkins.security.ApiTokenProperty}.
     */
    class DisplayAccessToken {

        Clock clock;
        ServiceProviderToken token;

        public DisplayAccessToken(ServiceProviderToken token, Clock clock) {
            this.token = token;
            this.clock = clock;
        }

        @SuppressWarnings("unused") // Stapler
        public String getCallbackUrl() {
            return Optional.ofNullable(token.getCallback()).map(URI::getPath).orElse("<none>");
        }

        @SuppressWarnings("unused") // Stapler
        public String getCreationDate() {
            return new DateTime(token.getCreationTime()).toString();
        }

        @SuppressWarnings("unused") // Stapler
        public String getToken() {
            return token.getToken();
        }

        @SuppressWarnings("unused") // Stapler
        public String getTokenStatus() {
            if (token.hasExpired(clock)) {
                return "EXPIRED";
            } else if (token.hasBeenDenied()) {
                return "DENIED";
            } else if (token.hasBeenAuthorized()) {
                return "AUTHORIZED";
            } else {
                return "NOT YET AUTHORIZED";
            }
        }
    }
}
