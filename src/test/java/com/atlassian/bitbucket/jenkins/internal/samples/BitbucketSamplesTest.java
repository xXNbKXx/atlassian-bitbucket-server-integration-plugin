package com.atlassian.bitbucket.jenkins.internal.samples;

import com.atlassian.bitbucket.jenkins.internal.config.BitbucketPluginConfiguration;
import com.atlassian.bitbucket.jenkins.internal.config.BitbucketServerConfiguration;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BitbucketSamplesTest {

    @Mock
    private Injector injector;
    private BitbucketSamples.BitbucketMavenSample mavenSampler;
    @Mock
    private BitbucketPluginConfiguration pluginConfiguration;

    @Before
    public void setUp() {
        mavenSampler = new BitbucketSamples.BitbucketMavenSample(injector);
    }

    @Test
    public void testFailToLoad() {
        BitbucketSamples.BitbucketMavenSample sampler = new BitbucketSamples.BitbucketMavenSample();
        String sample = sampler.script();
        assertEquals("Failed to load sample", sample);
    }

    @Test
    public void testMultipleServers() {
        BitbucketServerConfiguration server1 = mock(BitbucketServerConfiguration.class);
        BitbucketServerConfiguration server2 = mock(BitbucketServerConfiguration.class);
        BitbucketServerConfiguration server3 = mock(BitbucketServerConfiguration.class);
        List<BitbucketServerConfiguration> servers = Arrays.asList(server1, server2, server3);

        when(injector.getInstance(BitbucketPluginConfiguration.class)).thenReturn(pluginConfiguration);
        when(pluginConfiguration.getValidServerList()).thenReturn(servers);
        when(server1.getId()).thenReturn("server1-id");
        when(server1.getServerName()).thenReturn("First server goes first");
        when(server2.getId()).thenReturn("server2-id");
        when(server2.getServerName()).thenReturn("Second server goes second");
        when(server3.getId()).thenReturn("server3-id-mac-idface");
        when(server3.getServerName()).thenReturn("Server mcServerFace");

        String sample = mavenSampler.script();
        assertServerPresent(sample, "First", server1);
        assertServerPresent(sample, "Second", server2);
        assertServerPresent(sample, "Third", server3);
    }

    @Test
    public void testNoServerConfigured() {
        when(injector.getInstance(BitbucketPluginConfiguration.class)).thenReturn(pluginConfiguration);
        when(pluginConfiguration.getValidServerList()).thenReturn(Collections.emptyList());
        String sample = mavenSampler.script();
        assertFalse("Script should have replaced the placeholder", sample.contains("{replace}"));
        assertTrue("Script should have had instructions for adding a BbS instance", sample.contains("/NOTE! You need to configure a Bitbucket server at the global level to use Bitbucket Server"));
    }

    @Test
    public void testSingleServer() {
        BitbucketServerConfiguration server = mock(BitbucketServerConfiguration.class);
        when(injector.getInstance(BitbucketPluginConfiguration.class)).thenReturn(pluginConfiguration);
        when(pluginConfiguration.getValidServerList()).thenReturn(Collections.singletonList(server));
        when(server.getId()).thenReturn("myServerId");
        String sample = mavenSampler.script();
        assertTrue("Script should have had put in the server", sample.contains("serverId: 'myServerId'"));
    }

    private void assertServerPresent(String sample, String messageIdentifier, BitbucketServerConfiguration server) {
        assertTrue(messageIdentifier + " server should be present", sample.contains("Bitbucket Server - " + server.getServerName()));
        assertTrue(messageIdentifier + " server id should be present", sample.contains("serverId: '" + server.getId() + "'"));
    }
}