package com.atlassian.bitbucket.jenkins.internal.model.pullrequest;

/** All possible statuses a participant can have with regard to a pull request, as declared in the Bitbucket Server javadoc. */
public enum PullRequestParticipantStatus {

    /**
     * Indicates that the participant has not finished reviewing the changes in the pull request.
     */
    UNAPPROVED,

    /**
     * Indicates that the participant has finished reviewing the changes in the pull request.
     * They have provided feedback which needs to be addressed before they can approve.
     */
    NEEDS_WORK,

    /**
     * Indicates that the reviewer has approved the changes in the pull request.
     */
    APPROVED;
}
