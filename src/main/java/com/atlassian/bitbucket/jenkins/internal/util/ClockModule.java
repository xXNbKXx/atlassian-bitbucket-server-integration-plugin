package com.atlassian.bitbucket.jenkins.internal.util;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.ConsumerStore;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.ServiceProviderTokenStore;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.TokenFactory;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.temp.InMemoryConsumerStore;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.temp.ServiceProviderTokenStoreImpl;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.temp.TokenFactoryImpl;
import com.google.inject.AbstractModule;
import net.oauth.OAuthValidator;
import net.oauth.SimpleOAuthValidator;

import java.time.Clock;

public class ClockModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Clock.class).toInstance(Clock.systemUTC());
        bind(OAuthValidator.class).to(SimpleOAuthValidator.class);
        bind(TokenFactory.class).to(TokenFactoryImpl.class);
        bind(ServiceProviderTokenStore.class).to(ServiceProviderTokenStoreImpl.class);
        bind(ConsumerStore.class).to(InMemoryConsumerStore.class);
    }
}
