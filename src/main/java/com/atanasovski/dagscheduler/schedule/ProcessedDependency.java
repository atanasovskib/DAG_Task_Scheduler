package com.atanasovski.dagscheduler.schedule;

import com.atanasovski.dagscheduler.dependencies.DependencyDescription;
import com.atanasovski.dagscheduler.dependencies.DependencyType;

import java.util.Objects;
import java.util.Optional;

public class ProcessedDependency {
    public final Class outputTaskType;
    public final DependencyDescription dependency;

    ProcessedDependency(Class outputTaskType, DependencyDescription dependency) {
        this.outputTaskType = Objects.requireNonNull(outputTaskType);
        this.dependency = Objects.requireNonNull(dependency);
    }

    public DependencyType type() {
        return this.dependency.type;
    }

    public Optional<String> inputArg() {
        return dependency.inputArg();
    }

    public Optional<String> outputArg() {
        return dependency.outputArg();
    }
}
