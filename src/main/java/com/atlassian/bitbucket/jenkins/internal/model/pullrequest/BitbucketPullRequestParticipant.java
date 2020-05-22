package com.atlassian.bitbucket.jenkins.internal.model.pullrequest;

import com.atlassian.bitbucket.jenkins.internal.model.BitbucketUser;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BitbucketPullRequestParticipant {

    private final boolean approved;
    private final PullRequestRole role;
    private final PullRequestParticipantStatus status;
    private final BitbucketUser user;

    @JsonCreator
    public BitbucketPullRequestParticipant(
            @JsonProperty("approved") boolean approved,
            @JsonProperty("role") PullRequestRole role,
            @JsonProperty("status") PullRequestParticipantStatus status,
            @JsonProperty("user") BitbucketUser user) {
        this.user = user;
        this.role = role;
        this.approved = approved;
        this.status = status;
    }

    public boolean isApproved() {
        return approved;
    }

    public PullRequestRole getRole() {
        return role;
    }

    public PullRequestParticipantStatus getStatus() {
        return status;
    }

    public BitbucketUser getUser() {
        return user;
    }
}
