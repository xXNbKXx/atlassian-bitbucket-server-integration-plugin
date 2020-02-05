package com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.temp;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.ServiceProviderToken;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.ServiceProviderTokenStore;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.StoreException;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.stream.Collectors.toList;

public class ServiceProviderTokenStoreImpl implements ServiceProviderTokenStore {

    private List<ServiceProviderToken> tokens = new CopyOnWriteArrayList<>();

    @Override
    public ServiceProviderToken get(String token) throws StoreException {
        return tokens.stream()
                .filter(t -> t.getToken().equals(token))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No such token " + token));
    }

    @Override
    public Iterable<ServiceProviderToken> getAccessTokensForUser(String username) {
        return tokens.stream().filter(token -> token.getUser().getName().equals(username)).collect(toList());
    }

    @Override
    public ServiceProviderToken put(ServiceProviderToken token) throws StoreException {
        tokens.add(token);
        return token;
    }

    @Override
    public void remove(String token) throws StoreException {
        tokens.stream()
                .filter(t -> t.getToken().equals(token))
                .findFirst().ifPresent(tokens::remove);
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
