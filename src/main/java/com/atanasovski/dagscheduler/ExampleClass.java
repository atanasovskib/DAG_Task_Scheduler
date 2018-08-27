package com.atanasovski.dagscheduler;

import com.atanasovski.dagscheduler.tasks.Task;
import com.atanasovski.dagscheduler.annotations.TaskInput;
import com.atanasovski.dagscheduler.annotations.TaskOutput;

public class ExampleClass implements Task {
    @TaskOutput(outputName = "a")
    public String b;

    @TaskInput(paramName = "a")
    private int a;

    @Override
    public ExecutionStatus compute() {
        return ExecutionStatus.OK;
    }
}

// TODO компајлер за schedule да провери инпутот дали е достапен за да започне шкеџул
// TODO инјектор на инпут