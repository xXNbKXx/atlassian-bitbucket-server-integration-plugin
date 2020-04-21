package com.atlassian.bitbucket.jenkins.internal.client;

import com.atlassian.bitbucket.jenkins.internal.client.supply.BitbucketCapabilitiesSupplier;
import com.atlassian.bitbucket.jenkins.internal.model.AtlassianServerCapabilities;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class BitbucketCapabilitiesClientImplTest {

    @Mock
    private BitbucketCapabilitiesSupplier capabilitiesSupplier;
    @Mock
    private AtlassianServerCapabilities newCapabilities, cachedCapabilities;
    @InjectMocks
    private BitbucketCapabilitiesClientImpl capabilitiesClient;

    @Test
    public void testGetServerCapabilitiesNoCache() {
        Mockito.when(capabilitiesSupplier.get()).thenReturn(newCapabilities);
        assertEquals(newCapabilities, capabilitiesClient.getServerCapabilities());
        Mockito.verify(capabilitiesSupplier).get();
    }

    @Test
    public void testGetServerCapabilitiesWithCache() {
        Mockito.when(capabilitiesSupplier.get()).thenReturn(cachedCapabilities);
        capabilitiesClient.getServerCapabilities();
        Mockito.verify(capabilitiesSupplier).get();

        assertEquals(cachedCapabilities, capabilitiesClient.getServerCapabilities());
        Mockito.verifyNoMoreInteractions(capabilitiesSupplier.get());
    }
}