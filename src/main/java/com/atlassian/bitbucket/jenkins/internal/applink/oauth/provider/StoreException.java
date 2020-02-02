package com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider;

/**
 * Thrown if there is a problem storing or loading values.
 */
public class StoreException extends RuntimeException {

    public StoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public StoreException(String message) {
        super(message);
    }

    public StoreException(Throwable cause) {
        super(cause);
    }
}
