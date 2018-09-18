package com.atanasovski.dagscheduler.dependencies

import spock.lang.Specification

class TaskDependencyInputBuilderTest extends Specification {
    def "AsInput"() {
        given:
        def outputTaskId = "task id"
        def outputArg = "arg"
        def inputArg = "input arg"
        def inputBuilder = new TaskDependencyInputBuilder(outputTaskId, outputArg)

        when:
        def depDesc = inputBuilder.asInput(inputArg)

        then:
        depDesc.type == DependencyType.ON_OUTPUT
        depDesc.outputTaskId == outputTaskId
        depDesc.inputArg().get() == inputArg
        depDesc.outputArg().get() == outputArg
    }
}
