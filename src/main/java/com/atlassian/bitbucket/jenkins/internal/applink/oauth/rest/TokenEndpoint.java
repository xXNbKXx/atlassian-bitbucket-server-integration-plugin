package com.atlassian.bitbucket.jenkins.internal.applink.oauth.rest;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.temp.TempConsumerRegistrar;
import hudson.model.InvisibleAction;
import org.json.JSONException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.inject.Inject;
import javax.servlet.ServletException;
import java.io.IOException;

public class TokenEndpoint extends InvisibleAction {

    private AccessTokenRestEndpoint accessTokenRestEndpoint;
    private AuthorizeServlet authorizeServlet;
    private TempConsumerRegistrar consumerRegistrar;
    private RequestTokenRestEndpoint requestTokenRestEndpoint;

    @Inject
    public TokenEndpoint(
            AccessTokenRestEndpoint accessTokenRestEndpoint,
            AuthorizeServlet authorizeServlet,
            TempConsumerRegistrar consumerRegistrar,
            RequestTokenRestEndpoint requestTokenRestEndpoint) {
        this.accessTokenRestEndpoint = accessTokenRestEndpoint;
        this.authorizeServlet = authorizeServlet;
        this.consumerRegistrar = consumerRegistrar;
        this.requestTokenRestEndpoint = requestTokenRestEndpoint;
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
