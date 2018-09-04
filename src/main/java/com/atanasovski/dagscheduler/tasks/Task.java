package com.atanasovski.dagscheduler.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public abstract class Task {
    private static final Logger log = LoggerFactory.getLogger(Task.class);
    public final String taskId;

    public Task(String taskId) {
        log.trace("Created a new Task instance. Id: [{}]", taskId);
        this.taskId = Objects.requireNonNull(taskId);
        if (taskId.isEmpty()) {
            throw new IllegalArgumentException("Task Id can't be an empty string");
        }
    }

    public abstract void compute();

    public int weight(){
        return 1;
    }
}
