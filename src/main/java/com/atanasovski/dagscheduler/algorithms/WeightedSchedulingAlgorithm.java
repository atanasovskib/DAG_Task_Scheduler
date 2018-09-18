package com.atanasovski.dagscheduler.algorithms;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public abstract class WeightedSchedulingAlgorithm implements SchedulingAlgorithm {
    /**
     * Initialized an instance of a weighted scheduling algorithm.
     * On construction validates the dependency graph and task weights
     *
     * @param dependencyGraph a directed acyclic graph where each vertex is the id of a task, and a directed edge
     *                        between task A and task B exists if task B depends on task A
     * @param taskWeights     a map with the assigned weights of each task, the higher the weight the higher the priority
     */

    public WeightedSchedulingAlgorithm(DirectedAcyclicGraph<String, DefaultEdge> dependencyGraph,
                                       Map<String, Integer> taskWeights) {
        dependencyGraph = Objects.requireNonNull(dependencyGraph);
        taskWeights = Collections.unmodifiableMap(taskWeights);
        if (dependencyGraph.vertexSet().size() != taskWeights.size()) {
            throw new IllegalArgumentException("Dependency graph and task weights map contain different number of tasks");
        }

        for (String taskInGraph : dependencyGraph.vertexSet()) {
            if (!taskWeights.containsKey(taskInGraph)) {
                throw new IllegalArgumentException("Task " + taskInGraph + " not found in task weights map");
            }
        }
    }
}
