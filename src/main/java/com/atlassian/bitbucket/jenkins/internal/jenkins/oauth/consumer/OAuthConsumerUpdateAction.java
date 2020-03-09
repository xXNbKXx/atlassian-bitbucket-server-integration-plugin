package com.atlassian.bitbucket.jenkins.internal.jenkins.oauth.consumer;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.consumer.Consumer;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.consumer.ConsumerStore;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Action;
import hudson.model.Descriptor;
import jenkins.model.ModelObjectWithContextMenu;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.servlet.ServletException;
import java.net.URI;
import java.net.URISyntaxException;

import static com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.consumer.Consumer.SignatureMethod.HMAC_SHA1;
import static java.util.Objects.requireNonNull;

public class OAuthConsumerUpdateAction extends AbstractDescribableImpl<OAuthConsumerUpdateAction> implements Action, ModelObjectWithContextMenu {

    private final String consumerKey;
    private final ConsumerStore store;

    public OAuthConsumerUpdateAction(String consumerKey, ConsumerStore store) {
        this.consumerKey = requireNonNull(consumerKey, "consumerKey");
        this.store = requireNonNull(store, "store");
    }

    @Override
    public ContextMenu doContextMenu(StaplerRequest request, StaplerResponse response) throws Exception {
        return new ContextMenu().from(this, request, response);
    }

    //TODO: Can we delegate this work to a service class?
    @RequirePOST
    @SuppressWarnings("unused")
    public HttpResponse doPerformDelete(StaplerRequest req) {
        store.delete(consumerKey);
        return HttpResponses.redirectTo("../..");
    }

    //TODO: Can we delegate this work to a service class?
    @RequirePOST
    @SuppressWarnings("unused")
    public HttpResponse doPerformUpdate(StaplerRequest req) throws ServletException, URISyntaxException {
        JSONObject data = req.getSubmittedForm();

        String consumerName = data.getString("consumerName");
        String callbackUrl = data.getString("callbackUrl");
        String consumerKey = data.getString("consumerKey");
        String consumerSecret = data.getString("consumerSecret");

        Consumer.Builder builder = Consumer.key(consumerKey)
                .name(consumerName)
                .consumerSecret(consumerSecret)
                .signatureMethod(HMAC_SHA1);
        if (!StringUtils.isBlank(callbackUrl)) {
            builder.callback(new URI(callbackUrl));
        }
        return HttpResponses.redirectTo("../..");
    }

    @SuppressWarnings("unused") // Stapler
    public String getCallbackUrl() {
        return store.get(consumerKey).getCallback().toString();
    }

    @SuppressWarnings("unused") // Stapler
    public Descriptor getConsumerDescriptor() {
        Consumer consumer = store.get(consumerKey);
        if (consumer != null) {
            return new OAuthConsumerEntry(consumer).getDescriptor();
        }
        return null;
    }

    @SuppressWarnings("unused") // Stapler
    public String getConsumerKey() {
        return store.get(consumerKey).getKey();
    }

    @SuppressWarnings("unused") // Stapler
    public String getConsumerSecret() {
        return store.get(consumerKey).getConsumerSecret().orElse("");
    }

    @Override
    public String getDisplayName() {
        return "Update Consumer Configuration";
    }

    @Override
    public String getIconFileName() {
        return "setting.png";
    }

    @Override
    public String getUrlName() {
        return consumerKey;
    }

    @Extension
    @SuppressWarnings("unused") // Stapler
    @Symbol("oauth-consumer-update")
    public static class DescriptorImpl extends Descriptor<OAuthConsumerUpdateAction> {

        @Override
        public String getDisplayName() {
            return "Update OAuth Consumer";
        }
    }
}
