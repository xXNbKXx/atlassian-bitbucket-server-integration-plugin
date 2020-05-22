package com.atlassian.bitbucket.jenkins.internal.model.pullrequest;

import com.atlassian.bitbucket.jenkins.internal.model.BitbucketRepository;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BitbucketPullRequestRef {

    private final String displayId;
    private final String id;
    private final String latestCommit;
    private final BitbucketRepository repository;

    @JsonCreator
    public BitbucketPullRequestRef(
            @JsonProperty("displayId") String displayId,
            @JsonProperty("id") String id,
            @JsonProperty("latestCommit") String latestCommit,
            @JsonProperty("repository") BitbucketRepository repository) {
        this.displayId = displayId;
        this.id = id;
        this.latestCommit = latestCommit;
        this.repository = repository;
    }

    public String getDisplayId() {
        return displayId;
    }

    public String getId() {
        return id;
    }

    public String getLatestCommit() {
        return latestCommit;
    }

    public BitbucketRepository getRepository() {
        return repository;
    }
}
