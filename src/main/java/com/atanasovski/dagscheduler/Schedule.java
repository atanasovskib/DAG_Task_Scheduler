package com.atanasovski.dagscheduler;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Blagoj on 02-Mar-16.
 */
public abstract class Schedule {
    private DefaultDirectedGraph<Executable, DefaultEdge> dependencies = new DefaultDirectedGraph<>(DefaultEdge.class);
    private boolean hasErrors = false;
    private ConcurrentMap<String, List<Object>> results = new ConcurrentHashMap<>();
    private List<String> errors = new LinkedList<>();
    private Set<Executable> runningTasks = new HashSet<>();

    public ConcurrentMap<String, List<Object>> getResults() {
        return this.results;
    }

    public Schedule add(Executable dependentTask, Executable... dependsOn) {
        this.dependencies.addVertex(dependentTask);
        for (Executable parentTask : dependsOn) {
            this.dependencies.addEdge(parentTask, dependentTask);
        }

        return this;
    }

    public synchronized boolean isDone() {
        if (this.hasErrors) {
            return true;
        }

        return this.dependencies.vertexSet().isEmpty();
    }

    public synchronized Executable[] getReadyTasks() {
        return this.dependencies.vertexSet().stream()
                .filter(task -> !this.runningTasks.contains(task) && this.dependencies.inDegreeOf(task) == 0)
                .toArray(size -> new Executable[size]);
    }

    public synchronized void notifyDone(final Executable task) {
        Objects.requireNonNull(task);
        if (!this.runningTasks.contains(task)) {
            throw new IllegalStateException(String.format("Task %s hasn't been started, or already executed", task.getId()));
        }

        final Map<String, List<Object>> outputParameters = task.getOutputParameters();
        Set<DefaultEdge> dependenciesOfTask = this.dependencies.outgoingEdgesOf(task);
        if (dependenciesOfTask != null && dependenciesOfTask.size() > 0) {
            dependenciesOfTask.stream().map(edge -> this.dependencies.getEdgeTarget(edge))
                    .forEach(dependant -> dependant.addInput(outputParameters));
        } else {
            outputParameters.entrySet().stream().forEach(entry -> {
                if (!this.results.containsKey(entry.getKey())) {
                    this.results.put(entry.getKey(), new LinkedList<>());
                }

                this.results.get(entry.getKey()).addAll(entry.getValue());
            });
        }

        this.runningTasks.remove(task);
        this.dependencies.removeVertex(task);
    }

    public synchronized void notifyError(final Collection<String> error) {
        Objects.requireNonNull(error);
        this.errors.addAll(error);
        this.hasErrors = true;
    }

    public boolean hasErrors() {
        return this.hasErrors;
    }

    public void setAsStarted(final Executable task) {
        Objects.requireNonNull(task);
        if (this.runningTasks.contains(task)) {
            throw new IllegalStateException(String.format("Task %s already started", task.getId()));
        }

        this.runningTasks.add(task);
    }

    public List<String> getErrors() {
        return this.errors;
    }

    public DefaultDirectedGraph<Executable, DefaultEdge> getDependencies() {
        return dependencies;
    }
}
