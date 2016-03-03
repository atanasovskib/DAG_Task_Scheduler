package com.atanasovski.dagscheduler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Blagoj on 02-Mar-16.
 */
public abstract class Schedule {
    private ConcurrentMap<Executable, Set<Executable>> taskToDependencies = new ConcurrentHashMap<>();
    private boolean hasErrors = false;
    private Set<Executable> runningTasks = new HashSet<>();
    private ConcurrentMap<String, List<Object>> results = new ConcurrentHashMap<>();
    private List<String> errors = new LinkedList<>();

    public ConcurrentMap<String, List<Object>> getResults() {
        return this.results;
    }

    public Schedule add(Executable exe) {
        if (!taskToDependencies.containsKey(exe)) {
            taskToDependencies.put(exe, new HashSet<>());
        }

        return this;
    }

    public Schedule add(Executable exe, Executable... dependencies) {
        if (!taskToDependencies.containsKey(exe)) {
            taskToDependencies.put(exe, new HashSet<>());
        }

        taskToDependencies.get(exe).addAll(Arrays.asList(dependencies));
        return this;
    }

    public synchronized boolean isDone() {
        if (this.hasErrors) {
            return true;
        }

        return this.taskToDependencies.isEmpty() && this.runningTasks.isEmpty();
    }

    public synchronized Executable[] getReadyTasks() {
        return taskToDependencies.entrySet().stream()
                .filter(entry -> entry.getValue().isEmpty())
                .map(entry -> entry.getKey())
                .toArray(size -> new Executable[size]);
    }

    public synchronized void removeForExecution(Executable task) {
        Set<Executable> deps = taskToDependencies.remove(task);
        if (deps.size() > 0) {
            throw new IllegalStateException("task has unresolved dependencies");
        }

        if (runningTasks.contains(task)) {
            throw new IllegalStateException("task already running");
        }

        runningTasks.add(task);
    }

    public synchronized void notifyDone(final Executable task) {
        final Map<String, List<Object>> outputParameters = task.getOutputParameters();
        boolean atLeastOneDependant = this.taskToDependencies.entrySet().stream().anyMatch(entry -> entry.getValue().contains(task));
        this.taskToDependencies.entrySet().stream().forEach(entry -> {
            boolean removed = entry.getValue().remove(task);
            if (removed) {
                entry.getKey().addInput(outputParameters);
            }
        });

        if (!atLeastOneDependant) {
            outputParameters.entrySet().forEach(entry -> {
                if (!this.results.containsKey(entry.getKey())) {
                    this.results.put(entry.getKey(), new LinkedList<>());
                }

                this.results.get(entry.getKey()).addAll(entry.getValue());
            });
        }

        this.runningTasks.remove(task);
    }

    public synchronized void notifyError(List<String> error) {
        this.errors.addAll(error);
        this.hasErrors = true;
    }

    public boolean hasErrors() {
        return this.hasErrors;
    }

    public List<String> getErrors() {
        return this.errors;
    }

}
