package com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.common.Consumer;

/**
 * Provides persistent storage for OAuth consumers. The implementation of this store should only concern itself
 * with the immediate task that it is being asked to perform.
 */
public interface ConsumerStore {

    /**
     * Add the consumer to the store.
     *
     * @param consumer the consumer
     */
    void add(Consumer consumer);

    /**
     * Retrieve a {@code Consumer} from the store whose {@code key} attribute is equal to the
     * {@code consumer} parameter.
     *
     * @param key the key
     * @return {@code Consumer} whose key is {@code token}
     */
    Consumer get(String key);

    /**
     * Deletes a consumer with the {@code key}.
     *
     * @param key the key
     */
    void delete(String key);
}
