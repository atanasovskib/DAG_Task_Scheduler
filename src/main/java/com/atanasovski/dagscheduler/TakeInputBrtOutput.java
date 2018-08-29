package com.atanasovski.dagscheduler;

import com.atanasovski.dagscheduler.annotations.TaskInput;
import com.atanasovski.dagscheduler.tasks.Task;

public class TakeInputBrtOutput extends Task {

    @TaskInput(paramName = "input")
    public String input;

    public TakeInputBrtOutput(String taskId) {
        super(taskId);
    }

    @Override
    public void compute() {
        System.out.println(taskId + " sleeping");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(taskId + ":" + input);
    }
}
