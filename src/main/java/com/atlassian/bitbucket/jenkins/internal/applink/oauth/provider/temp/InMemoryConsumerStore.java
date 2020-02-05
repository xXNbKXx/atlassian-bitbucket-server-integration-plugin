package com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.temp;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.common.Consumer;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.ConsumerStore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
