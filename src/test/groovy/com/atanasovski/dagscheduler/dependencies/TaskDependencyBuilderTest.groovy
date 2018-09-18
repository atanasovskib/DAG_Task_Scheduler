package com.atanasovski.dagscheduler.dependencies

import spock.lang.Specification

class TaskDependencyBuilderTest extends Specification {
    def "OfTask"() {
        given:
        def taskId = "taskId"
        def outputArgName = "output arg"
        def depBuilder = new TaskDependencyBuilder(outputArgName)
        when:
        def inputBuilder = depBuilder.ofTask(taskId)

        then:
        inputBuilder.outputArg == outputArgName
        inputBuilder.outputTaskId == taskId
    }
}
