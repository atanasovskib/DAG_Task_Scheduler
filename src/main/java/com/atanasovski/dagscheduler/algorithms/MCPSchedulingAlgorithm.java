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

    private Map<Executable, List<Integer>> alapLists = new HashMap<>();

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
        final DefaultDirectedGraph<Executable, DefaultEdge> graph = schedule.getDependencies();
        Set<Executable> allVertices = graph.vertexSet();
        allVertices.stream()
                .filter(task -> graph.inDegreeOf(task) == 0)
                .forEach(task -> calculateALAP(graph, task));
        allVertices.stream().forEach(exe -> createALAPListForAllNodes(graph, exe));
    }

    private int calculateALAP(final DefaultDirectedGraph<Executable, DefaultEdge> graph, final Executable current) {
        if (current.hasExecutionWeight()) {
            return current.getExecutionWeight();
        }

        if (graph.outDegreeOf(current) == 0) {
            current.setExecutionWeight(current.getExecutionTime());
            return current.getExecutionWeight();
        } else {
            Set<DefaultEdge> edges = graph.outgoingEdgesOf(current);
            Stream<Executable> neighbours = edges.stream().map(graph::getEdgeTarget);
            int maxWeightOfNeighbours = neighbours.map(next -> calculateALAP(graph, next)).max(Integer::compare).get();
            int weightOfCurrent = current.getExecutionTime() + maxWeightOfNeighbours;
            current.setExecutionWeight(weightOfCurrent);
            return weightOfCurrent;
        }
    }


    private List<Integer> createALAPListForAllNodes(final DefaultDirectedGraph<Executable, DefaultEdge> graph, final Executable current) {
        System.out.println("calculateALAP List for: " + current.getId());
        if (alapLists.containsKey(current)) {
            System.out.println("result exists: " + alapLists.get(current));
            return alapLists.get(current);
        }

        if (graph.outDegreeOf(current) == 0) {
            System.out.println("ALAP leaf:" + Collections.singletonList(current.getExecutionWeight()));
            current.setExecutionWeight(current.getExecutionTime());
            List<Integer> result = Collections.singletonList(current.getExecutionWeight());
            alapLists.put(current, result);
            return result;
        } else {
            Set<DefaultEdge> edges = graph.outgoingEdgesOf(current);
            Stream<Executable> neighbours = edges.stream().map(graph::getEdgeTarget);
            List<Integer> result = new LinkedList<>();
            neighbours.map(next -> createALAPListForAllNodes(graph, next))
                    .collect(Collectors.toList())
                    .forEach(result::addAll);
            result.add(current.getExecutionWeight());
            result.sort(Integer::compare);
            alapLists.put(current, result);
            System.out.println("calculated ALAP for: " + current.getId() + "; " + result.toString());
            return result;
        }
    }
}