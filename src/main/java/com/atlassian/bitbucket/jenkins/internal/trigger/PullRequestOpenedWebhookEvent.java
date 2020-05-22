package com.atlassian.bitbucket.jenkins.internal.trigger;

import com.atlassian.bitbucket.jenkins.internal.model.BitbucketUser;
import com.atlassian.bitbucket.jenkins.internal.model.pullrequest.BitbucketPullRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PullRequestOpenedWebhookEvent extends PullRequestWebhookEvent {

    public PullRequestOpenedWebhookEvent(BitbucketUser actor, Date date, String eventKey,
                                         BitbucketPullRequest pullRequest) {
        super(actor, date, eventKey, pullRequest);
    }
}
