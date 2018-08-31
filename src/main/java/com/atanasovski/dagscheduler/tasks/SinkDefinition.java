package com.atanasovski.dagscheduler.tasks;

import com.atanasovski.dagscheduler.dependencies.DependencyDescription;

import java.util.List;

public class SinkDefinition<T, U> {
    public final String taskId;

    public final Class<T> taskClass;
    public final Class<U> outputClass;

    public final List<DependencyDescription> dependencies;

    public SinkDefinition(
            String taskId,
            Class<T> taskClass,
            Class<U> outputClass,
            List<DependencyDescription> dependencies) {
        this.taskId = taskId;
        this.taskClass = taskClass;
        this.outputClass = outputClass;
        this.dependencies = dependencies;
    }
}
