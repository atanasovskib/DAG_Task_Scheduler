package com.atanasovski.dagscheduler.examples;

import com.atanasovski.dagscheduler.annotations.TaskInput;
import com.atanasovski.dagscheduler.tasks.Sink;

public class ExampleSink extends Sink<String> {
    @TaskInput(paramName = "result")
    public String result;

    public ExampleSink(String taskId) {
        super(taskId);
    }

    @Override
    public String getResult() {
        System.out.println("Get result in sink");
        return result;
    }
}
