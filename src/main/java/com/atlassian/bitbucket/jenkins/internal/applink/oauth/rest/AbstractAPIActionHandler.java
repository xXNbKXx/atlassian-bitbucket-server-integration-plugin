package com.atlassian.bitbucket.jenkins.internal.applink.oauth.rest;

import hudson.model.Action;

import javax.annotation.CheckForNull;

public class AbstractAPIActionHandler implements Action {

    private static final String URL_BASE = "bitbucket";

    @CheckForNull
    @Override
    public String getUrlName() {
        return null;
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
}
