package com.atlassian.bitbucket.jenkins.internal.jenkins.oauth.token;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.token.ServiceProviderTokenStore;
import com.atlassian.bitbucket.jenkins.internal.jenkins.oauth.consumer.OAuthGlobalConfiguration;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import javax.servlet.ServletException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

@Extension
public class OAuthTokenConfiguration implements Action, Describable<OAuthGlobalConfiguration> {

    @Inject
    private Clock clock;
    @Inject
    private ServiceProviderTokenStore tokenStore;

    @CheckForNull
    @Override
    public String getIconFileName() {
        return "secure.gif";
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return "access-tokens";
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return "tokens";
    }

    @SuppressWarnings("unused") // Stapler
    public List<DisplayAccessToken> getTokens() {
        List<DisplayAccessToken> tokenList = new ArrayList<>();
        tokenStore.getAccessTokensForUser(Jenkins.getAuthentication().getName())
                .forEach(token -> tokenList.add(new DisplayAccessToken(token, clock)));

        return tokenList;
    }

    @RequirePOST
    public HttpResponse doRevoke(StaplerRequest request) throws ServletException {
        JSONObject data = request.getSubmittedForm();

        data.keySet().stream()
                .map(Object::toString)
                .filter(k -> ((String) k).startsWith("token:"))
                .forEach(key -> {
                    boolean isSet = data.getBoolean((String) key);
                    if (isSet) {
                        String tokenName = ((String) key).replace("token:", "").trim();
                        tokenStore.remove(tokenName);
                    }
                });
        return HttpResponses.redirectToDot();
    }

    @Override
    public Descriptor<OAuthGlobalConfiguration> getDescriptor() {
        return Jenkins.get().getDescriptorOrDie(getClass());
    }
}
