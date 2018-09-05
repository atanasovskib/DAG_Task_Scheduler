package com.atanasovski.dagscheduler.schedule.validation;

import com.atanasovski.dagscheduler.dependencies.DependencyType;
import com.atanasovski.dagscheduler.dependencies.ProcessedDependency;
import com.atanasovski.dagscheduler.schedule.InputDependencyException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DependencyExtractor {

    public Map<String, ProcessedDependency> outputDependenciesByInputName(List<ProcessedDependency> dependencies) {
        Map<String, List<ProcessedDependency>> outputDependenciesByInputName = dependencies.stream()
                                                           .filter(dep -> dep.type() == DependencyType.ON_OUTPUT)
                                                           .collect(Collectors.groupingBy(this::getInputArg));
        Map<String, ProcessedDependency> toReturn = new HashMap<>();
        for (String inputArg : outputDependenciesByInputName.keySet()) {
            List<ProcessedDependency> dependenciesForInputArg = outputDependenciesByInputName.get(inputArg);
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
