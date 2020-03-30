package com.atlassian.bitbucket.jenkins.internal.jenkins.oauth.servlet;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.Randomizer;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.token.ServiceProviderTokenStore;
import com.atlassian.bitbucket.jenkins.internal.jenkins.oauth.servlet.AuthorizeConfirmationConfig.AuthorizeConfirmationConfigDescriptor;
import hudson.model.Descriptor.FormException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Clock;
import java.util.Optional;

import static net.oauth.OAuth.OAUTH_TOKEN;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthorizeConfirmationConfigTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Mock
    private ServiceProviderTokenStore serviceProviderTokenStore;
    @Mock
    private Randomizer randomizer;
    @Mock
    private Clock clock;
    @Mock
    private StaplerRequest request;

    @Test
    public void throwsExceptionForInvalidToken() throws FormException {
        String aToken = "123";
        when(request.getParameter(OAUTH_TOKEN)).thenReturn(aToken);
        when(serviceProviderTokenStore.get(aToken)).thenReturn(Optional.empty());

        AuthorizeConfirmationConfigDescriptor descriptor = createDescriptor();

        expectedException.expect(FormException.class);
        descriptor.createInstance(request);
    }

    private AuthorizeConfirmationConfigDescriptor createDescriptor() {
        return new AuthorizeConfirmationConfigDescriptor(serviceProviderTokenStore, randomizer, clock);
    }
}