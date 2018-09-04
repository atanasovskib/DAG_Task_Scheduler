package com.atanasovski.dagscheduler.schedule;

import com.atanasovski.dagscheduler.ValidationResult;
import com.atanasovski.dagscheduler.annotations.TaskInput;
import com.atanasovski.dagscheduler.dependencies.DependencyType;
import com.atanasovski.dagscheduler.tasks.FieldExtractor;
import com.atanasovski.dagscheduler.tasks.Task;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DependencyValidator {
    private final FieldExtractor fieldExtractor;

    public DependencyValidator(FieldExtractor fieldExtractor) {
        this.fieldExtractor = Objects.requireNonNull(fieldExtractor);
    }

    public DependencyValidator() {
        this.fieldExtractor = new FieldExtractor();
    }

    public <T extends Task> void validate(Class<T> taskType, List<ProcessedDependency> dependencies) {
        Map<String, ProcessedDependency> outputDependencies = extractOutputDependencies(dependencies);


        inputFields(taskType)
                .stream()
                .peek(this.validateByName(outputDependencies))
                .peek(this.validateByType(outputDependencies))
                .forEach(this::validateAccessibility);

        dependencies.forEach(this::validateOutputParams);
    }

    private void validateAccessibility(Field field) {
        final int fieldModifiers = field.getModifiers();
        if (Modifier.isPublic(fieldModifiers)) {
            return;
        }

        if (!Modifier.isFinal(fieldModifiers)) {
            return;
        }

        throw new InputDependencyException("Field annotated with @TaskInput or @TaskOutput must be public and not final");
    }

    private List<Field> inputFields(Class<? extends Task> taskType) {
        Field[] taskFields = taskType.getFields();
        return Arrays.stream(taskFields)
                       .filter(x -> x.getAnnotation(TaskInput.class) != null)
                       .collect(Collectors.toList());
    }

    private void validateOutputParams(ProcessedDependency dependency) {
        if (dependency.type() != DependencyType.ON_OUTPUT) {
            return;
        }

        Class<? extends Task> outputType = dependency.outputTaskType;

        String outputArg = dependency.outputArg()
                                   .orElseThrow(this::expectedOutputDependency);

        Field outputField = fieldExtractor.getOutputField(outputType, outputArg)
                                    .orElseThrow(() -> {
                                        String validationError = String.format(
                                                "In class [%s] no field was marked as TaskOutput named [%s]",
                                                dependency.outputTaskType.getName(),
                                                outputArg);
                                        return new InputDependencyException(validationError);
                                    });

        this.validateAccessibility(outputField);
    }

    private Consumer<Field> validateByType(Map<String, ProcessedDependency> dependenciesByInputArg) {
        return inputField -> {
            TaskInput inputDesc = inputField.getAnnotation(TaskInput.class);
            String paramName = inputDesc.paramName();

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
        return new InputDependencyException("Expected output dependency");
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

    private Function<Field, ValidationResult> compareTypes(Field inputField) {
        return field -> {
            boolean isAssignable = inputField.getType().isAssignableFrom(field.getType());
            return isAssignable
                           ? ValidationResult.ok()
                           : inputAndOutputIncompatible(inputField.getType(), field.getType());
        };
    }

    private ValidationResult inputAndOutputIncompatible(Class<?> inputType, Class<?> outputType) {
        String errorMessage = String.format(
                "Input argument type [%s] can't accept the value of the output argument of type [%s]",
                inputType,
                outputType);

        return ValidationResult.error(errorMessage);
    }

    private Consumer<Field> validateByName(Map<String, ProcessedDependency> dependencies) {
        return field -> {
            TaskInput inputDesc = field.getAnnotation(TaskInput.class);
            String paramName = inputDesc.paramName();
            if (!dependencies.containsKey(paramName)) {
                String errorMessage = String.format(
                        "There was no input dependency specified for the parameter [%s]",
                        paramName);
                throw new InputDependencyException(errorMessage);
            }
        };
    }

    private Map<String, ProcessedDependency> extractOutputDependencies(List<ProcessedDependency> dependencies) {
        Map<String, List<ProcessedDependency>> x = dependencies.stream()
                                                           .filter(dep -> dep.type() == DependencyType.ON_OUTPUT)
                                                           .collect(Collectors.groupingBy(this::getInputArg));
        Map<String, ProcessedDependency> toReturn = new HashMap<>();
        for (String inputArg : x.keySet()) {
            List<ProcessedDependency> dependenciesForInputArg = x.get(inputArg);
            if (dependenciesForInputArg.size() != 1) {
                String errorMessage = "There are multiple output dependencies specified " +
                                              "for the same input argument";
                throw new InputDependencyException(errorMessage);
            }

            toReturn.put(inputArg, dependenciesForInputArg.get(0));
        }

        return toReturn;
    }

    private String getInputArg(ProcessedDependency dependency) {
        return dependency.inputArg().get();
    }
}

