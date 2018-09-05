package com.atanasovski.dagscheduler;

import com.atanasovski.dagscheduler.algorithms.HLFETSchedulingAlgorithm;
import com.atanasovski.dagscheduler.algorithms.SchedulingAlgorithm;
import com.atanasovski.dagscheduler.annotations.TaskOutput;
import com.atanasovski.dagscheduler.schedule.Schedule;
import com.atanasovski.dagscheduler.schedule.ScheduleBuilder;
import com.atanasovski.dagscheduler.schedule.Scheduler;
import com.atanasovski.dagscheduler.tasks.SinkDefinition;
import com.atanasovski.dagscheduler.tasks.Task;
import com.atanasovski.dagscheduler.tasks.TaskDefinition;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.atanasovski.dagscheduler.dependencies.DependencyDescription.theCompletionOf;
import static com.atanasovski.dagscheduler.dependencies.DependencyDescription.theOutput;
import static com.atanasovski.dagscheduler.tasks.SinkBuilder.produceA;
import static com.atanasovski.dagscheduler.tasks.TaskBuilder.task;

public class ExampleClass extends Task {
    @TaskOutput(outputName = "output")
    public String b;

    public ExampleClass(String taskId) {
        super(taskId);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        TaskDefinition<TakeInputBrtOutput> print1 = task(TakeInputBrtOutput.class).called("Print 1")
                                                            .waitFor(theOutput("output").ofTask("Start").asInput("input"));
        TaskDefinition<TakeInputBrtOutput> print2 = task(TakeInputBrtOutput.class).called("Print 2").waitFor(
                theOutput("output").ofTask("Start").asInput("input"), theCompletionOf("Print 1"));

        TaskDefinition<TakeInputBrtOutput> print3 = task(TakeInputBrtOutput.class).called("Print 3")
                                                            .waitFor(theOutput("output").ofTask("Start").asInput("input"));

        SinkDefinition<String, ExampleSink> sinkTask = produceA(String.class).with(ExampleSink.class)
                                                               .using(theOutput("output").ofTask("Start").asInput("result"));

        Schedule<String> schedule = ScheduleBuilder.startWith(task(ExampleClass.class).called("Start"))
                                            .add(print1).add(print2).add(print3).endWith(sinkTask);

        DirectedAcyclicGraph<String, DefaultEdge> dependencyGraph = schedule.dependencyGraph;
        Map<String, Integer> taskWeights = schedule.taskWeights;
        SchedulingAlgorithm schedulingAlgorithm = new HLFETSchedulingAlgorithm(dependencyGraph, taskWeights);
        Scheduler<String> scheduler = new Scheduler<>(schedule, schedulingAlgorithm, 2);
        Future<String> result = scheduler.start();
        System.out.println("In main:" + result.get());
    }

    @Override
    public void compute() {
        this.b = "Симона ТЕ САКАМ!!!";
    }
}
