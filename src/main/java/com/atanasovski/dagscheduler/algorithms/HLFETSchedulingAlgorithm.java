package com.atanasovski.dagscheduler.algorithms;

import com.atanasovski.dagscheduler.tasks.Task;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class HLFETSchedulingAlgorithm extends WeightedSchedulingAlgorithm {

    private final Map<String, Integer> calculatedPriorities;

    /**
     * Creates an instance of a scheduling algorithm. On construction calculates the priorities for the given tasks
     * and their dependencies based on each task's own weight and the HLFET algorithm.
     *
     * @param dependencyGraph a directed acyclic graph where each vertex is the id of a task, and a directed edge
     *                        between task A and task B exists if task B depends on task A
     * @param taskWeights     a map with the assigned weights of each task, the higher the weight the higher the priority
     */
    public HLFETSchedulingAlgorithm(DirectedAcyclicGraph<String, DefaultEdge> dependencyGraph,
                                    Map<String, Integer> taskWeights) {
        super(dependencyGraph, taskWeights);

        this.calculatedPriorities = Collections.unmodifiableMap(this.calculatePriorities(dependencyGraph, taskWeights));
    }

    @Override
    public List<Task> orderByPriority(List<Task> readyTasks) {
        if (readyTasks.isEmpty()) {
            return Collections.emptyList();
        }

        List<Task> sortedByLevel = new ArrayList<>(readyTasks);

        sortedByLevel.sort((x, y) -> {
            int priorityOfX = calculatedPriorities.get(x.taskId);
            int priorityOfY = calculatedPriorities.get(y.taskId);
            return Integer.compare(priorityOfY, priorityOfX);
        });

        return sortedByLevel;
    }

    private Map<String, Integer> calculatePriorities(DirectedAcyclicGraph<String, DefaultEdge> dependencyGraph,
                                                     Map<String, Integer> taskWeights) {
        Set<String> allVertices = dependencyGraph.vertexSet();
        Predicate<String> isStartingTask = task -> dependencyGraph.inDegreeOf(task) == 0;
        HashMap<String, Integer> calculatedPriorities = new HashMap<>();

        Consumer<String> calculateLevel = startingTask -> calculateLevelStartingFrom(
                startingTask, dependencyGraph, taskWeights, calculatedPriorities);
        allVertices.stream()
                .filter(isStartingTask)
                .forEach(calculateLevel);

        return calculatedPriorities;
    }

    private int calculateLevelStartingFrom(String currentTask,
                                           DirectedAcyclicGraph<String, DefaultEdge> dependencyGraph,
                                           Map<String, Integer> taskWeights,
                                           Map<String, Integer> calculatedPriorities) {
        if (calculatedPriorities.containsKey(currentTask)) {
            return calculatedPriorities.get(currentTask);
        }

        int taskWeight = taskWeights.get(currentTask);
        if (dependencyGraph.outDegreeOf(currentTask) == 0) {
            calculatedPriorities.put(currentTask, taskWeight);
            return taskWeight;
        }

        Set<DefaultEdge> edges = dependencyGraph.outgoingEdgesOf(currentTask);
        Stream<String> neighbours = edges.stream().map(dependencyGraph::getEdgeTarget);

        Function<String, Integer> calculateLevel = neighbour -> calculateLevelStartingFrom(
                neighbour, dependencyGraph, taskWeights, calculatedPriorities);

        int maxWeightOfNeighbours = neighbours
                                            .map(calculateLevel)
                                            .max(Integer::compare)
                                            .orElse(0);

        int calculatedWeightForTask = taskWeight + maxWeightOfNeighbours;
        calculatedPriorities.put(currentTask, calculatedWeightForTask);
        return calculatedWeightForTask;
    }

}
