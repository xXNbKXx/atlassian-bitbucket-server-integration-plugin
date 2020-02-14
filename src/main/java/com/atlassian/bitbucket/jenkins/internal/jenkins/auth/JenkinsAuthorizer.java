package com.atlassian.bitbucket.jenkins.internal.jenkins.auth;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.auth.UnderlyingSystemAuthorizerFilter;
import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.exception.NoSuchUserException;
import hudson.model.User;
import hudson.security.ACL;
import hudson.security.ACLContext;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;

public class JenkinsAuthorizer implements UnderlyingSystemAuthorizerFilter {

    private static final Logger log = Logger.getLogger(OAuthFilterRegistrar.class.getName());

    @Override
    public void authorize(String userName, HttpServletRequest request, HttpServletResponse response,
                          FilterChain filterChain) throws IOException, ServletException, NoSuchUserException {
        requireNonNull(userName, "userName");
        User u = User.getById(userName, false);
        if (u != null) {
            try (ACLContext ignored = ACL.as(u)) {
                filterChain.doFilter(request, response);
            }
        } else {
            throw new NoSuchUserException();
        }
    }
}
