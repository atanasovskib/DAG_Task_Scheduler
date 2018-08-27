package com.atanasovski.dagscheduler;

import com.atanasovski.dagscheduler.tasks.Task;

public class NoPublicDefaultConstructorInTask extends RuntimeException {
    public <T extends Task> NoPublicDefaultConstructorInTask(Class<T> taskType) {
        super("Task type " + taskType.getName() + " does not have a public default constructor");
    }
}
