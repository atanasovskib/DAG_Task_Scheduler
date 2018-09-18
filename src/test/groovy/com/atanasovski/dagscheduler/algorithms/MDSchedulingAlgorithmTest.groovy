package com.atanasovski.dagscheduler.algorithms

import com.atanasovski.dagscheduler.tasks.Task
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DirectedAcyclicGraph
import spock.lang.Specification
import spock.lang.Unroll

class MDSchedulingAlgorithmTest extends Specification {

    WeightedSchedulingAlgorithm getAlgorithm(DirectedAcyclicGraph<String, DefaultEdge> dependencyGraph, Map<String, Integer> taskWeights) {
        return new MDSchedulingAlgorithm(dependencyGraph, taskWeights)
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
        readyTasks             | expectedOrder
        ["b", "a"] as String[] | ["a", "b"] as List<String>
        [] as String[]         | [] as List<String>
        ["a", "b"] as String[] | ["a", "b"] as List<String>
    }


    def "empty dependency graph and task weights are accepted"() {
        given:
        DirectedAcyclicGraph<String, DefaultEdge> dependencyGraph = new DirectedAcyclicGraph<>(DefaultEdge.class)

        when:
        getAlgorithm(dependencyGraph, Collections.emptyMap())

        then:
        noExceptionThrown()
    }

    @Unroll
    def "number of tasks with weights and vertices in dependency graph is not equal == problem"() {
        when:
        getAlgorithm(dependencyGraph, taskWeights)

        then:
        thrown(IllegalArgumentException)

        where:
        dependencyGraph                               | taskWeights
        new DirectedAcyclicGraph<>(DefaultEdge.class) | Collections.singletonMap("task1", 1)
        graphWithOneTask()                            | Collections.emptyMap()
        graphWithTwoTasks()                           | Collections.singletonMap("task1", 1)
    }

    private static DirectedAcyclicGraph<String, DefaultEdge> graphWithOneTask() {
        DirectedAcyclicGraph<String, DefaultEdge> graph = new DirectedAcyclicGraph<>(DefaultEdge.class)
        graph.addVertex("task1")
        return graph
    }

    private static DirectedAcyclicGraph<String, DefaultEdge> graphWithTwoTasks() {
        DirectedAcyclicGraph<String, DefaultEdge> graph = graphWithOneTask()
        graph.addVertex("task2")
        graph.addEdge("task1", "task2")
        return graph
    }

    def "vertices of dependency graph must be the same as keys/taskIds in taskWeights map"() {
        given:
        def dependencyGraph = graphWithOneTask()
        def taskWeights = Collections.singletonMap("wrongTaskId", 1)

        when:
        getAlgorithm(dependencyGraph, taskWeights)

        then:
        thrown(IllegalArgumentException)
    }


    @Unroll
    def "no branching, straight line of dependent tasks a-> b -> c"() {
        given:
        def graph = new DirectedAcyclicGraph<String, DefaultEdge>(DefaultEdge)
        graph.addVertex("a")
        graph.addVertex("b")
        graph.addVertex("c")

        graph.addEdge("a", "b")
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
        readyTasks                  | expectedOrder
        ["b", "a"] as String[]      | ["b", "a"] as List<String>
        [] as String[]              | [] as List<String>
        ["b", "c", "a"] as String[] | ["b", "c", "a"] as List<String>
    }

    def "no dependencies between tasks, order depends on task weight"() {
        given:
        def graph = new DirectedAcyclicGraph<String, DefaultEdge>(DefaultEdge)
        graph.addVertex("a")
        graph.addVertex("b")
        graph.addVertex("c")

        def map = new HashMap<String, Integer>()
        map.put("a", 3)
        map.put("b", 2)
        map.put("c", 1)

        def algo = getAlgorithm(graph, map)

        when:
        def result = algo.orderByPriority(createTasks(readyTasks))

        then:
        taskToId(result) == expectedOrder

        where:
        readyTasks                  | expectedOrder
        ["b", "a"] as String[]      | ["a", "b"] as List<String>
        [] as String[]              | [] as List<String>
        ["b", "c", "a"] as String[] | ["a", "b", "c"] as List<String>
        ["c", "b"] as String[]      | ["b", "c"] as List<String>
    }

    def "no dependencies between tasks, same weight order depends on input order"() {
        given:
        def graph = new DirectedAcyclicGraph<String, DefaultEdge>(DefaultEdge)
        graph.addVertex("a")
        graph.addVertex("b")
        graph.addVertex("c")

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
        ["b", "a"] as String[]      | ["b", "a"] as List<String>
        [] as String[]              | [] as List<String>
        ["b", "c", "a"] as String[] | ["b", "c", "a"] as List<String>
    }

    List<String> taskToId(List<Task> tasks) {
        List<String> toReturn = new LinkedList<>()
        for (Task task : tasks) {
            toReturn.add(task.taskId)
        }

        return toReturn
    }

    List<MockTask> createTasks(String[] taskIds) {
        List<MockTask> toReturn = new LinkedList<>()
        for (String task : taskIds) {
            toReturn.add(new MockTask(task))
        }

        return toReturn
    }
}
