package com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.rest;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.Randomizer;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.exception.InvalidTokenException;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.token.ServiceProviderToken;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.token.ServiceProviderTokenStore;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Action;
import hudson.model.Descriptor;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.server.OAuthServlet;
import org.jenkinsci.Symbol;
import org.json.JSONException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Clock;
import java.util.logging.Logger;

import static net.oauth.OAuth.OAUTH_TOKEN;
import static net.oauth.OAuth.Problems.*;

@Singleton
public class AuthorizeAction extends AbstractDescribableImpl<AuthorizeAction> implements Action {

    private static final int VERIFIER_LENGTH = 6;
    private static final Logger LOGGER = Logger.getLogger(AuthorizeServlet.class.getName());

    private Clock clock;
    private ServiceProviderTokenStore tokenStore;
    private Randomizer randomizer;

    @Inject
    public AuthorizeAction(ServiceProviderTokenStore store, Randomizer randomizer, Clock clock) {
        this.tokenStore = store;
        this.randomizer = randomizer;
        this.clock = clock;
    }

    @SuppressWarnings("unused") // Stapler
    public void doPerformAuthorize(HttpServletRequest request) throws IOException, ServletException, JSONException {
        //TODO: Authorize & redirect logic
    }

    private ServiceProviderToken getTokenForAuthorization(
            HttpServletRequest request) throws OAuthProblemException, IOException {
        OAuthMessage requestMessage = OAuthServlet.getMessage(request, null);
        requestMessage.requireParameters(OAUTH_TOKEN);
        ServiceProviderToken token;
        try {
            token = tokenStore.get(requestMessage.getToken())
                    .orElseThrow(() -> new OAuthProblemException(TOKEN_REJECTED));
        } catch (InvalidTokenException e) {
            throw new OAuthProblemException(TOKEN_REJECTED);
        }
        if (token.isAccessToken()) {
            throw new OAuthProblemException(TOKEN_REJECTED);
        }
        if (token.getAuthorization() == ServiceProviderToken.Authorization.AUTHORIZED ||
            token.getAuthorization() == ServiceProviderToken.Authorization.DENIED) {
            throw new OAuthProblemException(TOKEN_USED);
        }
        if (token.hasExpired(clock)) {
            throw new OAuthProblemException(TOKEN_EXPIRED);
        }
        return token;
    }

    @CheckForNull
    @Override
    public String getIconFileName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return "Authorize Action";
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return null;
    }

    @Extension
    @Symbol("authorize-action")
    public static class DescriptorImpl extends Descriptor<AuthorizeAction> {

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Authorize Action";
        }
    }
}
