package com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.temp;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.consumer.Consumer;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.consumer.ConsumerStore;

import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class InMemoryConsumerStore implements ConsumerStore {

    private Map<String, Consumer> consumers = new ConcurrentHashMap<>();

    @Override
    public void add(Consumer consumer) {
        consumers.put(consumer.getKey(), consumer);

    }

    @Override
    public Consumer get(String key) {
        return consumers.get(key);
    }

    @Override
    public void delete(String key) {
        consumers.remove(key);
    }
}
