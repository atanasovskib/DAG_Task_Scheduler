package com.atanasovski.dagscheduler.examples;

import com.atanasovski.dagscheduler.annotations.TaskInput;
import com.atanasovski.dagscheduler.tasks.Task;

public class TunelInputToOutputAfterAWhile extends Task {

    @TaskInput(paramName = "input")
    public String input;

    public TunelInputToOutputAfterAWhile(String taskId) {
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
