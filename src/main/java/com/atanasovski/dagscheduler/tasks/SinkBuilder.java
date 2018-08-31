package com.atanasovski.dagscheduler.tasks;

import com.atanasovski.dagscheduler.dependencies.DependencyDescription;
import com.atanasovski.dagscheduler.dependencies.DependencyType;

import java.util.Arrays;
import java.util.Objects;

public class SinkBuilder<Result> {
    private final Class<Result> resultType;

    private SinkBuilder(Class<Result> resultType) {
        this.resultType = resultType;
    }

    public static <Result> SinkBuilder<Result> theProductionOf(Class<Result> resultType) {
        return new SinkBuilder<>(Objects.requireNonNull(resultType));
    }

    public static <Result> SinkBuilder<Result> produceA(Class<Result> resultType) {
        return theProductionOf(resultType);
    }

    public <SinkType extends Sink<Result>> SinkDependencyBuilder<SinkType> with(Class<SinkType> producer) {
        return new SinkDependencyBuilder<>(resultType, Objects.requireNonNull(producer));
    }

    public class SinkDependencyBuilder<SinkType extends Sink<Result>> {
        private final Class<Result> resultType;
        private final Class<SinkType> producerType;

        public SinkDependencyBuilder(Class<Result> resultType, Class<SinkType> producerType) {
            this.resultType = resultType;
            this.producerType = producerType;
        }


        public SinkDefinition<Result, SinkType> using(DependencyDescription... dependencyDescriptions) {
            boolean invalidDependenciesGiven = Arrays.stream(Objects.requireNonNull(dependencyDescriptions))
                                                       .anyMatch(x -> x.type != DependencyType.ON_OUTPUT);
            if (invalidDependenciesGiven) {
                throw new IllegalArgumentException("Producer tasks can only take output type dependencies");
            }

            return new SinkDefinition<>(
                    producerType,
                    resultType,
                    Arrays.asList(dependencyDescriptions));
        }
    }
}

