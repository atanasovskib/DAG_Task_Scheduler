package com.atanasovski.dagscheduler;

import com.atanasovski.dagscheduler.tasks.Task;

public class NoProperConstructorException extends RuntimeException {
    public <T extends Task> NoProperConstructorException(Class<T> taskType) {
        super("Task type " + taskType.getName() + " does not have a public constructor with one string argument");
    }
}
