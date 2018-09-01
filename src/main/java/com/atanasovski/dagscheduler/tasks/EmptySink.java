package com.atanasovski.dagscheduler.tasks;

public class EmptySink extends Sink<Void> {

    public EmptySink(String taskId) {
        super(taskId);
    }

    @Override
    public Void getResult() {
        return null;
    }
}
