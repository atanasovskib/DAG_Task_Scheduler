package com.atanasovski.dagscheduler.dependencies;

public class TaskDependencyInputBuilder {
    public final String outputTaskId;
    public final String outputArg;

    public TaskDependencyInputBuilder(String outputTaskId, String outputArg) {
        this.outputTaskId = outputTaskId;
        this.outputArg = outputArg;
    }

    public DependencyDescription asInput(String inputArgName) {
        return new DependencyDescription(DependencyType.ON_OUTPUT, inputArgName, outputTaskId, outputArg);
    }
}
