package com.atanasovski.dagscheduler.schedule;

import com.atanasovski.dagscheduler.dependencies.DependencyType;
import com.atanasovski.dagscheduler.tasks.FieldExtractor;
import com.atanasovski.dagscheduler.tasks.Sink;
import com.atanasovski.dagscheduler.tasks.Task;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Schedule<Output> {
    private static final Logger logger = LoggerFactory.getLogger(Schedule.class);
    public final DirectedAcyclicGraph<String, DefaultEdge> dependencyGraph;
    private final FieldExtractor fieldExtractor;
    private final Map<String, Task> taskInstances;
    private final List<ProcessedDependency> sinkDependencies;
    private final Map<String, List<ProcessedDependency>> taskDependencies;
    private final Table<String, String, Boolean> dependencySatisfaction;
    private final Map<String, Boolean> taskScheduled;
    public final Sink<Output> sinkTask;
    private boolean sinkComplete = false;

    public Schedule(DirectedAcyclicGraph<String, DefaultEdge> dependencyGraph, Map<String, Task> taskInstances,
            Map<String, List<ProcessedDependency>> taskDependencies, Sink<Output> sinkTask,
            List<ProcessedDependency> sinkDependencies) {
        this.dependencyGraph = dependencyGraph;
        this.sinkTask = sinkTask;
        this.fieldExtractor = new FieldExtractor();
        this.taskInstances = new HashMap<>(taskInstances);
        this.sinkDependencies = sinkDependencies;
        this.taskScheduled = new HashMap<>();

        this.taskInstances.forEach((key, value) -> this.taskScheduled.put(key, false));
        this.taskDependencies = new HashMap<>(taskDependencies);
        this.dependencySatisfaction = HashBasedTable.create();
        this.taskDependencies.forEach((inTaskId, dependencies) -> dependencies
                .forEach(dep -> this.dependencySatisfaction.put(inTaskId, dep.outputTaskId(), false)));

    }

    public synchronized List<Task> getReady() {
        if (this.sinkComplete) {
            logger.debug("Sink is complete, nothing ready left");
            return Collections.emptyList();
        }

        List<String> readyTasks = new LinkedList<>();
        List<String> taskNotYetScheduled = this.taskScheduled.entrySet().stream().filter(x -> !x.getValue())
                .map(Map.Entry::getKey).collect(Collectors.toList());

        boolean allTasksAreComplete = taskNotYetScheduled.isEmpty() && allDependenciesSatisfied();
        if (allTasksAreComplete) {
            logger.debug("All tasks have completed, returning sink task as ready");
            injectInputForTask(this.sinkTask, this.sinkDependencies);
            return Collections.singletonList(this.sinkTask);
        }

        logger.debug("Not all tasks are complete, searching for unscheduled, ready tasks");
        for (String inputTaskId : taskNotYetScheduled) {
            boolean allDependenciesSatisfied = this.dependencySatisfaction.row(inputTaskId).values().stream()
                    .allMatch(x -> x.equals(true));
            if (allDependenciesSatisfied) {
                readyTasks.add(inputTaskId);
            }
        }

        logger.debug("Ready task ids: [{}]", readyTasks);
        return readyTasks.stream().map(this::injectInput).collect(Collectors.toList());
    }

    private boolean allDependenciesSatisfied() {
        return this.dependencySatisfaction.values().stream()
                .allMatch(dependencySatisfiedStatus -> dependencySatisfiedStatus);
    }

    private void injectInputForTask(Task task, List<ProcessedDependency> dependencies) {
        dependencies.stream().filter(dep -> dep.type() == DependencyType.ON_OUTPUT).forEach(dep -> {
            String inputArg = dep.inputArg().orElseThrow(this::invalidOutputDependency);

            Field inputField = fieldExtractor.getInputField(task.getClass(), inputArg)
                    .orElseThrow(this::inputFieldMissing);

            String outputArg = dep.outputArg().orElseThrow(this::invalidOutputDependency);
            logger.debug("Injecting input for arg [{}] from output arg [{}]", inputArg, outputArg);

            Field outputField = fieldExtractor.getOutputField(dep.outputTaskType, outputArg)
                    .orElseThrow(this::outputFieldMissing);

            Task outputInstance = this.taskInstances.get(dep.outputTaskId());
            try {
                Object output = outputField.get(outputInstance);
                inputField.set(task, output);
            } catch (IllegalAccessException e) {
                logger.error("Field [{}] of [{}] is inaccessible", inputArg, task.getClass(), e);
                throw new IllegalStateException("Field is inaccessible for value injection");
            }
        });
    }

    private Task injectInput(String taskId) {
        logger.debug("Injecting input in [{}]", taskId);
        Task inputTask = this.taskInstances.get(taskId);
        List<ProcessedDependency> dependencies = this.taskDependencies.get(taskId);
        this.injectInputForTask(inputTask, dependencies);

        return inputTask;
    }

    private InputDependencyException outputFieldMissing() {
        return new InputDependencyException("Class didn't have a field annotated with required @TaskOutput arg name");
    }

    private InputDependencyException inputFieldMissing() {
        return new InputDependencyException("Class didn't have a field annotated with required @TaskInput arg name");
    }

    private InputDependencyException invalidOutputDependency() {
        return new InputDependencyException("Output dependency didn't have an input or output arg specified");
    }

    public synchronized void setRunning(String taskId) {
        logger.debug("Task [{}] submitted for running", taskId);
        Boolean wasAlreadyRunning = this.taskScheduled.put(taskId, true);
        if (wasAlreadyRunning != null && wasAlreadyRunning) {
            throw new IllegalStateException("Task " + taskId + " was already running");
        }
    }

    public synchronized void onTaskComplete(String taskId) {
        logger.debug("Task [{}] marking as complete", taskId);
        if (taskId.equals(this.sinkTask.taskId)) {
            this.sinkComplete = true;
            return;
        }

        Map<String, Boolean> dependentTasks = this.dependencySatisfaction.column(taskId);
        for (String dependentTaskId : dependentTasks.keySet()) {
            final boolean dependencySatisfied = true;
            dependentTasks.put(dependentTaskId, dependencySatisfied);
        }
    }

    public CompletableFuture<Output> onComplete() {
        return this.sinkTask.future;
    }
}
