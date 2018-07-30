package com.atanasovski.dagscheduler.version2;

public class NoPublicDefaultConstructorInTaskType extends RuntimeException {
    public <T extends Task> NoPublicDefaultConstructorInTaskType(Class<T> taskType) {
        super("Task type " + taskType.getName() + " does not have a public default constructor");
    }
}
