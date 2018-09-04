package com.atanasovski.dagscheduler.schedule;

import com.atanasovski.dagscheduler.NoProperConstructorException;
import com.atanasovski.dagscheduler.dependencies.DependencyDescription;
import com.atanasovski.dagscheduler.tasks.*;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;


public class ScheduleBuilder {
    private final DirectedAcyclicGraph<String, DefaultEdge> dependencyGraph = new DirectedAcyclicGraph<>(DefaultEdge.class);
    private final Map<String, TaskDefinition<? extends Task>> tasksInSchedule = new HashMap<>();
    private final Table<String, String, List<DependencyDescription>> dependencyTable = HashBasedTable.create();
    private Logger logger = LoggerFactory.getLogger(ScheduleBuilder.class);

    private ScheduleBuilder(TaskDefinition... startingTasks) {
        long numberOfDistinctTaskIds = Arrays.stream(startingTasks)
                                               .map(TaskDefinition::taskId)
                                               .distinct()
                                               .count();

        if (numberOfDistinctTaskIds != startingTasks.length) {
            throw new IllegalArgumentException("Multiple of the starting tasks have the same taskId");
        }

        for (TaskDefinition<? extends Task> task : startingTasks) {
            tasksInSchedule.put(task.taskId, task);
            dependencyGraph.addVertex(task.taskId);
        }

    }

    public static ScheduleBuilder startWith(TaskDefinition... startingTasks) {
        return new ScheduleBuilder(startingTasks);
    }

    public <T extends Task> ScheduleBuilder add(TaskDefinition<T> newTask) {
        String newTaskId = newTask.taskId;
        if (tasksInSchedule.containsKey(newTaskId)) {
            throw new IllegalArgumentException("Task " + newTaskId + " is already added to the schedule");
        }

        tasksInSchedule.put(newTaskId, newTask);
        dependencyGraph.addVertex(newTaskId);
        newTask.dependencies.forEach(dep -> {
            String dependeeTask = dep.outputTaskId;
            if (!tasksInSchedule.containsKey(dependeeTask)) {
                String errorMessage = "Task [" + dependeeTask + "] does not exist in the schedule. " +
                                              "Task [" + newTaskId + "] can't depend on it";
                throw new IllegalArgumentException(errorMessage);
            }

            dependencyGraph.addEdge(dependeeTask, newTaskId);
            if (!dependencyTable.contains(newTaskId, dependeeTask)) {
                dependencyTable.put(newTaskId, dependeeTask, new LinkedList<>());
            }

            List<DependencyDescription> deps = dependencyTable.get(newTaskId, dependeeTask);
            deps.add(dep);
        });

        return this;
    }

    public Schedule<Void> build() {
        SinkDefinition<Void, EmptySink> emptySinkDefinition = createEmptySink();
        return endWith(emptySinkDefinition);
    }

    private SinkDefinition<Void, EmptySink> createEmptySink() {
        return new SinkDefinition<>(EmptySink.class, Void.class, Collections.emptyList());
    }

    public <Result> Schedule<Result> endWith(SinkDefinition<Result, ? extends Sink<Result>> sinkDefinition) {
        sinkDefinition = Objects.requireNonNull(sinkDefinition);
        Map<String, List<ProcessedDependency>> processedDependencies = new HashMap<>();
        Map<String, Task> taskInstances = new HashMap<>();

        DependencyValidator dependencyValidator = new DependencyValidator();


        for (String taskId : this.tasksInSchedule.keySet()) {
            TaskDefinition<? extends Task> task = this.tasksInSchedule.get(taskId);
            List<ProcessedDependency> dependencies = processedDependencies(task.dependencies);
            dependencyValidator.validate(task.taskClass, dependencies);

            processedDependencies.put(taskId, dependencies);

            Task instance = createInstance(task.taskClass, taskId);
            taskInstances.put(taskId, instance);
        }

        Pair<Sink<Result>, List<ProcessedDependency>> sink = buildSink(sinkDefinition, dependencyValidator);
        return new Schedule<>(this.dependencyGraph, taskInstances, processedDependencies, sink.getFirst(), sink.getSecond());
    }

    private <Result, SinkType extends Sink<Result>> Pair<Sink<Result>, List<ProcessedDependency>> buildSink(
            SinkDefinition<Result, SinkType> sinkDefinition,
            DependencyValidator dependencyValidator) {
        // Sinks are tasks too, just not of interest to the user
        String sinkTaskId;
        do {
            sinkTaskId = UUID.randomUUID().toString();
        } while (this.tasksInSchedule.containsKey(sinkTaskId));

        List<ProcessedDependency> sinkDependencies = processedDependencies(sinkDefinition.dependencies);
        dependencyValidator.validate(sinkDefinition.producerType, sinkDependencies);


        Sink<Result> sinkTask = createInstance(sinkDefinition.producerType, sinkTaskId);
        return Pair.of(sinkTask, sinkDependencies);
    }

    private <T extends Task> T createInstance(Class<T> taskType, String taskId) {
        try {
            Constructor<T> noArgConstructor = taskType.getConstructor(String.class);
            if (!Modifier.isPublic(noArgConstructor.getModifiers())) {
                throw new NoProperConstructorException(taskType);
            }

            return noArgConstructor.newInstance(taskId);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
            logger.error("A public constructor with only one string arg not found in [{}]", taskType.getName());
            throw new NoProperConstructorException(taskType);
        }
    }

    private List<ProcessedDependency> processedDependencies(List<DependencyDescription> dependencyDescriptions) {
        return dependencyDescriptions.stream().map(x -> {
            Class<? extends Task> outputType = typeOf(x.outputTaskId);
            return new ProcessedDependency(outputType, x);
        }).collect(Collectors.toList());
    }

    private Class<? extends Task> typeOf(String taskId) {
        return Optional.ofNullable(this.tasksInSchedule.get(taskId))
                       .map(x -> x.taskClass)
                       .orElseThrow(() -> new IllegalStateException("Task [" + taskId + "] not found in schedule builder"));
    }
}


