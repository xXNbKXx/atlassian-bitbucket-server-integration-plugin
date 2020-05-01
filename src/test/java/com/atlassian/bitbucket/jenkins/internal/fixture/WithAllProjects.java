package com.atlassian.bitbucket.jenkins.internal.fixture;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * For use with the {@link JenkinsProjectRule} to specify a method being run against all {@link ProjectType} specified
 * in the test rule. It will use this to ensure the selected types are available in testing.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface WithAllProjects {

}
