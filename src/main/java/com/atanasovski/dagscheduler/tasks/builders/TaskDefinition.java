package com.atanasovski.dagscheduler.tasks.builders;

import com.atanasovski.dagscheduler.dependencies.DependencyDescription;
import com.atanasovski.dagscheduler.tasks.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TaskDefinition<T extends Task> {
    public final String taskId;

    public final Class<T> taskClass;
    public final List<DependencyDescription> dependencies;

    public TaskDefinition(String taskId, Class<T> taskClass) {
        this.taskClass = taskClass;
        this.taskId = taskId;
        this.dependencies = Collections.emptyList();
    }

    public TaskDefinition(String taskId, Class<T> taskClass, List<DependencyDescription> dependencies) {
        this.taskClass = taskClass;
        this.dependencies = new ArrayList<>(dependencies);
        this.taskId = taskId;
    }

    public String taskId() {
        return taskId;
    }

    public TaskDefinition<T> waitFor(DependencyDescription... taskDependencyInputs) {
        return new TaskDefinition<>(taskId, taskClass, Arrays.asList(taskDependencyInputs));
    }
}
