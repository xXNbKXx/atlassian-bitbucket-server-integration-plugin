package com.atlassian.bitbucket.jenkins.internal.applink.oauth.rest;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.Randomizer;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.InvalidTokenException;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.ServiceProviderToken;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.ServiceProviderTokenStore;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.util.OAuthProblemUtils;
import jenkins.model.Jenkins;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.server.OAuthServlet;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Principal;
import java.time.Clock;
import java.util.logging.Logger;

import static net.oauth.OAuth.OAUTH_TOKEN;
import static net.oauth.OAuth.Problems.*;
import static net.oauth.server.OAuthServlet.handleException;

public class AuthorizeServlet {

    static final int VERIFIER_LENGTH = 6;

    private static final Logger LOGGER = Logger.getLogger(AuthorizeServlet.class.getName());

    private final Clock clock;
    private final ServiceProviderTokenStore tokenStore;
    private final Randomizer randomizer;

    public AuthorizeServlet(ServiceProviderTokenStore store, Randomizer randomizer, Clock clock) {
        this.tokenStore = store;
        this.randomizer = randomizer;
        this.clock = clock;
    }

    public void authorize(HttpServletRequest request,
                          HttpServletResponse response) throws IOException, ServletException, JSONException {
        if (!request.getMethod().equalsIgnoreCase("GET")) {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        ServiceProviderToken token;
        try {
            token = getTokenForAuthorization(request);
        } catch (OAuthException e) {
            if (e instanceof OAuthProblemException) {
                OAuthProblemUtils.logOAuthProblem(OAuthServlet.getMessage(request, null), (OAuthProblemException) e, LOGGER);
            }
            handleException(response, e, request.getRequestURL().toString(), false);
            return;
        }

        String verifier = randomizer.randomAlphanumericString(AuthorizeServlet.VERIFIER_LENGTH);
        Principal userPrincipal = Jenkins.getAuthentication();
        if (userPrincipal == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            ServiceProviderToken newToken = token.authorize(userPrincipal, verifier);
            tokenStore.put(newToken);
            response.setContentType("application/json");
            OutputStream out = response.getOutputStream();
            JSONObject json = new JSONObject();
            json.put("authorizeCode", newToken.getVerifier());
            out.write(json.toString().getBytes());
        }
    }

    private ServiceProviderToken getTokenForAuthorization(
            HttpServletRequest request) throws OAuthProblemException, IOException {
        OAuthMessage requestMessage = OAuthServlet.getMessage(request, null);
        requestMessage.requireParameters(OAUTH_TOKEN);
        ServiceProviderToken token;
        try {
            token = tokenStore.get(requestMessage.getToken());
        } catch (InvalidTokenException e) {
            throw new OAuthProblemException(TOKEN_REJECTED);
        }
        if (token == null || token.isAccessToken()) {
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
}
