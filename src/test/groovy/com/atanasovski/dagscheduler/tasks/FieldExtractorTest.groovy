package com.atanasovski.dagscheduler.tasks

import com.atanasovski.dagscheduler.annotations.TaskInput
import com.atanasovski.dagscheduler.annotations.TaskOutput
import spock.lang.Specification
import spock.lang.Unroll

class FieldExtractorTest extends Specification {
    FieldExtractor extractor

    def setup() {
        extractor = new FieldExtractor()
    }

    @Unroll
    def "get field annotated as input named #inputArg should be of type #fieldType and have a name #fieldName"() {
        when:
        def field = extractor.getInputField(A.class, inputArg)

        then:
        field.isPresent()
        field.get().type == fieldType
        field.get().getName() == fieldName
        field.get().getAnnotation(TaskInput.class).inputName() == inputArg

        where:
        inputArg | fieldType   | fieldName
        "a"      | int.class   | "a"
        "b"      | float.class | "b1"
    }

    def "private fields can not be extracted even if annotated"() {
        when:
        def field = extractor.getInputField(A.class, "c")

        then:
        !field.isPresent()

    }

    def "wrongfully named input and output fields should not exist"() {
        when:
        def field = extractor.getInputField(A.class, "wrong")
        def field2 = extractor.getOutputField(A.class , "wrong")

        then:
        !field.isPresent()
        !field2.isPresent()
    }

    def "get field annotated as output should be of type String and named d"(){
        def outputArgName = "d_arg"
        when:
        def field = extractor.getOutputField(A.class, outputArgName)

        then:
        field.isPresent()
        field.get().type == String.class
        field.get().getName() == "d"
        field.get().getAnnotation(TaskOutput.class).outputName() == outputArgName
    }

    abstract class A extends Task {
        @TaskInput(inputName = "a")
        public int a

        @TaskInput(inputName = "b")
        public float b1

        @TaskInput(inputName = "c")
        private float c

        @TaskOutput(outputName = "d_arg")
        public String d

        public long e

        A(String taskId) {
            super(taskId)
        }
    }
}
