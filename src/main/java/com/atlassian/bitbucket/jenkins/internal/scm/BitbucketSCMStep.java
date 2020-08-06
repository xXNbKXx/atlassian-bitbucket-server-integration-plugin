package com.atlassian.bitbucket.jenkins.internal.scm;

import com.atlassian.bitbucket.jenkins.internal.client.BitbucketClientFactoryProvider;
import com.atlassian.bitbucket.jenkins.internal.config.BitbucketPluginConfiguration;
import com.atlassian.bitbucket.jenkins.internal.config.BitbucketServerConfiguration;
import com.atlassian.bitbucket.jenkins.internal.credentials.GlobalCredentialsProvider;
import com.atlassian.bitbucket.jenkins.internal.credentials.JenkinsToBitbucketCredentials;
import com.atlassian.bitbucket.jenkins.internal.credentials.JenkinsToBitbucketCredentialsModule;
import com.atlassian.bitbucket.jenkins.internal.model.BitbucketNamedLink;
import com.atlassian.bitbucket.jenkins.internal.model.BitbucketProject;
import com.atlassian.bitbucket.jenkins.internal.model.BitbucketRepository;
import com.atlassian.bitbucket.jenkins.internal.model.RepositoryState;
import com.google.inject.Guice;
import hudson.Extension;
import hudson.model.Item;
import hudson.plugins.git.BranchSpec;
import hudson.plugins.git.GitTool;
import hudson.plugins.git.extensions.GitSCMExtensionDescriptor;
import hudson.scm.SCM;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.steps.scm.SCMStep;
import org.kohsuke.stapler.*;
import org.kohsuke.stapler.verb.POST;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class BitbucketSCMStep extends SCMStep {

    private List<BranchSpec> branches;
    private String cloneUrl;
    private String credentialsId;
    private String sshCredentialsId;
    private String id;
    private String projectKey;
    private final String projectName;
    private final String repositoryName;
    private String repositorySlug;
    private int repositoryId;
    private String selfLink;
    private final String serverId;
    private String mirrorName;

    @DataBoundConstructor
    public BitbucketSCMStep(String projectName, String repositoryName, String serverId) {
        this.id = UUID.randomUUID().toString();
        this.branches = Collections.singletonList(new BranchSpec("**"));

        if (isBlank(serverId)) {
            throw new BitbucketSCMException("Error creating Bitbucket SCM: No server configuration provided");
        }
        this.serverId = serverId;

        if (isBlank(projectName)) {
            throw new BitbucketSCMException("Error creating the Bitbucket SCM: The project name is blank");
        }
        this.projectName = projectName;

        if (isBlank(repositoryName)) {
            throw new BitbucketSCMException("Error creating the Bitbucket SCM: The repository name is blank");
        }
        this.repositoryName = repositoryName;
    }

    @DataBoundSetter
    public void setId(String id) {
        this.id = requireNonNull(id, "id");
    }

    @DataBoundSetter
    public void setCredentialsId(@Nullable String credentialsId) {
        this.credentialsId = StringUtils.stripToNull(credentialsId);
    }

    @DataBoundSetter
    public void setSshCredentialsId(@Nullable String sshCredentialsId) {
        this.sshCredentialsId = StringUtils.stripToNull(sshCredentialsId);
    }

    @DataBoundSetter
    public void setMirrorName(@Nullable String mirrorName) {
        this.mirrorName = StringUtils.stripToNull(mirrorName);
    }

    @DataBoundSetter
    public void setBranches(List<BranchSpec> branches) {
        this.branches = requireNonNull(branches, "branches");
    }

    public List<BranchSpec> getBranches() {
        return branches;
    }

    public String getCloneUrl() {
        return cloneUrl;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public String getSshCredentialsId() {
        return sshCredentialsId;
    }

    public String getId() {
        return id;
    }

    public String getMirrorName() {
        return mirrorName;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public String getRepositorySlug() {
        return repositorySlug;
    }

    public String getSelfLink() {
        return selfLink;
    }

    public String getServerId() {
        return serverId;
    }

    public int getRepositoryId() {
        return repositoryId;
    }

    @Override
    protected SCM createSCM() {
        DescriptorImpl descriptor = (DescriptorImpl) getDescriptor();
        Optional<BitbucketServerConfiguration> mayBeServerConf = descriptor.getConfiguration(serverId);
        if (!mayBeServerConf.isPresent()) {
            throw new BitbucketSCMException("Error creating the Bitbucket SCM: No Bitbucket Server configuration for serverId " + serverId);
        }
        BitbucketServerConfiguration serverConfiguration = mayBeServerConf.get();
        GlobalCredentialsProvider globalCredentialsProvider = serverConfiguration.getGlobalCredentialsProvider(
                format("Bitbucket SCM Step: Query Bitbucket for project [%s] repo [%s] mirror [%s]",
                        projectName,
                        repositoryName,
                        mirrorName));
        BitbucketScmHelper scmHelper =
                descriptor.getBitbucketScmHelper(serverConfiguration.getBaseUrl(), globalCredentialsProvider, credentialsId);
        BitbucketRepository repository;
        if (!isBlank(mirrorName)) {
            try {
                EnrichedBitbucketMirroredRepository mirroredRepository =
                        descriptor.createMirrorHandler(scmHelper)
                                .fetchRepository(
                                        new MirrorFetchRequest(
                                                serverConfiguration.getBaseUrl(),
                                                credentialsId,
                                                globalCredentialsProvider,
                                                projectName,
                                                repositoryName,
                                                mirrorName));
                repository = mirroredRepository.getRepository();
                cloneUrl = getCloneUrl(mirroredRepository.getMirroringDetails().getCloneUrls());
            } catch (MirrorFetchException ex) {
                throw new BitbucketSCMException("Error creating the Bitbucket SCM: " + ex.getMessage());
            }
        } else {
            repository = scmHelper.getRepository(projectName, repositoryName);
            cloneUrl = getCloneUrl(repository.getCloneUrls());
        }
        projectKey = repository.getProject().getKey();
        repositorySlug = repository.getSlug();
        selfLink = repository.getSelfLink();
        repositoryId = repository.getId();

        BitbucketProject bitbucketProject = new BitbucketProject(projectKey, null, projectName);
        List<BitbucketNamedLink> cloneUrls = singletonList(new BitbucketNamedLink(getCloneProtocol().name, cloneUrl));
        BitbucketRepository bitbucketRepository =
                new BitbucketRepository(repositoryId, repositoryName, bitbucketProject,
                        repositorySlug, RepositoryState.AVAILABLE, cloneUrls, selfLink);
        return new BitbucketSCM(id, branches, credentialsId, sshCredentialsId, null, null, serverId, bitbucketRepository);
    }

    private String getCloneUrl(List<BitbucketNamedLink> cloneUrls) {
        return cloneUrls.stream()
                .filter(link -> getCloneProtocol().name.equals(link.getName()))
                .findFirst()
                .map(BitbucketNamedLink::getHref)
                .orElse("");
    }

    private CloneProtocol getCloneProtocol() {
        return isBlank(sshCredentialsId) ? CloneProtocol.HTTP : CloneProtocol.SSH;
    }

    @Symbol("BitbucketSCMStep")
    @Extension
    public static final class DescriptorImpl extends SCMStepDescriptor implements BitbucketScmFormValidation, BitbucketScmFormFill {

        @Inject
        private BitbucketClientFactoryProvider bitbucketClientFactoryProvider;
        @Inject
        private BitbucketPluginConfiguration bitbucketPluginConfiguration;
        @Inject
        private BitbucketScmFormFillDelegate formFill;
        @Inject
        private BitbucketScmFormValidationDelegate formValidation;
        private transient JenkinsToBitbucketCredentials jenkinsToBitbucketCredentials;

        @Override
        @POST
        public FormValidation doCheckCredentialsId(@QueryParameter String credentialsId) {
            return formValidation.doCheckCredentialsId(credentialsId);
        }

        @Override
        public FormValidation doCheckSshCredentialsId(String credentialsId) {
            return formValidation.doCheckSshCredentialsId(credentialsId);
        }

        @Override
        @POST
        public FormValidation doCheckProjectName(@QueryParameter String serverId, @QueryParameter String credentialsId,
                                                 @QueryParameter String projectName) {
            return formValidation.doCheckProjectName(serverId, credentialsId, projectName);
        }

        @Override
        @POST
        public FormValidation doCheckRepositoryName(@QueryParameter String serverId,
                                                    @QueryParameter String credentialsId,
                                                    @QueryParameter String projectName,
                                                    @QueryParameter String repositoryName) {
            return formValidation.doCheckRepositoryName(serverId, credentialsId, projectName, repositoryName);
        }

        @Override
        @POST
        public FormValidation doCheckServerId(@QueryParameter String serverId) {
            return formValidation.doCheckServerId(serverId);
        }

        @Override
        public FormValidation doTestConnection(@QueryParameter String serverId,
                                               @QueryParameter String credentialsId,
                                               @QueryParameter String projectName,
                                               @QueryParameter String repositoryName,
                                               @QueryParameter String mirrorName) {
            return formValidation.doTestConnection(serverId, credentialsId, projectName, repositoryName, mirrorName);
        }

        @Override
        @POST
        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item context,
                                                     @QueryParameter String baseUrl,
                                                     @QueryParameter String credentialsId) {
            return formFill.doFillCredentialsIdItems(context, baseUrl, credentialsId);
        }

        @Override
        @POST
        public ListBoxModel doFillSshCredentialsIdItems(@QueryParameter String baseUrl,
                                                     @QueryParameter String credentialsId) {
            return formFill.doFillSshCredentialsIdItems(baseUrl, credentialsId);
        }

        @Override
        @POST
        public HttpResponse doFillProjectNameItems(@QueryParameter String serverId,
                                                   @QueryParameter String credentialsId,
                                                   @QueryParameter String projectName) {
            return formFill.doFillProjectNameItems(serverId, credentialsId, projectName);
        }

        @Override
        @POST
        public HttpResponse doFillRepositoryNameItems(@QueryParameter String serverId,
                                                      @QueryParameter String credentialsId,
                                                      @QueryParameter String projectName,
                                                      @QueryParameter String repositoryName) {
            return formFill.doFillRepositoryNameItems(serverId, credentialsId, projectName, repositoryName);
        }

        @Override
        @POST
        public ListBoxModel doFillServerIdItems(@QueryParameter String serverId) {
            return formFill.doFillServerIdItems(serverId);
        }

        @Override
        public ListBoxModel doFillMirrorNameItems(@QueryParameter String serverId, @QueryParameter String credentialsId,
                                                  @QueryParameter String projectName,
                                                  @QueryParameter String repositoryName,
                                                  @QueryParameter String mirrorName) {
            return formFill.doFillMirrorNameItems(serverId, credentialsId, projectName, repositoryName, mirrorName);
        }

        @Override
        public List<GitSCMExtensionDescriptor> getExtensionDescriptors() {
            return emptyList();
        }

        @Override
        public String getFunctionName() {
            return "bbs_checkout";
        }

        @Override
        public List<GitTool> getGitTools() {
            return emptyList();
        }

        @Override
        public boolean getShowGitToolOptions() {
            return false;
        }

        @Inject
        public void setJenkinsToBitbucketCredentials(
                JenkinsToBitbucketCredentials jenkinsToBitbucketCredentials) {
            this.jenkinsToBitbucketCredentials = jenkinsToBitbucketCredentials;
        }

        private BitbucketMirrorHandler createMirrorHandler(BitbucketScmHelper helper) {
            injectJenkinsToBitbucketCredentials();
            return new BitbucketMirrorHandler(
                    bitbucketClientFactoryProvider,
                    jenkinsToBitbucketCredentials,
                    (client, project, repo) -> helper.getRepository(project, repo));
        }

        private BitbucketScmHelper getBitbucketScmHelper(String bitbucketUrl,
                                                         GlobalCredentialsProvider globalCredentialsProvider,
                                                         @Nullable String credentialsId) {
            injectJenkinsToBitbucketCredentials();
            return new BitbucketScmHelper(bitbucketUrl,
                    bitbucketClientFactoryProvider,
                    globalCredentialsProvider,
                    credentialsId, jenkinsToBitbucketCredentials);
        }

        private Optional<BitbucketServerConfiguration> getConfiguration(@Nullable String serverId) {
            return bitbucketPluginConfiguration.getServerById(serverId);
        }

        private void injectJenkinsToBitbucketCredentials() {
            if (jenkinsToBitbucketCredentials == null) {
                Guice.createInjector(new JenkinsToBitbucketCredentialsModule()).injectMembers(this);
            }
        }
    }
}
