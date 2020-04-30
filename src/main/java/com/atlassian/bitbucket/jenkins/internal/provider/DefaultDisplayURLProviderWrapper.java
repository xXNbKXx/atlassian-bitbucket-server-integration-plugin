package com.atlassian.bitbucket.jenkins.internal.provider;

import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;

public class DefaultDisplayURLProviderWrapper implements DisplayURLProviderWrapper {

    @Override
    public DisplayURLProvider get() {
        return DisplayURLProvider.get();
    }
}
