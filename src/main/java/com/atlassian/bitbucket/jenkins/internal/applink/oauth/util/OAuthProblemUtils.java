package com.atlassian.bitbucket.jenkins.internal.applink.oauth.util;

import net.oauth.OAuth;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility methods for servlets that implement OAuth.
 */
public class OAuthProblemUtils {

    public static void logOAuthProblem(final OAuthMessage message,
                                       final OAuthProblemException ope,
                                       final Logger logger) {
        if (OAuth.Problems.TIMESTAMP_REFUSED.equals(ope.getProblem())) {
            logger.log(Level.WARNING, "Rejecting OAuth request for url \"{}\" due to invalid timestamp ({}). " +
                                      "This is most likely due to our system clock not being " +
                                      "synchronized with the consumer's clock.",
                    new Object[]{message.URL, ope.getParameters()});
        } else if (logger.isLoggable(Level.FINE)) {
            // include the full stacktrace
            logger.log(Level.WARNING,
                    "Problem encountered authenticating OAuth client request for url \"" +
                    message.URL + "\", error was \"" + ope.getProblem() +
                    "\", with parameters \"" + ope.getParameters() + "\"", ope);
        } else {
            // omit the stacktrace
            logger.log(Level.WARNING,
                    "Problem encountered authenticating OAuth client for url \"{}\", error was \"{}\", with parameters \"{}\"",
                    new Object[]{message.URL, ope.getProblem(), ope.getParameters()}
            );
        }
    }
}
