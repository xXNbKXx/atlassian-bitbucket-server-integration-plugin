package com.atlassian.bitbucket.jenkins.internal.model.pullrequest;

import com.atlassian.bitbucket.jenkins.internal.model.BitbucketNamedLink;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.CheckForNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BitbucketPullRequest {

    private final BitbucketPullRequestParticipant author;
    private final int id;
    private final BitbucketPullRequestRef fromRef;
    private final boolean locked;
    private final List<BitbucketPullRequestParticipant> participants;
    private final List<BitbucketPullRequestParticipant> reviewers;
    private final PullRequestState state;
    private final String title;
    private final BitbucketPullRequestRef toRef;
    private final int version;
    private List<BitbucketNamedLink> cloneUrls = new ArrayList<>();
    private String selfLink;

    @JsonCreator
    public BitbucketPullRequest(
            @JsonProperty("author") BitbucketPullRequestParticipant author,
            @JsonProperty("id") int id,
            @JsonProperty("fromRef") BitbucketPullRequestRef fromRef,
            @JsonProperty("toRef") BitbucketPullRequestRef toRef,
            @JsonProperty("locked") boolean locked,
            @JsonProperty("participants") List<BitbucketPullRequestParticipant> participants,
            @JsonProperty("reviewers") List<BitbucketPullRequestParticipant> reviewers,
            @CheckForNull @JsonProperty("links") Map<String, List<BitbucketNamedLink>> links,
            @JsonProperty("state") PullRequestState state,
            @JsonProperty("title") String title,
            @JsonProperty("version") int version) {
        this.author = author;
        this.id = id;
        this.fromRef = fromRef;
        this.locked = locked;
        this.participants = participants;
        this.reviewers = reviewers;
        this.state = state;
        this.title = title;
        this.toRef = toRef;
        this.version = version;
        if (links != null) {
            setLinks(links);
        }
    }

    public int getId() {
        return id;
    }

    public PullRequestState getState() {
        return state;
    }

    public BitbucketPullRequestParticipant getAuthor() {
        return author;
    }

    public BitbucketPullRequestRef getFromRef() {
        return fromRef;
    }

    public boolean isLocked() {
        return locked;
    }

    public List<BitbucketPullRequestParticipant> getParticipants() {
        return participants;
    }

    public List<BitbucketPullRequestParticipant> getReviewers() {
        return reviewers;
    }

    public BitbucketPullRequestRef getToRef() {
        return toRef;
    }

    public List<BitbucketNamedLink> getCloneUrls() {
        return cloneUrls;
    }

    public String getSelfLink() {
        return selfLink;
    }

    public String getTitle() {
        return title;
    }

    public int getVersion() {
        return version;
    }

    private void setLinks(Map<String, List<BitbucketNamedLink>> rawLinks) {
        List<BitbucketNamedLink> clones = rawLinks.get("clone");
        if (clones != null) {
            cloneUrls = unmodifiableList(clones);
        } else {
            cloneUrls = emptyList();
        }
        List<BitbucketNamedLink> link = rawLinks.get("self");
        if (link != null && !link.isEmpty()) { // there should always be exactly one self link.
            selfLink = link.get(0).getHref();
        }
    }
}
