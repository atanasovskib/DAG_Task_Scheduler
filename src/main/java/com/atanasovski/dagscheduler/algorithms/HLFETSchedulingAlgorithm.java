package com.atanasovski.dagscheduler.algorithms;

import com.atanasovski.dagscheduler.Executable;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Created by Blagoj on 05-Mar-16.
 */
public class HLFETSchedulingAlgorithm implements SchedulingAlgorithm {
    @Override
    public Executable choose(Executable... readyTasks) {
        return Arrays.stream(readyTasks)
                .max((t1, t2) -> Float.compare(t1.getExecutionWeight(), t2.getExecutionWeight())).get();
    }

    @Override
    public boolean usesPriority() {
        return true;
    }


    @Override
    public void calculatePriorities(final Schedule schedule) {
        final DirectedGraph<Executable, DefaultEdge> graph = schedule.getDependencies();
        Set<Executable> allVertices = graph.vertexSet();
        allVertices.stream()
                .filter(task -> graph.inDegreeOf(task) == 0)
                .forEach(task -> dfs(graph, task));
    }

    private int dfs(final DirectedGraph<Executable, DefaultEdge> graph, final Executable current) {
        if (current.hasExecutionWeight()) {
            return (int) current.getExecutionWeight();
        }

        if (graph.outDegreeOf(current) == 0) {
            current.setExecutionWeight(current.getExecutionTime());
            return (int) current.getExecutionWeight();
        } else {
            Set<DefaultEdge> edges = graph.outgoingEdgesOf(current);
            Stream<Executable> neighbours = edges.stream().map(graph::getEdgeTarget);
            int maxWeightOfNeighbours = neighbours.map(next -> dfs(graph, next)).max(Integer::compare).get();
            int weightOfCurrent = current.getExecutionTime() + maxWeightOfNeighbours;
            current.setExecutionWeight(weightOfCurrent);
            return weightOfCurrent;
        }
    }
}
