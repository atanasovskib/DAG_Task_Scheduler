package com.atanasovski.dagscheduler.dependencies;

import java.util.Objects;
import java.util.Optional;

public class DependencyDescription {
    public final String outputTaskId;
    public final DependencyType type;
    private final String inputArg;
    private final String outputArg;

    public DependencyDescription(DependencyType type, String inputArg, String outputTaskId, String outputArg) {
        this.type = Objects.requireNonNull(type);
        this.outputTaskId = Objects.requireNonNull(outputTaskId);
        this.inputArg = inputArg;
        this.outputArg = outputArg;
    }

    public static DependencyDescription theCompletionOf(String outputTaskId) {
        return new DependencyDescription(DependencyType.ON_COMPLETION, null, outputTaskId, null);
    }


    public static TaskDependencyBuilder theOutput(String outputArgName) {
        return new TaskDependencyBuilder(outputArgName);
    }

    public Optional<String> inputArg() {
        return Optional.ofNullable(this.inputArg);
    }

    public Optional<String> outputArg() {
        return Optional.ofNullable(this.outputArg);
    }
}
