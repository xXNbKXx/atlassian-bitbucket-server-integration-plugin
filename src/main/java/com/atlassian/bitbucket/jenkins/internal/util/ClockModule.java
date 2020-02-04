package com.atlassian.bitbucket.jenkins.internal.util;

import com.google.inject.Binder;
import com.google.inject.Module;

import java.time.Clock;

public class ClockModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind(Clock.class).toInstance(Clock.systemUTC());
    }
}
