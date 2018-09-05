package com.atanasovski.dagscheduler.tasks

import com.atanasovski.dagscheduler.annotations.TaskInput
import com.atanasovski.dagscheduler.annotations.TaskOutput
import spock.lang.Specification
import spock.lang.Unroll

class FieldExtractorTest extends Specification {
    FieldExtractor extractor

    def setup() {
        extractor = new FieldExtractor();
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

    abstract class A extends Task {
        @TaskInput(inputName = "a")
        public int a

        @TaskInput(inputName = "b")
        public float b1

        @TaskInput(inputName = "c")
        private float c

        @TaskOutput(outputName = "d")
        public String d

        public long e

        A(String taskId) {
            super(taskId)
        }
    }
}
