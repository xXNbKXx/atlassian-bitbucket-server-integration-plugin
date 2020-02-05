package com.atlassian.bitbucket.jenkins.internal.applink.oauth.rest;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.InvisibleAction;
import hudson.model.RootAction;
import org.kohsuke.stapler.WebMethod;

@Extension
public class BaseApplinkRestProxy extends InvisibleAction implements RootAction {

    private static final String URL_BASE = "bitbucket";

    private RequestTokenRestEndpoint requestTokenRestEndpoint = new RequestTokenRestEndpoint();
    private AccessTokenRestEndpoint accessTokenRestEndpoint = new AccessTokenRestEndpoint();

    @Override
    public String getUrlName() {
        return URL_BASE;
    }

    @WebMethod(name = "rest")
    public Action[] getRestEndpoint() {
        return new Action[]{
                requestTokenRestEndpoint,
                accessTokenRestEndpoint
        };
    }
}