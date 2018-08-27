package com.atanasovski.dagscheduler.dependencies;

import java.util.Objects;

public class TaskDependencyBuilder {
    public final String outputArgName;

    public TaskDependencyBuilder(String outputArgName) {
        this.outputArgName = Objects.requireNonNull(outputArgName);
    }

    public TaskDependencyInputBuilder ofTask(String taskId) {
        return new TaskDependencyInputBuilder(taskId, outputArgName);
    }
}
