package dev.atanasovski.dagscheduler.algorithms;

import dev.atanasovski.dagscheduler.Executable;
import dev.atanasovski.dagscheduler.Schedule;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Stream;

public class HLFETSchedulingAlgorithm implements SchedulingAlgorithm {
    @Override
    public Executable choose(Executable... readyTasks) {
        return Arrays.stream(readyTasks)
                .max(Comparator.comparing(Executable::getExecutionWeight))
                .orElse(readyTasks[0]);
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
            int maxWeightOfNeighbours = neighbours.map(next -> dfs(graph, next)).max(Integer::compare).orElse(0);
            int weightOfCurrent = current.getExecutionTime() + maxWeightOfNeighbours;
            current.setExecutionWeight(weightOfCurrent);
            return weightOfCurrent;
        }
    }
}
