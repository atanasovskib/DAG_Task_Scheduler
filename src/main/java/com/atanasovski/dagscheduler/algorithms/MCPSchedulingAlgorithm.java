package com.atanasovski.dagscheduler.algorithms;

import com.atanasovski.dagscheduler.Executable;
import com.atanasovski.dagscheduler.Schedule;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Blagoj on 05-Mar-16.
 */
public class MCPSchedulingAlgorithm implements SchedulingAlgorithm {
    final Logger logger = LoggerFactory.getLogger(MCPSchedulingAlgorithm.class);
    private Map<Executable, Integer> alapTimes = new HashMap<>();
    private Map<Executable, List<Integer>> alapLists = new HashMap<>();
    private int minAlap = Integer.MAX_VALUE;

    @Override
    public Executable choose(final Executable... readyTasks) {
        Objects.requireNonNull(readyTasks);
        if (readyTasks.length == 0) {
            throw new IllegalArgumentException("readyTasks should not be of length 0");
        } else if (readyTasks.length == 1) {
            return readyTasks[0];
        }

        final List<Executable> readyAsList = Arrays.asList(readyTasks);
        logger.info("choosing from: {}", readyAsList.toString());
        List<Executable> sorted = alapLists.entrySet().stream()
                .filter(entry -> readyAsList.contains(entry.getKey()))
                .sorted((x, y) ->
                        x.getValue().toString().compareTo(y.getValue().toString()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        logger.info("sorted: " + sorted.toString());
        return sorted.get(0);
    }

    @Override
    public boolean usesPriority() {
        return true;
    }

    @Override
    public void calculatePriorities(final Schedule schedule) {
        calculateAlap(schedule.getDependencies());
        schedule.getDependencies().vertexSet().forEach(exe -> createALAPListForAllNodes(schedule.getDependencies(), exe));
    }

    private void calculateAlap(final DefaultDirectedGraph<Executable, DefaultEdge> graph) {
        Set<Executable> allVertices = graph.vertexSet();
        allVertices.stream()
                .filter(task -> graph.inDegreeOf(task) == 0)
                .forEach(task -> alapFromOneNode(graph, task));
        int executionTime = -this.minAlap;
        allVertices.forEach(exe -> this.alapTimes.put(exe, executionTime + this.alapTimes.get(exe)));
        this.alapTimes.entrySet().forEach(entry -> System.out.println(entry.getKey().getId() + ": " + entry.getValue()));
    }

    private int alapFromOneNode(final DefaultDirectedGraph<Executable, DefaultEdge> graph, final Executable current) {
        if (this.alapTimes.containsKey(current)) {
            return this.alapTimes.get(current);
        }

        if (graph.outDegreeOf(current) == 0) {
            int tmp = -current.getExecutionTime();
            this.alapTimes.put(current, tmp);
            this.minAlap = Math.min(tmp, this.minAlap);
            return tmp;
        } else {
            Set<DefaultEdge> edges = graph.outgoingEdgesOf(current);
            Stream<Executable> neighbours = edges.stream().map(graph::getEdgeTarget);
            int maxStartTimeOfNeighbours = neighbours.map(next -> alapFromOneNode(graph, next)).min(Integer::compare).get();
            int maxStartTimeOfCurrent = -current.getExecutionTime() + maxStartTimeOfNeighbours;
            this.minAlap = Math.min(maxStartTimeOfCurrent, this.minAlap);
            this.alapTimes.put(current, maxStartTimeOfCurrent);
            return maxStartTimeOfCurrent;
        }

    }

    private List<Integer> createALAPListForAllNodes(final DefaultDirectedGraph<Executable, DefaultEdge> graph, final Executable current) {
        System.out.println("calculateALAP List for: " + current.getId());
        if (alapLists.containsKey(current)) {
            System.out.println("result exists: " + alapLists.get(current));
            return alapLists.get(current);
        }

        if (graph.outDegreeOf(current) == 0) {
            System.out.println("ALAP leaf:" + Collections.singletonList(this.alapTimes.get(current)));
            List<Integer> result = Collections.singletonList(this.alapTimes.get(current));
            alapLists.put(current, result);
            return result;
        } else {
            Set<DefaultEdge> edges = graph.outgoingEdgesOf(current);
            Stream<Executable> neighbours = edges.stream().map(graph::getEdgeTarget);
            List<Integer> result = new LinkedList<>();
            neighbours.map(next -> createALAPListForAllNodes(graph, next))
                    .collect(Collectors.toList())
                    .forEach(result::addAll);
            result.add(this.alapTimes.get(current));
            result.sort(Integer::compare);
            alapLists.put(current, result);
            System.out.println("Calculated ALAP for: " + current.getId() + "; " + result.toString());
            return result;
        }
    }
}