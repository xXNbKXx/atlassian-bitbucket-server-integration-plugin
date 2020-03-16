package com.atlassian.bitbucket.jenkins.internal.jenkins.oauth.consumer;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.consumer.Consumer;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.consumer.Consumer.Builder;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.consumer.Consumer.SignatureMethod;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.consumer.ConsumerStore;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.servlet.ServletException;
import java.net.URI;
import java.net.URISyntaxException;

import static com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.consumer.Consumer.SignatureMethod.HMAC_SHA1;
import static org.apache.commons.lang3.StringUtils.isAlphanumeric;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * A wrapper class exposing getter functions from an OAuthConsumer to the UI
 */
public class OAuthConsumerEntry extends AbstractDescribableImpl<OAuthConsumerEntry> {

    private static final OAuthConsumerEntry BLANK_ENTRY =
            new OAuthConsumerEntry(
                    Consumer.key("Enter Key")
                            .name("Enter Name")
                            .signatureMethod(SignatureMethod.HMAC_SHA1)
                            .build(), false);

    private final Consumer consumer;
    private final boolean isUpdate;

    public OAuthConsumerEntry(Consumer consumer, boolean isUpdate) {
        this.consumer = consumer;
        this.isUpdate = isUpdate;
    }

    public static OAuthConsumerEntry getOAuthConsumerForAdd() {
        return BLANK_ENTRY;
    }

    public static OAuthConsumerEntry getOAuthConsumerForUpdate(Consumer consumer) {
        return new OAuthConsumerEntry(consumer, true);
    }

    public String getCallbackUrl() {
        String callback = "";
        if (isCallbackUrlSet()) {
            callback = consumer.getCallback().toString();
        }
        return callback;
    }

    public boolean isCallbackUrlSet() {
        return consumer.getCallback() != null;
    }

    @Nonnull
    public String getConsumerKey() {
        return consumer.getKey();
    }

    @Nonnull
    public String getConsumerName() {
        return consumer.getName();
    }

    @SuppressWarnings("unused")
    public String getConsumerSecret() {
        return consumer.getConsumerSecret().orElse("");
    }

    public boolean isUpdate() {
        return isUpdate;
    }

    public OAuthConsumerEntryDescriptor getDescriptor() {
        return (OAuthConsumerEntryDescriptor) Jenkins.get().getDescriptorOrDie(getClass());
    }

    @Extension
    public static class OAuthConsumerEntryDescriptor extends Descriptor<OAuthConsumerEntry> {

        @Inject
        private ConsumerStore consumerStore;

        public FormValidation doCheckConsumerKey(@QueryParameter String consumerKey) {
            consumerKey = consumerKey.replaceAll("-", "");
            if (!isAlphanumeric(consumerKey)) {
                return FormValidation.error("Only Alphanumeric Consumer Key allowed");
            } else if (consumerStore.get(consumerKey) != null) {
                return FormValidation.error("Key with the same name already exists");
            } else {
                return FormValidation.ok();
            }
        }

        public FormValidation doCheckConsumerName(@QueryParameter String consumerName) {
            if (isBlank(consumerName)) {
                return FormValidation.error("Please provide a valid non empty consumer name");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckCallbackUrl(@QueryParameter String callbackUrl) {
            if (!isBlank(callbackUrl)) {
                try {
                    new URI(callbackUrl);
                } catch (URISyntaxException e) {
                    return FormValidation.error(e, "Invalid callback URL");
                }
            }
            return FormValidation.ok();
        }

        public Consumer getConsumerFromSubmittedForm(
                StaplerRequest request) throws ServletException, URISyntaxException {
            JSONObject data = request.getSubmittedForm();
            String consumerName = data.getString("consumerName");
            String callbackUrl = data.getString("callbackUrl");
            String consumerKey = data.getString("consumerKey");
            String consumerSecret = data.getString("consumerSecret");
            Builder builder = Consumer.key(consumerKey)
                    .name(consumerName)
                    .consumerSecret(consumerSecret)
                    .signatureMethod(HMAC_SHA1);
            if (!isBlank(callbackUrl)) {
                builder.callback(new URI(callbackUrl));
            }
            return builder.build();
        }
    }
}
