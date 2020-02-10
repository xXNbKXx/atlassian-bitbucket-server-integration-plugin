package com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.temp;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.consumer.Consumer;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.token.ServiceProviderToken;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.token.TokenFactory;
import net.oauth.OAuthMessage;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.net.URI;

@Singleton
public class TokenFactoryImpl implements TokenFactory {

    @Override
    public ServiceProviderToken generateRequestToken(Consumer consumer, @Nullable URI callback, OAuthMessage message) {
        return ServiceProviderToken.newRequestToken("request-token123").tokenSecret("secret123").consumer(consumer).build();
    }

    @Override
    public ServiceProviderToken generateAccessToken(ServiceProviderToken token) {
        return ServiceProviderToken.newAccessToken("access-token123")
                .tokenSecret(token.getTokenSecret())
                .consumer(token.getConsumer())
                .authorizedBy(token.getUser())
                .session(newSession(token))
                .build();
    }

    private ServiceProviderToken.Session newSession(ServiceProviderToken token) {
        ServiceProviderToken.Session.Builder builder = ServiceProviderToken.Session.newSession("session123");
        if (token.getSession() != null) {
            builder.creationTime(token.getSession().getCreationTime());
        }
        return builder.build();
    }
}
