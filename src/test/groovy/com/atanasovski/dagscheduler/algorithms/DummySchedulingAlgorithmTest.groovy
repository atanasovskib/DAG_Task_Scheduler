package com.atanasovski.dagscheduler.algorithms

import com.atanasovski.dagscheduler.tasks.Task
import spock.lang.Specification

class DummySchedulingAlgorithmTest extends Specification {
    def "OrderByPriority creates new list"() {
        given:
        def schedulingAlgorithm = new DummySchedulingAlgorithm()

        def inputList = [Mock(Task)]
        def inputCopy = new LinkedList(inputList)
        when:
        def result = schedulingAlgorithm.orderByPriority(inputList)
        inputList.clear()

        then:
        result == inputCopy
        result != inputList
    }

    def "empty list input returns empty list output"(){
        when:
        def result = new DummySchedulingAlgorithm().orderByPriority(Collections.emptyList())

        then:
        result.isEmpty()
    }

    def "order remains always the same"(){
        given:
        def schedulingAlgorithm = new DummySchedulingAlgorithm()

        def inputList = [Mock(Task), Mock(Task)]
        def inputCopy = new LinkedList(inputList)
        when:
        def result = schedulingAlgorithm.orderByPriority(inputList)
        inputList.clear()

        then:
        result == inputCopy
        result != inputList
    }
}

