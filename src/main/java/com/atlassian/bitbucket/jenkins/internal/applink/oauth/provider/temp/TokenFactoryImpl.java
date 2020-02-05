package com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.temp;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.common.Consumer;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.ServiceProviderToken;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.Token;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider.TokenFactory;
import net.oauth.OAuthMessage;

import javax.annotation.Nullable;
import java.net.URI;

public class TokenFactoryImpl implements TokenFactory {

    @Override
    public ServiceProviderToken generateRequestToken(Consumer consumer, @Nullable URI callback, OAuthMessage message) {
        return ServiceProviderToken.newRequestToken("request-token123").build();
    }

    @Override
    public ServiceProviderToken generateAccessToken(Token token) {
        return ServiceProviderToken.newAccessToken("access-token123").build();
    }
}
