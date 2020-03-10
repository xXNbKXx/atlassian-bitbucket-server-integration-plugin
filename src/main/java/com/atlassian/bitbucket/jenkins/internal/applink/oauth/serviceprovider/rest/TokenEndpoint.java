package com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.rest;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.temp.TempConsumerRegistrar;
import hudson.model.InvisibleAction;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import java.io.IOException;

import static com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.rest.AccessTokenRestEndpoint.ACCESS_TOKEN_PATH_END;
import static com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.rest.RequestTokenRestEndpoint.REQUEST_TOKEN_PATH_END;

@Singleton
public class TokenEndpoint extends InvisibleAction {

    private final AccessTokenRestEndpoint accessTokenRestEndpoint;
    private final TempConsumerRegistrar consumerRegistrar;
    private final RequestTokenRestEndpoint requestTokenRestEndpoint;

    @Inject
    public TokenEndpoint(
            AccessTokenRestEndpoint accessTokenRestEndpoint,
            TempConsumerRegistrar consumerRegistrar,
            RequestTokenRestEndpoint requestTokenRestEndpoint) {
        this.accessTokenRestEndpoint = accessTokenRestEndpoint;
        this.consumerRegistrar = consumerRegistrar;
        this.requestTokenRestEndpoint = requestTokenRestEndpoint;
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
    @WebMethod(name = REQUEST_TOKEN_PATH_END)
    public void doRequestToken(StaplerRequest req,
                               StaplerResponse resp) throws ServletException, IOException {
        consumerRegistrar.registerConsumer("Stash", "stash-consumer", "foo");
        requestTokenRestEndpoint.handleRequestToken(req, resp);
    }
}
