package com.atlassian.bitbucket.jenkins.internal.model.pullrequest;

/** State of the pull request as declared in the Bitbucket Server javadoc. */
public enum PullRequestState {

    /**
     * The {@link BitbucketPullRequest pull request} has been declined.
     */
    DECLINED,
    /**
     * The {@link BitbucketPullRequest pull request} has been accepted and merged.
     */
    MERGED,
    /**
     * The {@link BitbucketPullRequest pull request} is open.
     */
    OPEN;
}
