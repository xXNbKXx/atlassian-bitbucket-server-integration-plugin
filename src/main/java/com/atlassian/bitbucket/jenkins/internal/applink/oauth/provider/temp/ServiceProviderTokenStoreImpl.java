package com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.temp;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.ServiceProviderToken;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.ServiceProviderTokenStore;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.StoreException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;

public class ServiceProviderTokenStoreImpl implements ServiceProviderTokenStore {

    private Map<String, ServiceProviderToken> tokens = new ConcurrentHashMap<>();

    @Override
    public ServiceProviderToken get(String token) throws StoreException {
        return tokens.get(token);
    }

    @Override
    public Iterable<ServiceProviderToken> getAccessTokensForUser(String username) {
        return tokens.values().stream().filter(token -> token.getUser().getName().equals(username)).collect(toList());
    }

    @Override
    public ServiceProviderToken put(ServiceProviderToken token) throws StoreException {
        tokens.put(token.getToken(), token);
        return token;
    }

    @Override
    public void remove(String token) throws StoreException {
        tokens.remove(token);
    }

    @Override
    public void removeExpiredTokens() throws StoreException {

    }

    @Override
    public void removeExpiredSessions() throws StoreException {

    }

    @Override
    public void removeByConsumer(String consumerKey) {

    }
}
