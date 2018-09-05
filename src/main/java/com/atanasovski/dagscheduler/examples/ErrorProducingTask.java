package com.atanasovski.dagscheduler.examples;

import com.atanasovski.dagscheduler.tasks.Task;

public class ErrorProducingTask extends Task {
    public ErrorProducingTask(String taskId) {
        super(taskId);
    }

    @Override
    public void compute() {
        throw new RuntimeException("Fuck you!!!");
    }
}
