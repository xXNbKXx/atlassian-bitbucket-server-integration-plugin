package com.atlassian.bitbucket.jenkins.internal.applink.oauth.rest;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.InvisibleAction;
import hudson.model.UnprotectedRootAction;

@Extension
public class BaseApplinkRestAPI extends InvisibleAction implements UnprotectedRootAction {

    private static final String URL_BASE = "bitbucket";

    private TokenEndpoint tokenEndpoint = new TokenEndpoint();

    @Override
    public String getUrlName() {
        return URL_BASE;
    }

    public Action getRest() {
        return tokenEndpoint;
    }
}