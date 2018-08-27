package com.atanasovski.dagscheduler.tasks;

import java.util.Objects;

public class TaskBuilder<T extends Task> {
    private final Class<T> typeOfTask;

    private TaskBuilder(Class<T> typeOfTask) {
        this.typeOfTask = Objects.requireNonNull(typeOfTask);
    }

    public static <T extends Task> TaskBuilder<T> task(Class<T> typeOfTask) {
        return new TaskBuilder<>(typeOfTask);
    }


    public TaskDefinition<T> called(String taskId) {
        return new TaskDefinition<T>(taskId, typeOfTask);
    }
}
