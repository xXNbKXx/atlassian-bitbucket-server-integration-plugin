package com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.auth;

import com.atlassian.bitbucket.jenkins.internal.applink.oauth.serviceprovider.exception.NoSuchUserException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface UnderlyingSystemAuthorizerFilter {

    void authorize(String userName, HttpServletRequest request, HttpServletResponse response,
                   FilterChain filterChain) throws IOException, ServletException, NoSuchUserException;
}
