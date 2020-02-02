package com.atlassian.bitbucket.jenkins.internal.applink.oauth.provider;

/**
 * Exception thrown by the {@link ServiceProviderTokenStore#get(String)} method if the token is no longer valid.
 * This can be for a number of reasons, for example, if the user that authorized the token is no longer present.
 */
public class InvalidTokenException extends StoreException {

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidTokenException(Throwable cause) {
        super(cause);
    }

    public InvalidTokenException(String message) {
        super(message);
    }
}
