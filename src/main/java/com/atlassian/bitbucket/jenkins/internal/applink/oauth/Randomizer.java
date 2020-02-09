package com.atlassian.bitbucket.jenkins.internal.applink.oauth;

import com.google.inject.ImplementedBy;

/**
 * Generates random values.
 */
@ImplementedBy(RandomizerImpl.class)
public interface Randomizer {

    /**
     * Generates and returns a random alpha-numeric string of the specified length.
     *
     * @param length length of the random string
     * @return random alpha-numeric string of the specified length
     */
    String randomAlphanumericString(int length);
}
