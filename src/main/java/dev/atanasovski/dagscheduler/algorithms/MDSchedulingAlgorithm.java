package dev.atanasovski.dagscheduler.algorithms;

import dev.atanasovski.dagscheduler.Executable;
import dev.atanasovski.dagscheduler.Schedule;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

public class MDSchedulingAlgorithm implements SchedulingAlgorithm {
    final Logger logger = LoggerFactory.getLogger(MDSchedulingAlgorithm.class);
    private int minALAP = Integer.MAX_VALUE;
    private final Map<Executable, Integer> alapTimes = new HashMap<>();
    private final Map<Executable, Integer> asapTimes = new HashMap<>();
    private final Schedule schedule;

    public MDSchedulingAlgorithm(Schedule schedule) {
        Objects.requireNonNull(schedule);
        this.schedule = schedule;
    }

    @Override
    public Executable choose(Executable... readyTasks) {
        if (readyTasks.length == 0) {
            throw new IllegalArgumentException("must pass at least one ready task");
        }

        this.calculatePriorities1(this.schedule);
        logger.info("Choosing from: {}", Arrays.toString(readyTasks));
        Executable chosen = Arrays.stream(readyTasks).min((a, b) -> Float.compare(a.getExecutionWeight(), b.getExecutionWeight())).get();
        logger.info("Chosen: {}", chosen);
        return chosen;
    }

    public void calculatePriorities1(final Schedule schedule) {
        Objects.requireNonNull(schedule);
        reset();
        this.calculateALAP(schedule.getDependencies());
        logger.info("ALAP: {}", this.alapTimes);
        this.calculateASAP(schedule.getDependencies());
        logger.info("ASAP: {}", this.asapTimes);
        this.calculateMD(schedule.getDependencies());
    }

    private void reset() {
        this.alapTimes.clear();
        this.asapTimes.clear();
        this.minALAP = Integer.MAX_VALUE;
    }

    private void calculateMD(DirectedGraph<Executable, DefaultEdge> dependencies) {
        StringBuilder sb = new StringBuilder();
        dependencies.vertexSet().forEach(exe -> {
            float diff = this.alapTimes.get(exe) - this.asapTimes.get(exe);
            exe.setExecutionWeight(diff / exe.getExecutionTime());
            sb.append(exe.getId()).append(':').append(exe.getExecutionWeight()).append(", ");
        });

        logger.info("MD: {}", sb);
    }

    private void calculateALAP(final DirectedGraph<Executable, DefaultEdge> graph) {
        Set<Executable> allVertices = graph.vertexSet();
        allVertices.stream()
                .filter(task -> graph.inDegreeOf(task) == 0)
                .forEach(task -> alapFromOneNode(graph, task));
        int executionTime = -this.minALAP;
        allVertices.forEach(exe -> this.alapTimes.put(exe, executionTime + this.alapTimes.get(exe)));
    }

    private int alapFromOneNode(final DirectedGraph<Executable, DefaultEdge> graph, final Executable current) {
        if (this.alapTimes.containsKey(current)) {
            return this.alapTimes.get(current);
        }

        if (graph.outDegreeOf(current) == 0) {
            int tmp = -current.getExecutionTime();
            this.alapTimes.put(current, tmp);
            this.minALAP = Math.min(tmp, this.minALAP);
            return tmp;
        } else {
            Set<DefaultEdge> edges = graph.outgoingEdgesOf(current);
            Stream<Executable> neighbours = edges.stream().map(graph::getEdgeTarget);
            int maxStartTimeOfNeighbours = neighbours.map(next -> alapFromOneNode(graph, next)).min(Integer::compare).orElse(0);
            int maxStartTimeOfCurrent = -current.getExecutionTime() + maxStartTimeOfNeighbours;
            this.minALAP = Math.min(maxStartTimeOfCurrent, this.minALAP);
            this.alapTimes.put(current, maxStartTimeOfCurrent);
            return maxStartTimeOfCurrent;
        }

    }

    private void calculateASAP(final DirectedGraph<Executable, DefaultEdge> graph) {
        Set<Executable> allVertices = graph.vertexSet();
        allVertices.stream()
                .filter(task -> graph.inDegreeOf(task) == 0)
                .forEach(task -> calculateASAPFromOneNode(graph, task, 0));
    }

    private void calculateASAPFromOneNode(final DirectedGraph<Executable, DefaultEdge> graph, Executable current, int startTime) {
        if (this.asapTimes.containsKey(current)) {
            int min = Math.max(this.asapTimes.get(current), startTime);
            this.asapTimes.put(current, min);
        } else {
            this.asapTimes.put(current, startTime);
        }

        if (graph.outDegreeOf(current) == 0) {
            return;
        }

        Set<DefaultEdge> edges = graph.outgoingEdgesOf(current);
        Stream<Executable> neighbours = edges.stream().map(graph::getEdgeTarget);
        neighbours.forEach(neighbour -> calculateASAPFromOneNode(graph, neighbour, startTime + current.getExecutionTime()));
    }
}
