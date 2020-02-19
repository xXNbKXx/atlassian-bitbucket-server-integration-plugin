package com.atlassian.bitbucket.jenkins.internal.applink.oauth;

import org.apache.commons.lang3.RandomStringUtils;

public class RandomizerImpl implements Randomizer {

    public String randomAlphanumericString(int length) {
        return RandomStringUtils.randomAlphanumeric(length);
    }
}
