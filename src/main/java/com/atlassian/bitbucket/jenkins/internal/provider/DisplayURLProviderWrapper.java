package com.atlassian.bitbucket.jenkins.internal.provider;

import com.google.inject.ImplementedBy;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;

@ImplementedBy(DefaultDisplayURLProviderWrapper.class)
public interface DisplayURLProviderWrapper {

    /**
     * Returns the result of making a static get call on {@link DisplayURLProvider}
     *
     * @return the DisplayURLProviderInstance
     */
    DisplayURLProvider get();
}
