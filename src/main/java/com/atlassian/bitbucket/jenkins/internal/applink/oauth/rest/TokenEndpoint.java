package com.atlassian.bitbucket.jenkins.internal.applink.oauth.rest;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.RandomizerImpl;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.ConsumerStore;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.ServiceProviderTokenStore;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.temp.InMemoryConsumerStore;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.temp.ServiceProviderTokenStoreImpl;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.temp.TempConsumerRegistrar;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.temp.TokenFactoryImpl;
import hudson.model.InvisibleAction;
import net.oauth.SimpleOAuthValidator;
import org.json.JSONException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.servlet.ServletException;
import java.io.IOException;
import java.time.Clock;

public class TokenEndpoint extends InvisibleAction {

    private RequestTokenRestEndpoint requestTokenRestEndpoint;
    private AccessTokenRestEndpoint accessTokenRestEndpoint;
    private AuthorizeServlet authorizeServlet;
    private TempConsumerRegistrar consumerRegistrar;

    public TokenEndpoint() {
        SimpleOAuthValidator oAuthValidator = new SimpleOAuthValidator();
        TokenFactoryImpl tokenFactory = new TokenFactoryImpl();
        ServiceProviderTokenStore tokenStore = new ServiceProviderTokenStoreImpl();
        ConsumerStore consumerStore = new InMemoryConsumerStore();
        Clock clock = Clock.systemUTC();

        requestTokenRestEndpoint =
                new RequestTokenRestEndpoint(oAuthValidator, consumerStore, tokenFactory, tokenStore);
        accessTokenRestEndpoint = new AccessTokenRestEndpoint(oAuthValidator, tokenFactory, tokenStore, clock);
        authorizeServlet = new AuthorizeServlet(tokenStore, new RandomizerImpl(), clock);
        consumerRegistrar = new TempConsumerRegistrar(consumerStore);
    }

    @RequirePOST
    @WebMethod(name = "access-token")
    public void doAccessToken(StaplerRequest request,
                              StaplerResponse response) throws ServletException, IOException {
        accessTokenRestEndpoint.handleAccessToken(request, response);
    }

    @RequirePOST
    @WebMethod(name = "request-token")
    public void doRequestToken(StaplerRequest req,
                               StaplerResponse resp) throws ServletException, IOException {
        consumerRegistrar.registerConsumer("stash-consumer", "foo");
        requestTokenRestEndpoint.handleRequestToken(req, resp);
    }

    @WebMethod(name = "authorize")
    public void getAuthorizeToken(StaplerRequest req,
                                  StaplerResponse resp) throws ServletException, JSONException, IOException {
        authorizeServlet.authorize(req, resp);
    }
}
