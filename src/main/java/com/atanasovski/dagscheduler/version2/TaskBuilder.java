package com.atanasovski.dagscheduler.version2;

import java.util.Objects;

public class TaskBuilder {
    private final String taskId;

    private TaskBuilder(String taskId) {
        this.taskId = taskId;
    }

    public static TaskBuilder task(String taskId) {
        return new TaskBuilder(Objects.requireNonNull(taskId));
    }

    public <E extends Task> TaskTemplate<E> of(Class<E> taskType) {
        return new TaskTemplate<>(taskId, taskType);
    }
}
