package com.atlassian.bitbucket.jenkins.internal.trigger;

import com.atlassian.bitbucket.jenkins.internal.model.BitbucketUser;
import com.atlassian.bitbucket.jenkins.internal.model.pullrequest.BitbucketPullRequest;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public abstract class PullRequestWebhookEvent extends AbstractWebhookEvent {

    private final BitbucketPullRequest pullRequest;

    @JsonCreator
    public PullRequestWebhookEvent(
            @JsonProperty("actor") BitbucketUser actor,
            @JsonProperty("date") Date date,
            @JsonProperty("eventKey") String eventKey,
            @JsonProperty("pullRequest") BitbucketPullRequest pullRequest) {
        super(actor, eventKey, date);
        this.pullRequest = pullRequest;
    }

    public BitbucketPullRequest getPullRequest() {
        return pullRequest;
    }
}
