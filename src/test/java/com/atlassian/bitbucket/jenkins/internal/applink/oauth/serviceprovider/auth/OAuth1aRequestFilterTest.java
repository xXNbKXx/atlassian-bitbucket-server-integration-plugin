package com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.auth;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.consumer.ConsumerStore;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.token.ServiceProviderTokenStore;
import net.oauth.OAuthValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Clock;

@RunWith(MockitoJUnitRunner.class)
public class OAuth1aRequestFilterTest {

    @Mock
    private ConsumerStore consumerStore;
    @Mock
    private ServiceProviderTokenStore serviceProviderTokenStore;
    @Mock
    private OAuthValidator validator;
    @Mock
    private Clock clock;
    @Mock
    private UnderlyingSystemAuthorizerFilter underlyingSystemAuthorizerFilter;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain chain;
    private OAuth1aRequestFilter filter;

    @Before
    public void setup() {
        filter =
                new OAuth1aRequestFilter(consumerStore, serviceProviderTokenStore, validator, clock, underlyingSystemAuthorizerFilter);
    }

    @Test
    public void verifyThatWhenOAuthParametersAreNotPresentWeLetTheRequestPassThru() throws Exception {
//        filter.doFilter(request, response, chain);
//
//        verify(chain).doFilter(isA(HttpServletRequest.class), isA(HttpServletResponse.class));
//        verify(response, never()).addHeader(eq("WWW-Authenticate"), startsWith("OAuth"));
//        verify(authenticationListener).authenticationNotAttempted(isA(HttpServletRequest.class), isA(HttpServletResponse.class));
//        verifyNoMoreInteractions(authenticationListener);
//        verifyZeroInteractions(authenticator);
    }
}