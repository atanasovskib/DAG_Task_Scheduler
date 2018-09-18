package com.atanasovski.dagscheduler.algorithms

import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DirectedAcyclicGraph
import spock.lang.Unroll

class MCPchedulingAlgorithmTest extends WeightedSchedulingAlgorithmTest {
    @Override
    WeightedSchedulingAlgorithm getAlgorithm(DirectedAcyclicGraph<String, DefaultEdge> dependencyGraph, Map<String, Integer> taskWeights) {
        return new MCPSchedulingAlgorithm(dependencyGraph, taskWeights)
    }

    @Unroll
    def "two branches, two ready tasks, same weights, one has a longer chain od dependencies"() {
        given:
        def graph = new DirectedAcyclicGraph<String, DefaultEdge>(DefaultEdge)
        graph.addVertex("a")
        graph.addVertex("b")
        graph.addVertex("c")

        graph.addEdge("b", "c")

        def map = new HashMap<String, Integer>()
        map.put("a", 1)
        map.put("b", 1)
        map.put("c", 1)

        def algo = getAlgorithm(graph, map)

        when:
        def result = algo.orderByPriority(createTasks(readyTasks))

        then:
        taskToId(result) == expectedOrder

        where:
        readyTasks             | expectedOrder
        ["b", "a"] as String[] | ["b", "a"] as List<String>
        [] as String[]         | [] as List<String>
        ["a", "b"] as String[] | ["b", "a"] as List<String>
    }

    @Unroll
    def "two branches, two ready tasks, different weights, both have same dependency chain"() {
        given:
        def graph = new DirectedAcyclicGraph<String, DefaultEdge>(DefaultEdge)
        graph.addVertex("a")
        graph.addVertex("b")
        graph.addVertex("c")

        graph.addEdge("a", "c")
        graph.addEdge("b", "c")

        def map = new HashMap<String, Integer>()
        map.put("a", 1)
        map.put("b", 2)
        map.put("c", 1)

        def algo = getAlgorithm(graph, map)

        when:
        def result = algo.orderByPriority(createTasks(readyTasks))

        then:
        taskToId(result) == expectedOrder

        where:
        readyTasks             | expectedOrder
        ["b", "a"] as String[] | ["b", "a"] as List<String>
        [] as String[]         | [] as List<String>
        ["a", "b"] as String[] | ["b", "a"] as List<String>
    }

    @Unroll
    def "three branches, three ready tasks, same weight, one of the tasks has a longer dependency chain"() {
        given:
        def graph = new DirectedAcyclicGraph<String, DefaultEdge>(DefaultEdge)
        graph.addVertex("a")
        graph.addVertex("b")
        graph.addVertex("c")

        graph.addEdge("a", "c")

        def map = new HashMap<String, Integer>()
        map.put("a", 1)
        map.put("b", 1)
        map.put("c", 1)

        def algo = getAlgorithm(graph, map)

        when:
        def result = algo.orderByPriority(createTasks(readyTasks))

        then:
        taskToId(result) == expectedOrder

        where:
        readyTasks                  | expectedOrder
        ["c", "b", "a"] as String[] | ["a", "c", "b"] as List<String>
        [] as String[]              | [] as List<String>
        ["c", "a", "b"] as String[] | ["a", "c", "b"] as List<String>
        ["b", "c", "a"] as String[] | ["a", "b", "c"] as List<String>
    }
}
