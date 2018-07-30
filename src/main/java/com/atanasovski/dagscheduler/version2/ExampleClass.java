package com.atanasovski.dagscheduler.version2;

import static com.atanasovski.dagscheduler.version2.TaskBuilder.task;

public class ExampleClass implements Task {
    @TaskOutput(outputName = "a")
    public String b;
    @TaskInput(paramName = "a")
    private int a;

    @Override
    public ExecutionResult compute() {
        Schedule.with(task("A").of(ExampleClass.class), task("B").of(ExampleClass.class))
                .and("A").dependsOn("B")
                .and("A").dependsOn("C");

        b = Integer.toString(a);
        return ExecutionResult.OK;

    }
}

// TODO компајлер за schedule да провери инпутот дали е достапен за да започне шкеџул
// TODO инјектор на инпут