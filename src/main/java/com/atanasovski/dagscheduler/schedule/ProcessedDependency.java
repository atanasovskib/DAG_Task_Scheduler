package com.atanasovski.dagscheduler.schedule;

import com.atanasovski.dagscheduler.dependencies.DependencyDescription;
import com.atanasovski.dagscheduler.dependencies.DependencyType;
import com.atanasovski.dagscheduler.tasks.Task;

import java.util.Objects;
import java.util.Optional;

public class ProcessedDependency {
    public final Class<? extends Task> outputTaskType;
    public final DependencyDescription dependency;

    ProcessedDependency(Class<? extends Task> outputTaskType, DependencyDescription dependency) {
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

    public String outputTaskId() {
        return dependency.outputTaskId;
    }
}
