package com.atanasovski.dagscheduler.schedule;

import com.atanasovski.dagscheduler.ValidationResult;
import com.atanasovski.dagscheduler.tasks.Task;
import com.atanasovski.dagscheduler.annotations.TaskInput;
import com.atanasovski.dagscheduler.annotations.TaskOutput;
import com.atanasovski.dagscheduler.dependencies.DependencyType;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DependencyValidator {
    public <T extends Task> void validate(Class<T> taskType, List<ProcessedDependency> dependencies) {
        Map<String, ProcessedDependency> outputDependencies;
        outputDependencies = extractOutputDependencies(dependencies)
                                     .orElseThrow(() -> {
                                         String errorMessage = "There are multiple output dependencies specified " +
                                                                       "for the same input argument";
                                         return new InputDependencyException(errorMessage);
                                     });


        inputFields(taskType)
                .forEach(this.validateByName(outputDependencies));

        dependencies.forEach(this::validateOutputParamNames);

        inputFields(taskType).stream()
                .forEach(this.validateByType(outputDependencies));
    }

    private List<Field> inputFields(Class taskType) {
        Field[] taskFields = taskType.getFields();
        return Arrays.stream(taskFields)
                       .filter(x -> x.getAnnotation(TaskInput.class) != null)
                       .collect(Collectors.toList());
    }

    private void validateOutputParamNames(ProcessedDependency dependency) {
        String outputArg = dependency.dependency.outputArg().get();
        Class outputType = dependency.outputTaskType;

        Predicate<Field> fieldIsRequiredOutputParam;
        fieldIsRequiredOutputParam = field -> Optional.ofNullable(field.getAnnotation(TaskOutput.class))
                                                      .map(annot -> annot.outputName().equals(outputArg))
                                                      .orElse(false);

        Arrays.stream(outputType.getFields())
                .filter(fieldIsRequiredOutputParam)
                .findAny()
                .orElseThrow(() -> {
                    String validationError = String.format(
                            "In class [%s] no field was marked as TaskOutput with name [%s]",
                            dependency.outputTaskType.getName(),
                            outputArg);
                    return new InputDependencyException(validationError);
                });
    }

    private Consumer<Field> validateByType(Map<String, ProcessedDependency> dependenciesByInputArg) {
        return inputField -> {
            TaskInput inputDesc = inputField.getAnnotation(TaskInput.class);
            String paramName = inputDesc.paramName();

            ProcessedDependency dependency = dependenciesByInputArg.get(paramName);
            Class outputTaskType = dependency.outputTaskType;
            String outputArgName = dependency.dependency.outputArg().get();
            Optional<Field> outputField = outputParamField(outputTaskType, outputArgName);
            outputField.map(compareTypes(inputField))
                    .orElseThrow(outputFieldDoesNotExist(outputTaskType, outputArgName));
        };
    }

    private Supplier<InputDependencyException> outputFieldDoesNotExist(Class outputTaskType, String outputArgName) {
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

    private ValidationResult inputAndOutputIncompatible(Class inputType, Class outputType) {
        String errorMessage = String.format(
                "Input argument type [%s] can't accept the value of the output argument of type [%s]",
                inputType,
                outputType);

        return ValidationResult.error(errorMessage);
    }

    private Optional<Field> outputParamField(Class taskType, String outputArgName) {
        Predicate<Field> outArgNameIsRequested = field -> Optional.ofNullable(field.getAnnotation(TaskOutput.class))
                                                                  .map(annot -> annot.outputName().equals(outputArgName))
                                                                  .orElse(false);

        return Arrays.stream(taskType.getFields())
                       .filter(outArgNameIsRequested)
                       .findAny();
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

    private Optional<Map<String, ProcessedDependency>> extractOutputDependencies(List<ProcessedDependency> dependencies) {
        Map<String, List<ProcessedDependency>> x = dependencies.stream()
                                                           .filter(dep -> dep.type() == DependencyType.ON_OUTPUT)
                                                           .collect(Collectors.groupingBy(this::getInputArg));
        Map<String, ProcessedDependency> toReturn = new HashMap<>();
        for (String inputArg : x.keySet()) {
            List<ProcessedDependency> dependenciesForInputArg = x.get(inputArg);
            if (dependenciesForInputArg.size() != 1) {
                return Optional.empty();
            }

            toReturn.put(inputArg, dependenciesForInputArg.get(0));
        }

        return Optional.of(toReturn);
    }

    private String getInputArg(ProcessedDependency dependency) {
        return dependency.inputArg().get();
    }
}

