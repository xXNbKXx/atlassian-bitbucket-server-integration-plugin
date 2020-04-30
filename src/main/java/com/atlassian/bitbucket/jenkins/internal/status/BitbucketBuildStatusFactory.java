package com.atlassian.bitbucket.jenkins.internal.status;

import com.atlassian.bitbucket.jenkins.internal.model.BitbucketBuildStatus;
import com.atlassian.bitbucket.jenkins.internal.model.BuildState;
import com.atlassian.bitbucket.jenkins.internal.provider.DisplayURLProviderWrapper;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;

import java.util.Arrays;
import java.util.Collection;

public final class BitbucketBuildStatusFactory {

    private static final Collection<Result> successfulResults = Arrays.asList(Result.SUCCESS, Result.UNSTABLE);

    /**
     * Creates a build status
     * @param build the build
     * @param displayURLProvider a static DisplayURLProvider. Use a {@link DisplayURLProviderWrapper}.
     * @return the build status
     */
    public static BitbucketBuildStatus fromBuild(Run<?, ?> build, DisplayURLProvider displayURLProvider) {
        Job<?, ?> parent = build.getParent();
        String key = parent.getFullName();
        String name = parent.getFullDisplayName();
        String url = displayURLProvider.getRunURL(build);
        BuildState state;
        if (build.isBuilding()) {
            state = BuildState.INPROGRESS;
        } else if (successfulResults.contains(build.getResult())) {
            state = BuildState.SUCCESSFUL;
        } else {
            state = BuildState.FAILED;
        }

        return new BitbucketBuildStatus.Builder(key, state, url)
                .setDescription(state.getDescriptiveText(build.getDisplayName(), build.getDurationString()))
                .setName(name)
                .build();
    }
}
