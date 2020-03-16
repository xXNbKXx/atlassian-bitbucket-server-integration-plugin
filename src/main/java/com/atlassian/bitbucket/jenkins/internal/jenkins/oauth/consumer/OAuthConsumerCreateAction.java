package com.atlassian.bitbucket.jenkins.internal.jenkins.oauth.consumer;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.consumer.Consumer;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.consumer.ConsumerStore;
import com.atlassian.bitbucket.jenkins.internal.jenkins.oauth.consumer.OAuthConsumerEntry.OAuthConsumerEntryDescriptor;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Action;
import hudson.model.Descriptor;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.inject.Inject;
import javax.servlet.ServletException;
import java.net.URISyntaxException;

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
        Consumer consumer = getConsumerDescriptor().getConsumerFromSubmittedForm(req);
        store.add(consumer);
        return HttpResponses.redirectTo("..");
    }

    @SuppressWarnings("unused") // Stapler
    public OAuthConsumerEntryDescriptor getConsumerDescriptor() {
        return OAuthConsumerEntry.getOAuthConsumerForAdd().getDescriptor();
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

        @Inject
        private ConsumerStore consumerStore;

        @Override
        public String getDisplayName() {
            return "Update OAuth Consumer";
        }
    }
}
