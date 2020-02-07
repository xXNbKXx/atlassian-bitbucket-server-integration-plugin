package com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.temp;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.common.Consumer;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.ServiceProviderToken;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.TokenFactory;
import net.oauth.OAuthMessage;

import javax.annotation.Nullable;
import java.net.URI;

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
