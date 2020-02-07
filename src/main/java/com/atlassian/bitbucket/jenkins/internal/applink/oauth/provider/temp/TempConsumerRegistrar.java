package com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.temp;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.common.Consumer;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.ConsumerStore;

public class TempConsumerRegistrar {

    private final ConsumerStore consumerStore;

    public TempConsumerRegistrar(ConsumerStore consumerStore) {
        this.consumerStore = consumerStore;
    }

    public void registerConsumer(String consumerKey, String consumerSecret) {
        Consumer consumer = consumerStore.get(consumerKey);
        if (consumer == null) {
            consumerStore.add(Consumer.key(consumerKey).name(consumerKey).signatureMethod(Consumer.SignatureMethod.HMAC_SHA1).consumerSecret(consumerSecret).build());
        }
    }
}
