package com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.common.Consumer;

public interface ConsumerStore {

    void add(Consumer consumer);

    Consumer get(String key);

    void delete(String key);
}
