package com.atanasovski.dagscheduler.algorithms;

import com.atanasovski.dagscheduler.tasks.Task;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MCPSchedulingAlgorithm extends WeightedSchedulingAlgorithm {
    private final Map<String, Integer> taskWeights;
    private final DirectedAcyclicGraph<String, DefaultEdge> dependencyGraph;
    private Map<String, Integer> alapTimes = new HashMap<>();
    private Map<String, List<Integer>> alapLists = new HashMap<>();
    private int minAlap = Integer.MAX_VALUE;

    public MCPSchedulingAlgorithm(DirectedAcyclicGraph<String, DefaultEdge> dependencyGraph,
                                  Map<String, Integer> taskWeights) {
        super(dependencyGraph, taskWeights);
        this.taskWeights = Collections.unmodifiableMap(taskWeights);
        this.dependencyGraph = dependencyGraph;
        this.calculateAlap();
        this.calculateAlapLists();
    }

    private void calculateAlapLists() {
        this.dependencyGraph.vertexSet().forEach(this::createAlapListForStartingNode);
    }

    @Override
    public List<Task> orderByPriority(List<Task> readyTasks) {
        if (readyTasks.size() <= 1) {
            return readyTasks;
        }

        List<Task> sortedByPriority = new ArrayList<>(readyTasks);
        sortedByPriority.sort((x, y) -> {
            List<Integer> xK = alapLists.get(x.taskId);
            List<Integer> yK = alapLists.get(y.taskId);
            return xK.toString().compareTo(yK.toString());
        });

        return sortedByPriority;
    }

    private void calculateAlap() {
        Set<String> allVertices = this.dependencyGraph.vertexSet();
        Stream<String> startingTasks = allVertices.stream()
                                               .filter(task -> dependencyGraph.inDegreeOf(task) == 0);

        startingTasks.forEach(this::alapFromOneNode);
        int executionTime = -this.minAlap;
        allVertices.forEach(exe -> this.alapTimes.put(exe, executionTime + this.alapTimes.get(exe)));
    }

    private int alapFromOneNode(String current) {
        if (this.alapTimes.containsKey(current)) {
            return this.alapTimes.get(current);
        }

        int weightOfCurrentTask = -taskWeights.get(current);
        int minStartTimeOfNeighbours = 0;
        if (dependencyGraph.outDegreeOf(current) != 0) {
            Set<DefaultEdge> edges = dependencyGraph.outgoingEdgesOf(current);
            Stream<String> neighbours = edges.stream().map(dependencyGraph::getEdgeTarget);
            minStartTimeOfNeighbours = neighbours.map(this::alapFromOneNode).min(Integer::compare).get();
        }

        int minStartTimeOfCurrent = weightOfCurrentTask + minStartTimeOfNeighbours;
        this.minAlap = Math.min(minStartTimeOfCurrent, this.minAlap);
        this.alapTimes.put(current, minStartTimeOfCurrent);
        return minStartTimeOfCurrent;
    }

    private List<Integer> createAlapListForStartingNode(String current) {
        if (alapLists.containsKey(current)) {
            return alapLists.get(current);
        }

        if (dependencyGraph.outDegreeOf(current) == 0) {
            List<Integer> result = Collections.singletonList(this.alapTimes.get(current));
            alapLists.put(current, result);
            return result;
        } else {
            Set<DefaultEdge> edges = dependencyGraph.outgoingEdgesOf(current);
            Stream<String> neighbours = edges.stream().map(dependencyGraph::getEdgeTarget);
            List<Integer> result = new LinkedList<>();
            neighbours.map(this::createAlapListForStartingNode)
                    .collect(Collectors.toList())
                    .forEach(result::addAll);
            result.add(this.alapTimes.get(current));
            result.sort(Integer::compare);
            alapLists.put(current, result);
            return result;
        }
    }
}