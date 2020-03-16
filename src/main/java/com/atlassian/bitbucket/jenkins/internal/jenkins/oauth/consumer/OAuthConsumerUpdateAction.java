package com.atlassian.bitbucket.jenkins.internal.jenkins.oauth.consumer;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.consumer.Consumer;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.consumer.ConsumerStore;
import com.atlassian.bitbucket.jenkins.internal.jenkins.oauth.consumer.OAuthConsumerEntry.OAuthConsumerEntryDescriptor;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Action;
import hudson.model.Descriptor;
import jenkins.model.ModelObjectWithContextMenu;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.servlet.ServletException;
import java.net.URISyntaxException;

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

    @RequirePOST
    @SuppressWarnings("unused")
    public HttpResponse doPerformDelete(StaplerRequest req) {
        store.delete(consumerKey);
        return HttpResponses.redirectTo("../..");
    }

    @RequirePOST
    @SuppressWarnings("unused")
    public HttpResponse doPerformUpdate(StaplerRequest req) throws ServletException, URISyntaxException {
        Consumer consumer = getConsumerDescriptor().getConsumerFromSubmittedForm(req);
        return HttpResponses.redirectTo("../..");
    }

    @SuppressWarnings("unused") // Stapler
    public OAuthConsumerEntryDescriptor getConsumerDescriptor() {
        Consumer consumer = store.get(consumerKey);
        if (consumer != null) {
            return getConsumerEntry().getDescriptor();
        }
        return null;
    }

    public OAuthConsumerEntry getConsumerEntry() {
        return OAuthConsumerEntry.getOAuthConsumerForUpdate(store.get(consumerKey));
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
