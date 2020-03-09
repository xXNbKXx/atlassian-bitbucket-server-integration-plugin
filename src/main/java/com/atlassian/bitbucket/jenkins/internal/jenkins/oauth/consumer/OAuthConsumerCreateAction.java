package com.atlassian.bitbucket.jenkins.internal.jenkins.oauth.consumer;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.consumer.Consumer;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.consumer.ConsumerStore;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Action;
import hudson.model.Descriptor;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.servlet.ServletException;
import java.net.URI;
import java.net.URISyntaxException;

import static com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.consumer.Consumer.SignatureMethod.HMAC_SHA1;
import static java.util.Objects.requireNonNull;

public class OAuthConsumerCreateAction extends AbstractDescribableImpl<OAuthConsumerCreateAction> implements Action {

    private final ConsumerStore store;

    public OAuthConsumerCreateAction(ConsumerStore store) {
        this.store = requireNonNull(store, "store");
    }

    //TODO: Can we delegate this work to a service class?
    @RequirePOST
    @SuppressWarnings("unused") // Stapler
    public HttpResponse doPerformCreate(StaplerRequest req) throws ServletException, URISyntaxException {
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
        store.add(builder.build());
        return HttpResponses.redirectTo("..");
    }

    @SuppressWarnings("unused") // Stapler
    public Descriptor getConsumerDescriptor() {
        return OAuthConsumerEntry.getBlankEntry().getDescriptor();
    }

    @Override
    public String getDisplayName() {
        return "Create New Consumer";
    }

    @Override
    public String getIconFileName() {
        return "setting.png";
    }

    @Override
    public String getUrlName() {
        return "new";
    }

    @Extension
    @SuppressWarnings("unused") // Stapler
    @Symbol("oauth-consumer-create")
    public static class DescriptorImpl extends Descriptor<OAuthConsumerCreateAction> {

        @Override
        public String getDisplayName() {
            return "Update OAuth Consumer";
        }
    }
}
