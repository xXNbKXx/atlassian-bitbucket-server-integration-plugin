package com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.rest;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.Randomizer;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.temp.TempConsumerRegistrar;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.token.ServiceProviderTokenStore;
import hudson.model.Action;
import hudson.model.InvisibleAction;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import java.io.IOException;
import java.time.Clock;

import static com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.rest.AccessTokenRestEndpoint.ACCESS_TOKEN_PATH_END;

@Singleton
public class TokenEndpoint extends InvisibleAction {

    private AccessTokenRestEndpoint accessTokenRestEndpoint;
    private AuthorizeServlet authorizeServlet;
    private TempConsumerRegistrar consumerRegistrar;
    private RequestTokenRestEndpoint requestTokenRestEndpoint;
    private Clock clock;
    private ServiceProviderTokenStore tokenStore;
    private Randomizer randomizer;

    @Inject
    public TokenEndpoint(
            AccessTokenRestEndpoint accessTokenRestEndpoint,
            AuthorizeServlet authorizeServlet,
            TempConsumerRegistrar consumerRegistrar,
            RequestTokenRestEndpoint requestTokenRestEndpoint,
            Clock clock,
            ServiceProviderTokenStore tokenStore,
            Randomizer randomizer) {
        this.accessTokenRestEndpoint = accessTokenRestEndpoint;
        this.authorizeServlet = authorizeServlet;
        this.consumerRegistrar = consumerRegistrar;
        this.requestTokenRestEndpoint = requestTokenRestEndpoint;
        this.clock = clock;
        this.tokenStore = tokenStore;
        this.randomizer = randomizer;
    }

    @RequirePOST
    @SuppressWarnings("unused") // Stapler
    @WebMethod(name = ACCESS_TOKEN_PATH_END)
    public void doAccessToken(StaplerRequest request,
                              StaplerResponse response) throws ServletException, IOException {
        accessTokenRestEndpoint.handleAccessToken(request, response);
    }

    @RequirePOST
    @SuppressWarnings("unused") // Stapler
    @WebMethod(name = RequestTokenRestEndpoint.REQUEST_TOKEN_PATH_END)
    public void doRequestToken(StaplerRequest req,
                               StaplerResponse resp) throws ServletException, IOException {
        consumerRegistrar.registerConsumer("stash-consumer", "foo");
        requestTokenRestEndpoint.handleRequestToken(req, resp);
    }

    @SuppressWarnings("unused") // Stapler
    public Action getAuthorize(StaplerRequest req) /*throws IOException, OAuthProblemException*/ {
        /*OAuthMessage requestMessage = OAuthServlet.getMessage(req, null);
        requestMessage.requireParameters(OAUTH_TOKEN);
        return new AuthorizeAction(tokenStore, randomizer, clock, requestMessage.getToken());*/
        return new AuthorizeAction(tokenStore, randomizer, clock, "Test token");
    }
}
