package com.atanasovski.dagscheduler.algorithms;

import com.atanasovski.dagscheduler.tasks.Task;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class MDSchedulingAlgorithm implements SchedulingAlgorithm {

    private Map<String, Integer> taskWeights;
    private DirectedAcyclicGraph<String, DefaultEdge> dependencyGraph;

    public MDSchedulingAlgorithm(Map<String, Integer> taskWeights,
                                 DirectedAcyclicGraph<String, DefaultEdge> depdendencyGraph) {
        this.taskWeights = Collections.unmodifiableMap(taskWeights);
        this.dependencyGraph = Objects.requireNonNull(dependencyGraph);
    }

    @Override
    public List<Task> orderByPriority(List<Task> readyTasks) {
        if (readyTasks.size() <= 1) {
            return readyTasks;
        }

        Map<String, Float> calculatedMD = this.calculatePriorities();
        List<Task> sortedByPriority = new ArrayList<>(readyTasks);
        sortedByPriority.sort((x, y) -> {
            float xK = calculatedMD.get(x.taskId);
            float yK = calculatedMD.get(y.taskId);
            return Float.compare(xK, yK);
        });

        return sortedByPriority;
    }

    public Map<String, Float> calculatePriorities() {
        Map<String, Integer> alapTimes = this.calculateALAP();
        Map<String, Integer> asapTimes = this.calculateASAP();
        return this.calculateMD(alapTimes, asapTimes);
    }

    private Map<String, Float> calculateMD(Map<String, Integer> alapTimes, Map<String, Integer> asapTimes) {
        Map<String, Float> md = new HashMap<>();
        this.dependencyGraph.vertexSet().forEach(exe -> {
            int diff = alapTimes.get(exe) - asapTimes.get(exe);
            int currentTaskWeight = taskWeights.get(exe);
            float taskMd = diff / (float) currentTaskWeight;
            md.put(exe, taskMd);
        });

        return md;
    }

    private Map<String, Integer> calculateALAP() {
        Set<String> allVertices = dependencyGraph.vertexSet();
        Stream<String> startingTasks = allVertices.stream().filter(task -> dependencyGraph.inDegreeOf(task) == 0);
        Map<String, Integer> alapTimes = new HashMap<>();
        AtomicInteger minAlap = new AtomicInteger(0);
        startingTasks.forEach(task -> this.alapFromOneNode(alapTimes, minAlap, task));
        int executionTime = -minAlap.get();
        allVertices.forEach(exe -> alapTimes.put(exe, executionTime + alapTimes.get(exe)));
        return alapTimes;
    }

    private int alapFromOneNode(Map<String, Integer> alapTimes, AtomicInteger minAlap, String current) {
        if (alapTimes.containsKey(current)) {
            return alapTimes.get(current);
        }

        int currentTaskWeight = -taskWeights.get(current);
        int maxStartTimeOfNeighbours = 0;
        if (this.dependencyGraph.outDegreeOf(current) != 0) {
            Set<DefaultEdge> edges = dependencyGraph.outgoingEdgesOf(current);
            Stream<String> neighbours = edges.stream().map(dependencyGraph::getEdgeTarget);
            maxStartTimeOfNeighbours = neighbours.map(next -> alapFromOneNode(alapTimes, minAlap, next))
                                               .min(Integer::compare).get();
        }

        int maxStartTimeOfCurrent = currentTaskWeight + maxStartTimeOfNeighbours;
        minAlap.set(Math.min(maxStartTimeOfCurrent, minAlap.get()));
        alapTimes.put(current, maxStartTimeOfCurrent);
        return maxStartTimeOfCurrent;
    }

    private Map<String, Integer> calculateASAP() {
        Map<String, Integer> asapTimes = new HashMap<>();
        Set<String> allVertices = this.dependencyGraph.vertexSet();
        Stream<String> startingTasks = allVertices.stream().filter(task -> dependencyGraph.inDegreeOf(task) == 0);

        startingTasks.forEach(task -> calculateASAPFromOneNode(asapTimes, task, 0));
        return asapTimes;
    }

    private void calculateASAPFromOneNode(Map<String, Integer> asapTimes, String current, int startTime) {
        if (asapTimes.containsKey(current)) {
            int min = Math.max(asapTimes.get(current), startTime);
            asapTimes.put(current, min);
        } else {
            asapTimes.put(current, startTime);
        }

        if (this.dependencyGraph.outDegreeOf(current) == 0) {
            return;
        }

        Set<DefaultEdge> edges = this.dependencyGraph.outgoingEdgesOf(current);
        int currentTaskWeight = this.taskWeights.get(current);
        Stream<String> neighbours = edges.stream().map(dependencyGraph::getEdgeTarget);
        neighbours.forEach(neighbour -> calculateASAPFromOneNode(asapTimes, neighbour, startTime + currentTaskWeight));
    }
}
