package com.atlassian.bitbucket.jenkins.internal.model.pullrequest;

/** Role of a {@link BitbucketPullRequestParticipant} as declared in the Bitbucket Server javadoc. */
public enum PullRequestRole {

    /**
     * The {@link BitbucketPullRequestParticipant participant} is an author of the {@link BitbucketPullRequest pull request}.
     */
    AUTHOR,

    /**
     * The {@link BitbucketPullRequestParticipant participant} is a reviewer of the {@link BitbucketPullRequest pull request}.
     */
    REVIEWER,

    /**
     * The {@link BitbucketPullRequestParticipant participant} has no explicit role in the {@link BitbucketPullRequest pull request}.
     */
    PARTICIPANT;
}
