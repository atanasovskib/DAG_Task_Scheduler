package com.atanasovski.dagscheduler.schedule.validation;

import com.atanasovski.dagscheduler.annotations.TaskInput;
import com.atanasovski.dagscheduler.dependencies.ProcessedDependency;
import com.atanasovski.dagscheduler.schedule.InputDependencyException;
import com.atanasovski.dagscheduler.tasks.FieldExtractor;
import com.atanasovski.dagscheduler.tasks.Task;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class InputDependencyValidation {

    private final FieldExtractor fieldExtractor;

    public InputDependencyValidation(FieldExtractor fieldExtractor){
        this.fieldExtractor = Objects.requireNonNull(fieldExtractor);
    }

    public InputDependencyValidation(){
        this.fieldExtractor = new FieldExtractor();
    }
    public Consumer<Field> dependencyForEachInputGivenValidator(Map<String, ProcessedDependency> outputDependenciesByInputName) {
        return field -> {
            TaskInput inputDesc = field.getAnnotation(TaskInput.class);
            String inputParamName = inputDesc.inputName();
            if (!outputDependenciesByInputName.containsKey(inputParamName)) {
                throwError(inputParamName);
            }
        };
    }


    public Consumer<Field> typeCompatibilityValidator(Map<String, ProcessedDependency> dependenciesByInputArg) {
        return inputField -> {
            TaskInput inputDesc = inputField.getAnnotation(TaskInput.class);
            String paramName = inputDesc.inputName();

            ProcessedDependency dependency = dependenciesByInputArg.get(paramName);
            Class<? extends Task> outputTaskType = dependency.outputTaskType;
            String outputArgName = dependency.outputArg()
                                           .orElseThrow(this::expectedOutputDependency);

            Optional<Field> outputField = fieldExtractor.getOutputField(outputTaskType, outputArgName);
            outputField.map(compareTypes(inputField))
                    .orElseThrow(outputFieldDoesNotExist(outputTaskType, outputArgName));
        };
    }


    private InputDependencyException expectedOutputDependency() {
        return new InputDependencyException("Dependency provided for validation is not an output dependency");
    }



    public void validateFieldAccessibility(Field field) {
        final int fieldModifiers = field.getModifiers();
        if (Modifier.isPublic(fieldModifiers)) {
            return;
        }

        if (!Modifier.isFinal(fieldModifiers)) {
            return;
        }

        throw new InputDependencyException("Field annotated with @TaskInput or @TaskOutput must be public and not final");
    }

    private void throwError(String missingParameterName) {
        String errorMessage = String.format(
                "There was no input dependency specified for the parameter [%s]",
                missingParameterName);
        throw new InputDependencyException(errorMessage);
    }


    private Supplier<InputDependencyException> outputFieldDoesNotExist(Class<? extends Task> outputTaskType, String outputArgName) {
        return () -> {
            String errorMessage = String.format(
                    "Task of type [%s], doesn't have a field annotated with @TaskOutput and name of [%s]",
                    outputTaskType.getName(),
                    outputArgName);
            return new InputDependencyException(errorMessage);
        };
    }

    private Function<Field, Field> compareTypes(Field inputField) {
        return field -> {
            boolean isAssignable = inputField.getType().isAssignableFrom(field.getType());
            if (!isAssignable) {
                throw new InputDependencyException(inputAndOutputIncompatible(inputField.getType(), field.getType()));
            }

            return field;
        };
    }


    private String inputAndOutputIncompatible(Class<?> inputType, Class<?> outputType) {
        return String.format(
                "Input argument type [%s] can't accept the value of the output argument of type [%s]",
                inputType,
                outputType);
    }
}
