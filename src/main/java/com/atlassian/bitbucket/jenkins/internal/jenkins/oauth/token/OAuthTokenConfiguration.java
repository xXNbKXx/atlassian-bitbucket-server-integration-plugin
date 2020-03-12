package com.atlassian.bitbucket.jenkins.internal.jenkins.oauth.token;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.consumer.Consumer;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.token.ServiceProviderTokenStore;
import com.atlassian.bitbucket.jenkins.internal.jenkins.oauth.consumer.OAuthGlobalConfiguration;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import javax.servlet.ServletException;
import java.time.Clock;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.consumer.Consumer.SignatureMethod.HMAC_SHA1;
import static com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.token.ServiceProviderToken.newRequestToken;

@Extension
public class OAuthTokenConfiguration implements Action, Describable<OAuthGlobalConfiguration> {

    public static final String REVOKE_BUTTON_NAME = "Revoke";

    @Inject
    private Clock clock;
    @Inject
    private ServiceProviderTokenStore tokenStore;
    private List<DisplayAccessToken> tokens;

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
        if (tokens == null) {
            tokens = Arrays.asList(
                    new DisplayAccessToken(
                            newRequestToken("1")
                                    .tokenSecret("123")
                                    .creationTime(new Date().getTime())
                                    .consumer(
                                            Consumer.key("1")
                                                    .name("cons")
                                                    .consumerSecret("123")
                                                    .signatureMethod(HMAC_SHA1)
                                                    .build()).build()
                                    .authorize("gj", "1"), clock),
                    new DisplayAccessToken(newRequestToken("2").tokenSecret("123").creationTime(new Date().getTime())
                            .consumer(Consumer.key("1").name("cons").consumerSecret("123").signatureMethod(HMAC_SHA1).build()).build().authorize("gj", "1"), clock),
                    new DisplayAccessToken(newRequestToken("3").tokenSecret("123").creationTime(new Date().getTime())
                            .consumer(Consumer.key("1").name("cons").consumerSecret("123").signatureMethod(HMAC_SHA1).build()).build().authorize("gj", "1"), clock),
                    new DisplayAccessToken(newRequestToken("4").tokenSecret("123").creationTime(new Date().getTime())
                            .consumer(Consumer.key("1").name("cons").consumerSecret("123").signatureMethod(HMAC_SHA1).build()).build().authorize("gj", "1"), clock),
                    new DisplayAccessToken(newRequestToken("5").tokenSecret("123").creationTime(new Date().getTime())
                            .consumer(Consumer.key("1").name("cons").consumerSecret("123").signatureMethod(HMAC_SHA1).build()).build().authorize("gj", "1"), clock),
                    new DisplayAccessToken(newRequestToken("6").tokenSecret("123").creationTime(new Date().getTime())
                            .consumer(Consumer.key("1").name("cons").consumerSecret("123").signatureMethod(HMAC_SHA1).build()).build().authorize("gj", "1"), clock)
            );
        }
        return tokens;

//        tokenStore.getAccessTokensForUser(Jenkins.getAuthentication().getName())
//                .forEach(token -> tokenList.add(new DisplayAccessToken(token, clock)));
//
//        return tokenList;
    }

    @RequirePOST
    public HttpResponse doRevoke(StaplerRequest request) throws ServletException {
        request
                .getParameterMap()
                .entrySet()
                .stream()
                .filter(e -> e.getValue().length == 1 && e.getValue()[0].equals(REVOKE_BUTTON_NAME))
                .map(Map.Entry::getKey)
                .forEach(t -> tokenStore.remove(t));
        return HttpResponses.redirectToDot();
    }

    @Override
    public Descriptor<OAuthGlobalConfiguration> getDescriptor() {
        return Jenkins.get().getDescriptorOrDie(getClass());
    }
}
