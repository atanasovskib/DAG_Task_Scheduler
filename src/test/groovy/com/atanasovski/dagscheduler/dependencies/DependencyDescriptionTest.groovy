package com.atanasovski.dagscheduler.dependencies

import spock.lang.Specification

class DependencyDescriptionTest extends Specification {
    def "TheCompletionOf"() {
        given:
        def outputTaskId = "output task id"
        when:
        def depDesc = DependencyDescription.theCompletionOf(outputTaskId)

        then:
        depDesc.type == DependencyType.ON_COMPLETION
        depDesc.outputTaskId == outputTaskId
        !depDesc.inputArg().isPresent()
        !depDesc.outputArg().isPresent()
    }

    def "TheOutput"() {
        given:
        def outputArgName = "Output Arg Name"
        when:
        def depBuilder = DependencyDescription.theOutput(outputArgName)

        then:
        depBuilder.outputArgName == outputArgName
    }

    def "constructor fails ON_OUTPUT, no input arg"(){
        when:
        new DependencyDescription(DependencyType.ON_OUTPUT, null, "outputTaskId", "outputArg")

        then:
        thrown(IllegalArgumentException)
    }

    def "constructor fails ON_OUTPUT, no output arg"(){
        when:
        new DependencyDescription(DependencyType.ON_OUTPUT, "input arg", "outputTaskId", null)

        then:
        thrown(IllegalArgumentException)
    }

    def "constructor fails ON_COMPLETION, args given"(){
        when:
        new DependencyDescription(DependencyType.ON_COMPLETION, "input arg", "outputTaskId", "outputArg")

        then:
        thrown(IllegalArgumentException)
    }
}
