package com.atlassian.bitbucket.jenkins.internal.fixture;

import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.model.TopLevelItem;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import java.util.EnumSet;
import java.util.Optional;

/**
 * Describes the kinds of jobs that the {@link JenkinsProjectRule} can create and run tests against. To support another
 * job type, it should be added to this list, and the project rule should be updated.
 */
public enum ProjectType {
    FREESTYLE(FreeStyleProject.class),
    PIPELINE(WorkflowJob.class);

    //Type casting of job into a consumable class is handled by getters- all supported types are both Jobs and TopLevelItems.
    private Class jobClass;

    ProjectType(Class jobClass) {
        this.jobClass = jobClass;
    }

    /**
     * Returns the class of the project as a {@link Job}. Used for interacting with the job itself, such as accessing the SCM.
     * @return The class as a Job
     */
    public Class<? extends Job> getJobClass() {
        return (Class<? extends Job>) jobClass;
    }

    /**
     * Returns the class of project type TopLevelItem. Used for instructing Jenkins to create the job
     * @return The class of the TopLevelItem
     */
    public Class<? extends TopLevelItem> getTopLevelItemClass() {
        return (Class<? extends TopLevelItem>) jobClass;
    }

    /**
     * Returns the ProjectType matching the provided job, if one exists
     * @param job the job
     * @return the matching ProjectType of the provided job if one exists; Optional.empty() otherwise
     */
    public static Optional<ProjectType> fromJob(Job job) {
        return EnumSet.allOf(ProjectType.class).stream()
                .filter(projectType -> projectType.jobClass.isInstance(job))
                .findFirst();
    }
}
