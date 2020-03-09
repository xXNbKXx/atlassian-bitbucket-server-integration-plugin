package com.atlassian.bitbucket.jenkins.internal.jenkins.oauth.consumer;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.consumer.Consumer;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.consumer.Consumer.SignatureMethod;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * A wrapper class exposing getter functions from an OAuthConsumer to the UI
 */
public class OAuthConsumerEntry extends AbstractDescribableImpl<OAuthConsumerEntry> {

    private static final OAuthConsumerEntry BLANK_ENTRY =
            new OAuthConsumerEntry(
                    Consumer.key("Enter Key")
                            .name("Enter Name")
                            .signatureMethod(SignatureMethod.HMAC_SHA1)
                            .build());

    private final Consumer consumer;

    public OAuthConsumerEntry(Consumer consumer) {
        this.consumer = consumer;
    }

    public static OAuthConsumerEntry getBlankEntry() {
        return BLANK_ENTRY;
    }

    public String getCallbackUrl() {
        String callback = "Not Set";
        if (consumer.getCallback() != null) {
            callback = consumer.getCallback().toString();
        }
        return callback;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    @Nonnull
    public String getConsumerKey() {
        return consumer.getKey();
    }

    public String getConsumerName() {
        return consumer.getName();
    }

    @Nonnull
    public Optional<String> getConsumerSecret() {
        return consumer.getConsumerSecret();
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<OAuthConsumerEntry> {

        @Override
        public String getDisplayName() {
            return "Consumer";
        }
    }
}
