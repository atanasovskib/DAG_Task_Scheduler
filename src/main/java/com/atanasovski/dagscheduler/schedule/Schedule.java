package com.atanasovski.dagscheduler.schedule;

import com.atanasovski.dagscheduler.dependencies.DependencyType;
import com.atanasovski.dagscheduler.tasks.FieldExtractor;
import com.atanasovski.dagscheduler.tasks.Task;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Schedule {
    private final Logger logger = LoggerFactory.getLogger(Schedule.class);

    private final FieldExtractor fieldExtractor;
    private final Map<String, Task> taskInstances;
    private final Map<String, List<ProcessedDependency>> taskDependencies;
    private final Table<String, String, Boolean> dependencySatisfaction;
    private final Map<String, Boolean> taskScheduled;

    public Schedule(FieldExtractor fieldExtractor,
                    Map<String, Task> taskInstances,
                    Map<String, List<ProcessedDependency>> taskDependencies) {
        this.fieldExtractor = fieldExtractor;
        this.taskInstances = new HashMap<>(taskInstances);
        this.taskScheduled = new HashMap<>();

        this.taskInstances.forEach((key, value) -> this.taskScheduled.put(key, false));
        this.taskDependencies = new HashMap<>(taskDependencies);
        Table<String, String, Boolean> satisfaction = HashBasedTable.create();
        this.taskDependencies.forEach((inTaskId, dependencies) -> dependencies.forEach(
                dep -> satisfaction.put(inTaskId, dep.outputTaskId(), false)));

        this.dependencySatisfaction = Tables.unmodifiableTable(satisfaction);
    }

    public synchronized List<Task> getReady() {
        logger.debug("Getting ready tasks");
        List<String> readyTasks = new LinkedList<>();
        List<String> taskNotYetScheduled = this.taskScheduled.entrySet()
                                                   .stream()
                                                   .filter(x -> !x.getValue())
                                                   .map(Map.Entry::getKey)
                                                   .collect(Collectors.toList());

        for (String inputTaskId : taskNotYetScheduled) {
            boolean allDependenciesSatisfied = this.dependencySatisfaction.row(inputTaskId)
                                                       .values()
                                                       .stream()
                                                       .allMatch(x -> x.equals(true));
            if (allDependenciesSatisfied) {
                readyTasks.add(inputTaskId);
            }
        }

        logger.debug("Read task ids: [{}]", readyTasks);
        // for each ready task inject input
        return readyTasks.stream()
                       .map(this::injectInput)
                       .collect(Collectors.toList());
    }

    private Task injectInput(String taskId) {
        logger.debug("Injecting input in [{}]", taskId);
        Task inputTask = this.taskInstances.get(taskId);
        this.taskDependencies.get(taskId)
                .stream()
                .filter(dep -> dep.type() != DependencyType.ON_OUTPUT)
                .forEach(dep -> {
                    String inputArg = dep.inputArg().orElseThrow(this::invalidOutputDependency);


                    Field inputField = fieldExtractor.getInputField(inputTask.getClass(), inputArg)
                                               .orElseThrow(this::inputFieldMissing);

                    String outputArg = dep.outputArg().orElseThrow(this::invalidOutputDependency);
                    logger.debug("Injecting input for arg [{}] from output arg [{}]", inputArg, outputArg);

                    Field outputField = fieldExtractor.getOutputField(dep.outputTaskType, outputArg)
                                                .orElseThrow(this::outputFieldMissing);

                    Task outputInstance = this.taskInstances.get(dep.outputTaskId());
                    try {
                        Object output = outputField.get(outputInstance);
                        inputField.set(inputTask, output);
                    } catch (IllegalAccessException e) {
                        logger.error("Field [{}] of [{}] is inaccessible", inputArg, inputTask.getClass(), e);
                        throw new IllegalStateException("Field is inaccessible for value injection");
                    }
                });

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
        Map<String, Boolean> dependentTasks = this.dependencySatisfaction.column(taskId);
        for (String dependentTaskId : dependentTasks.keySet()) {
            final boolean dependencySatisfied = true;
            dependentTasks.put(dependentTaskId, dependencySatisfied);
        }
    }
}
