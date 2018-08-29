package com.atanasovski.dagscheduler;

import com.atanasovski.dagscheduler.annotations.TaskOutput;
import com.atanasovski.dagscheduler.schedule.Schedule;
import com.atanasovski.dagscheduler.schedule.ScheduleBuilder;
import com.atanasovski.dagscheduler.schedule.Scheduler;
import com.atanasovski.dagscheduler.tasks.Task;
import com.atanasovski.dagscheduler.tasks.TaskDefinition;

import static com.atanasovski.dagscheduler.dependencies.DependencyDescription.theCompletionOf;
import static com.atanasovski.dagscheduler.dependencies.DependencyDescription.theOutput;
import static com.atanasovski.dagscheduler.tasks.TaskBuilder.task;

public class ExampleClass extends Task {
    @TaskOutput(outputName = "output")
    public String b;

    public ExampleClass(String taskId) {
        super(taskId);
    }

    public static void main(String[] args) {
        ScheduleBuilder scheduleBuilder;
        TaskDefinition<TakeInputBrtOutput> print1 = task(TakeInputBrtOutput.class).called("Print 1")
                                                            .waitFor(theOutput("output")
                                                                             .ofTask("Start")
                                                                             .asInput("input"));
        TaskDefinition<TakeInputBrtOutput> print2 = task(TakeInputBrtOutput.class).called("Print 2")
                                                            .waitFor(theOutput("output")
                                                                             .ofTask("Start")
                                                                             .asInput("input"),
                                                                    theCompletionOf("Print 1"));
        scheduleBuilder = ScheduleBuilder.startWith(task(ExampleClass.class).called("Start"))
                                  .add(print1)
                                  .add(print2);

        Schedule schedule = scheduleBuilder.build();
        Scheduler scheduler = new Scheduler(schedule, 2);
        scheduler.start();
    }

    @Override
    public void compute() {
        this.b = "Симона ТЕ САКАМ!!!";
    }
}

