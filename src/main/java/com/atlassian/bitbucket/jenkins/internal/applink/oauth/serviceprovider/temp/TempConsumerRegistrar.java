package com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.temp;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.consumer.Consumer;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.consumer.ServiceProviderConsumerStore;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TempConsumerRegistrar {

    private final ServiceProviderConsumerStore consumerStore;

    @Inject
    public TempConsumerRegistrar(ServiceProviderConsumerStore consumerStore) {
        this.consumerStore = consumerStore;
    }

    public void registerConsumer(String consumerKey, String consumerSecret) {
        if (!consumerStore.get(consumerKey).isPresent()) {
            consumerStore.add(Consumer.key(consumerKey).name(consumerKey).signatureMethod(Consumer.SignatureMethod.HMAC_SHA1).consumerSecret(consumerSecret).build());
        }
    }
}
