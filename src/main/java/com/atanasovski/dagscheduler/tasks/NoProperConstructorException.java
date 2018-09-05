package com.atanasovski.dagscheduler.tasks;

public class NoProperConstructorException extends RuntimeException {
    public <T extends Task> NoProperConstructorException(Class<T> taskType) {
        super("Task type " + taskType.getName() + " does not have a public constructor with one string argument, " +
                      "or the task class is not public");
    }
}
