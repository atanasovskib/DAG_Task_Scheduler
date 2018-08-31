package com.atanasovski.dagscheduler.tasks;

import com.atanasovski.dagscheduler.dependencies.DependencyDescription;

import java.util.List;

public class SinkDefinition<Result, TaskType extends Sink<Result>> {
    public final Class<TaskType> producerType;
    public final Class<Result> resultType;

    public final List<DependencyDescription> dependencies;

    public SinkDefinition(
            Class<TaskType> producerType,
            Class<Result> resultType,
            List<DependencyDescription> dependencies) {
        this.producerType = producerType;
        this.resultType = resultType;
        this.dependencies = dependencies;
    }
}
