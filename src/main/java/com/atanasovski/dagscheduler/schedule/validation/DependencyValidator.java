package com.atanasovski.dagscheduler.schedule.validation;

import com.atanasovski.dagscheduler.annotations.TaskInput;
import com.atanasovski.dagscheduler.dependencies.DependencyType;
import com.atanasovski.dagscheduler.dependencies.ProcessedDependency;
import com.atanasovski.dagscheduler.schedule.InputDependencyException;
import com.atanasovski.dagscheduler.tasks.FieldExtractor;
import com.atanasovski.dagscheduler.tasks.Task;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DependencyValidator {
    private final FieldExtractor fieldExtractor;
    private final InputDependencyValidation inputValidation;
    private final DependencyExtractor dependencyExtractor;

    public DependencyValidator(FieldExtractor fieldExtractor,
                               InputDependencyValidation inputDependencyValidation,
                               DependencyExtractor dependencyExtractor) {
        this.fieldExtractor = Objects.requireNonNull(fieldExtractor);
        this.inputValidation = Objects.requireNonNull(inputDependencyValidation);
        this.dependencyExtractor = Objects.requireNonNull(dependencyExtractor);
    }

    public DependencyValidator() {
        this.fieldExtractor = new FieldExtractor();
        this.inputValidation = new InputDependencyValidation();
        this.dependencyExtractor = new DependencyExtractor();
    }

    public <T extends Task> void validate(Class<T> taskType, List<ProcessedDependency> dependencies) {
        Map<String, ProcessedDependency> outputDependencies = this.dependencyExtractor.outputDependenciesByInputName(
                dependencies);

        dependencies.forEach(this::validateOutputParams);

        Consumer<Field> validateInputIsSatisfied = inputValidation.dependencyForEachInputGivenValidator(outputDependencies);
        Consumer<Field> validateTypeCompatibility = inputValidation.typeCompatibilityValidator(outputDependencies);
        Consumer<Field> validateFieldAccessability = inputValidation::validateFieldAccessibility;
        inputFields(taskType)
                .stream()
                .peek(validateInputIsSatisfied)
                .peek(validateTypeCompatibility)
                .forEach(validateFieldAccessability);

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

        this.inputValidation.validateFieldAccessibility(outputField);
    }

    private InputDependencyException expectedOutputDependency() {
        return new InputDependencyException("Dependency provided for validation is not an output dependency");
    }
}

