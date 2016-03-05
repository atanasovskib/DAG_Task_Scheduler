package com.atanasovski.dagscheduler.algorithms;

import com.atanasovski.dagscheduler.Executable;
import com.atanasovski.dagscheduler.Schedule;
import org.jgrapht.graph.DefaultDirectedGraph;
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
                .max((t1, t2) -> Float.compare(t1.getWeight(), t2.getWeight())).get();
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
                .forEach(task -> dfs(graph, task));
        allVertices.forEach(exe -> System.out.println(String.format("%s:%f", exe.getId(), exe.getExecutionWeight())));
    }

    private float dfs(final DefaultDirectedGraph<Executable, DefaultEdge> graph, final Executable current) {
        System.out.println("dfs for: " + current.getId());
        if (current.hasExecutionWeight()) {
            System.out.println(current.getId() + " has weight: " + current.getExecutionWeight());
            return current.getExecutionWeight();
        }

        if (graph.outDegreeOf(current) == 0) {
            System.out.println("current is leaf, weight: " + current.getExecutionTime());
            current.setExecutionWeight(current.getExecutionTime());
            return current.getExecutionWeight();
        } else {
            Set<DefaultEdge> edges = graph.outgoingEdgesOf(current);
            Stream<Executable> neighbours = edges.stream().map(edge -> graph.getEdgeTarget(edge));
            float maxWeightOfNeighbours = neighbours.map(next -> dfs(graph, next)).max(Float::compare).get();
            float weightOfCurrent = current.getExecutionTime() + maxWeightOfNeighbours;
            System.out.println("current is not leaf, weight: " + weightOfCurrent);
            current.setExecutionWeight(weightOfCurrent);
            return weightOfCurrent;
        }
    }
}
