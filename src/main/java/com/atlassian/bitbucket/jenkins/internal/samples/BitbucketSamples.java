package com.atlassian.bitbucket.jenkins.internal.samples;

import com.atlassian.bitbucket.jenkins.internal.config.BitbucketPluginConfiguration;
import com.atlassian.bitbucket.jenkins.internal.config.BitbucketServerConfiguration;
import com.google.inject.Injector;
import hudson.Extension;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.cps.GroovySample;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

public class BitbucketSamples {

    private BitbucketSamples() {
        //this class is a holder for samples. As such it should never be instantiated
    }

    @Extension
    public static class BitbucketMavenSample implements GroovySample {

        @Override
        public String name() {
            return "bbs-maven";
        }

        @Override
        public String script() {
            Injector injector = Jenkins.get().getInjector();
            if (injector != null) {
                BitbucketPluginConfiguration configuration = injector.getInstance(BitbucketPluginConfiguration.class);

                try {
                    StringBuffer output = new StringBuffer();
                    Files.readAllLines(Paths.get(BitbucketMavenSample.class.getResource("/samples/bbsMaven.groovy").toURI())).forEach(line -> {
                        if (line.endsWith("{replace}")) {
                            output.append("                ");
                            List<BitbucketServerConfiguration> serverList = configuration.getValidServerList();
                            if (serverList.size() == 0) {
                                output.append("\n//NOTE! You need to configure a Bitbucket server at the global level to use Bitbucket Server");
                            } else if (serverList.size() == 1) {
                                output.append("// Please replace the project and repostory name with the correct ones.\n");
                                output.append(convertToGroovyCheckoutString(serverList.get(0)));
                            } else {
                                output.append("// Please replace the project and repostory name with the correct ones.\n");
                                output.append("                // Several Bitbucket Server instances found, please uncomment the one that contains the repository to clone\n");
                                serverList.forEach(server -> {
                                    output.append("                // Bitbucket Server - ")
                                            .append(server.getServerName())
                                            .append("\n                //")
                                            .append(convertToGroovyCheckoutString(server))
                                            .append('\n');
                                });
                            }
                            output.append('\n');
                        } else {
                            output.append(line);
                            output.append('\n');
                        }
                    });
                    return output.toString();
                } catch (IOException | URISyntaxException e) {
                    //empty block as it just falls through to the default return below
                }
            }
            return "Failed to load sample";
        }

        @Override
        public String title() {
            return "Bitbucket server + maven";
        }

        private String convertToGroovyCheckoutString(BitbucketServerConfiguration server) {
            StringBuilder output = new StringBuilder();
            output.append("bbs_checkout ")
                    .append("projectName: 'sampleProject', ")
                    .append("repositoryName: 'sampleRepository',")
                    .append("serverId: '").append(server.getId()).append("', ")
                    .append("branches: [[name: '*/master']],")
                    .append("credentialsId: '").append(server.getCredentialsId()).append("', ")
                    .append("mirrorName: '', ")
                    .append("id: '").append(UUID.randomUUID().toString()).append("'");
            return output.toString();
        }
    }
}
