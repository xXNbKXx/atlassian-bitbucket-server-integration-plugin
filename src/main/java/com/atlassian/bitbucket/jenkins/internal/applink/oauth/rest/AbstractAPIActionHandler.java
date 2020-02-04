package com.atlassian.bitbucket.jenkins.internal.applink.oauth.rest;

import com.atlassian.bitbucket.jenkins.internal.provider.JenkinsProvider;
import hudson.model.Action;

import javax.annotation.CheckForNull;
import javax.inject.Inject;

public class AbstractAPIActionHandler implements Action {

    private static final String URL_BASE = "bitbucket";

    @Inject
    private JenkinsProvider jenkinsProvider;

    AbstractAPIActionHandler() {
    }

    AbstractAPIActionHandler(JenkinsProvider jenkinsProvider) {
        this.jenkinsProvider = jenkinsProvider;
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return URL_BASE;
    }

    @CheckForNull
    @Override
    public String getIconFileName() {
        // No display
        return null;
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        // No display
        return null;
    }

    String getBaseUrl() {
        return jenkinsProvider.get().getRootUrl() + "/" + getUrlName();
    }

}
