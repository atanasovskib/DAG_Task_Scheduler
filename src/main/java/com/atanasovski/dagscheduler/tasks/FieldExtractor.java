package com.atanasovski.dagscheduler.tasks;

import com.atanasovski.dagscheduler.annotations.TaskInput;
import com.atanasovski.dagscheduler.annotations.TaskOutput;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

public class FieldExtractor {
    public Optional<Field> getInputField(Class<? extends Task> taskClass, String inputArgName) {
        return Arrays.stream(taskClass.getFields())
                       .filter(x -> x.getAnnotation(TaskInput.class) != null)
                       .filter(x -> x.getAnnotation(TaskInput.class).inputName().equals(inputArgName))
                       .findAny();
    }

    public Optional<Field> getOutputField(Class<? extends Task> taskClass, String outputArgName) {
        return Arrays.stream(taskClass.getFields())
                       .filter(x -> x.getAnnotation(TaskOutput.class) != null)
                       .filter(x -> x.getAnnotation(TaskOutput.class).outputName().equals(outputArgName))
                       .findAny();
    }
}
