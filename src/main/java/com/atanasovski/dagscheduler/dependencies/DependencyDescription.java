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
        if (type == DependencyType.ON_COMPLETION && (inputArg != null || outputArg != null)) {
            throw new IllegalArgumentException("If task depends on completion of another, then it should not have an" +
                                                       "input and output arg assigned");
        }

        if (type == DependencyType.ON_OUTPUT && (inputArg == null || outputArg == null)) {
            throw new IllegalArgumentException("If task depends on the output of another then the input and output arg" +
                                                       "should never be null");
        }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DependencyDescription that = (DependencyDescription) o;
        return Objects.equals(outputTaskId, that.outputTaskId) &&
                       type == that.type &&
                       Objects.equals(inputArg, that.inputArg) &&
                       Objects.equals(outputArg, that.outputArg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(outputTaskId, type, inputArg, outputArg);
    }
}
