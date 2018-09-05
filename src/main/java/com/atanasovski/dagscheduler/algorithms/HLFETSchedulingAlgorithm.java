package com.atanasovski.dagscheduler.algorithms;

import com.atanasovski.dagscheduler.tasks.Task;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class HLFETSchedulingAlgorithm implements SchedulingAlgorithm {
    private static final Logger log = LoggerFactory.getLogger(HLFETSchedulingAlgorithm.class);

    private final DirectedAcyclicGraph<String, DefaultEdge> dependencyGraph;
    private final Map<String, Integer> taskWeights;
    private final Map<String, Integer> calculatedLevels;

    public HLFETSchedulingAlgorithm(DirectedAcyclicGraph<String, DefaultEdge> dependencyGraph,
                                    Map<String, Integer> taskWeights) {
        this.dependencyGraph = Objects.requireNonNull(dependencyGraph);
        this.taskWeights = Collections.unmodifiableMap(taskWeights);
        this.calculatedLevels = new HashMap<>();
        calculatePriorities();
    }

    @Override
    public List<Task> orderByPriority(List<Task> readyTasks) {

        List<Task> sortedByLevel = new ArrayList<>(readyTasks);

        sortedByLevel.sort((x, y) -> {
            int levelX = calculatedLevels.get(x.taskId);
            int levelY = calculatedLevels.get(y.taskId);
            return Integer.compare(levelX, levelY);
        });

        return sortedByLevel;
    }

    private void calculatePriorities() {
        Set<String> allVertices = this.dependencyGraph.vertexSet();
        Predicate<String> startingTask = task -> this.dependencyGraph.inDegreeOf(task) == 0;
        allVertices.stream().filter(startingTask).forEach(this::calculateLevelStartingFrom);
    }

    private int calculateLevelStartingFrom(String current) {
        if (this.calculatedLevels.containsKey(current)) {
            return this.calculatedLevels.get(current);
        }

        int taskWeight = this.taskWeights.get(current);
        if (this.dependencyGraph.outDegreeOf(current) == 0) {
            this.calculatedLevels.put(current, taskWeight);
            return taskWeight;
        } else {
            Set<DefaultEdge> edges = this.dependencyGraph.outgoingEdgesOf(current);
            Stream<String> neighbours = edges.stream().map(this.dependencyGraph::getEdgeTarget);

            int maxWeightOfNeighbours = neighbours.map(this::calculateLevelStartingFrom).max(Integer::compare)
                                                .orElse(0);
            int calculatedWeightForTask = taskWeight + maxWeightOfNeighbours;
            log.debug("Calculated weight for task [{}] = [{}]", current, calculatedWeightForTask);
            this.calculatedLevels.put(current, taskWeight);
            return calculatedWeightForTask;
        }
    }
}
