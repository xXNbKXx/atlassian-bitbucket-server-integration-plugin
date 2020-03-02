package com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.rest;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.Randomizer;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.exception.InvalidTokenException;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.token.ServiceProviderToken;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.token.ServiceProviderTokenStore;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.util.OAuthProblemUtils;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Action;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;
import net.oauth.OAuthProblemException;
import net.oauth.server.OAuthServlet;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.json.JSONException;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;
import java.time.Clock;
import java.util.Map;
import java.util.logging.Logger;

import static com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.token.ServiceProviderToken.*;
import static jenkins.model.Jenkins.ANONYMOUS;
import static net.oauth.OAuth.Problems.*;

public class AuthorizeAction extends AbstractDescribableImpl<AuthorizeAction> implements Action {

    private static final Logger LOGGER = Logger.getLogger(AuthorizeServlet.class.getName());
    private static final int VERIFIER_LENGTH = 6;
    private Clock clock;
    private Randomizer randomizer;
    private String token;
    private ServiceProviderTokenStore tokenStore;
    private String accessRequest;

    public AuthorizeAction(ServiceProviderTokenStore tokenStore, Randomizer randomizer, Clock clock, String token) {
        this.tokenStore = tokenStore;
        this.randomizer = randomizer;
        this.clock = clock;
        this.token = token;
    }

    @SuppressWarnings("unused") // Stapler
    public final HttpResponse doPerformSubmit(StaplerRequest request) throws IOException, ServletException, JSONException {
        JSONObject data = request.getSubmittedForm();
        Map<String, String[]> params = request.getParameterMap();
        if (params.containsKey("cancel")) {
            // Redirect to Bitbucket, nothing else happens
            return HttpResponses.redirectTo(/*consumer.getCallbackUrl()*/"http://www.atlassian.com");
        } else if (params.containsKey("authorize")) {
            ServiceProviderToken token;
            try {
                token = getTokenForAuthorization((String) data.get("token"));
            } catch (OAuthProblemException e) {
                OAuthProblemUtils.logOAuthProblem(OAuthServlet.getMessage(request, null), e, LOGGER);
                return HttpResponses.error(e);
            }

            String verifier = randomizer.randomAlphanumericString(VERIFIER_LENGTH);
            Principal userPrincipal = Jenkins.getAuthentication();
            if (ANONYMOUS.getPrincipal().equals(userPrincipal.getName())) {
                return HttpResponses.error(HttpServletResponse.SC_UNAUTHORIZED, "Error Message");
            } else {
                ServiceProviderToken newToken = token.authorize(userPrincipal.getName(), verifier);
                tokenStore.put(newToken);
                org.json.JSONObject json = new org.json.JSONObject();
                json.put("authorizeCode", newToken.getVerifier());

                //Not sure where this is returning to yet...
                return (staplerRequest, staplerResponse, node) -> {
                    staplerResponse.setContentType("application/json;charset=UTF-8");
                    PrintWriter pw = staplerResponse.getWriter();
                    pw.print(json);
                    pw.flush();
                };
            }

        } else {
            // Unexpected response to form. Angry Jenkins UI error here
            return HttpResponses.error(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
        }
    }

    public String getDisplayName() {
        //TODO: Need a "real" display name
        return "Authorize #PLACEHOLDER 1";
    }

    @SuppressWarnings("unused") //Stapler
    public String getAccessRequest() {
        return accessRequest;
    }

    @SuppressWarnings("unused") //Stapler
    public String getInstanceName() {
        return "Jenkins";
    }

    public String getAuthenticatedUsername() {
        return Jenkins.getAuthentication().getName();
    }

    @CheckForNull
    @Override
    public String getIconFileName() {
        return null;
    }

    public String getToken() {
        return token;
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return Jenkins.get().getRootUrl() != null ? Jenkins.get().getRootUrl() : "this domain";
    }

    private ServiceProviderToken getTokenForAuthorization(String rawToken) throws OAuthProblemException, IOException {
        ServiceProviderToken token;
        try {
            token = tokenStore.get(rawToken)
                    .orElseThrow(() -> new OAuthProblemException(TOKEN_REJECTED));
        } catch (InvalidTokenException e) {
            throw new OAuthProblemException(TOKEN_REJECTED);
        }
        if (token.isAccessToken()) {
            throw new OAuthProblemException(TOKEN_REJECTED);
        }
        if (token.getAuthorization() == Authorization.AUTHORIZED ||
            token.getAuthorization() == Authorization.DENIED) {
            throw new OAuthProblemException(TOKEN_USED);
        }
        if (token.hasExpired(clock)) {
            throw new OAuthProblemException(TOKEN_EXPIRED);
        }
        return token;
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
