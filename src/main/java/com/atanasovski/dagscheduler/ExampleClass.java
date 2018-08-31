package com.atanasovski.dagscheduler;

import com.atanasovski.dagscheduler.annotations.TaskOutput;
import com.atanasovski.dagscheduler.dependencies.DependencyDescription;
import com.atanasovski.dagscheduler.dependencies.DependencyType;
import com.atanasovski.dagscheduler.schedule.Schedule;
import com.atanasovski.dagscheduler.schedule.ScheduleBuilder;
import com.atanasovski.dagscheduler.schedule.Scheduler;
import com.atanasovski.dagscheduler.tasks.SinkDefinition;
import com.atanasovski.dagscheduler.tasks.Task;
import com.atanasovski.dagscheduler.tasks.TaskDefinition;

import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.atanasovski.dagscheduler.dependencies.DependencyDescription.theCompletionOf;
import static com.atanasovski.dagscheduler.dependencies.DependencyDescription.theOutput;
import static com.atanasovski.dagscheduler.tasks.TaskBuilder.task;

public class ExampleClass extends Task {
    @TaskOutput(outputName = "output")
    public String b;

    public ExampleClass(String taskId) {
        super(taskId);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ScheduleBuilder<String> scheduleBuilder;
        TaskDefinition<TakeInputBrtOutput> print1 = task(TakeInputBrtOutput.class).called("Print 1")
                                                            .waitFor(theOutput("output")
                                                                             .ofTask("Start")
                                                                             .asInput("input"));
        TaskDefinition<TakeInputBrtOutput> print2 = task(TakeInputBrtOutput.class).called("Print 2")
                                                            .waitFor(theOutput("output")
                                                                             .ofTask("Start")
                                                                             .asInput("input"),
                                                                    theCompletionOf("Print 1"));

        TaskDefinition<TakeInputBrtOutput> print3 = task(TakeInputBrtOutput.class).called("Print 3")
                                                            .waitFor(theOutput("output")
                                                                             .ofTask("Start")
                                                                             .asInput("input"),
                                                                    theCompletionOf("Print 2"));
        scheduleBuilder = ScheduleBuilder.<String>startWith(task(ExampleClass.class).called("Start"))
                                  .add(print1)
                                  .add(print2)
                                  .add(print3);

        SinkDefinition<ExampleSink, String> sink = new SinkDefinition<>(
                "sink",
                ExampleSink.class,
                String.class,
                Collections.singletonList(new DependencyDescription(
                        DependencyType.ON_OUTPUT, "result", "Start", "output")));
        Schedule<String> schedule = scheduleBuilder.sink(sink).build();
        Scheduler<String> scheduler = new Scheduler<>(schedule, 2);
        Future<String> result = scheduler.start();
        System.out.println("In main:" + result.get());
        System.out.println("After get");
    }

    @Override
    public void compute() {
        this.b = "Симона ТЕ САКАМ!!!";
    }
}

