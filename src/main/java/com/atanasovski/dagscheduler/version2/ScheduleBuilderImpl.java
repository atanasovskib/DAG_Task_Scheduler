package com.atanasovski.dagscheduler.version2;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

public class ScheduleBuilderImpl implements ScheduleBuilder {
    private final Map<String, TaskTemplate<? extends Task>> taskTemplatesByTaskId;
    private final Map<String, Set<String>> taskDependencies;

    public ScheduleBuilderImpl(TaskTemplate<? extends Task>[] tasks) {
        if (tasks == null) {
            throw new IllegalArgumentException("schedule must have tasks");
        }

        if (tasks.length == 0) {
            throw new IllegalArgumentException("schedule must have tasks");
        }

        long numDistinctIds = Arrays.stream(tasks)
                .map(TaskTemplate::taskId)
                .distinct()
                .count();
        if (numDistinctIds != tasks.length) {
            throw new IllegalArgumentException("Task ids should be unique");
        }

        Map<String, TaskTemplate<? extends Task>> byTaskId = new HashMap<>();
        Arrays.stream(tasks).forEach(task -> byTaskId.put(task.taskId, task));
        taskTemplatesByTaskId = Collections.unmodifiableMap(byTaskId);
        taskDependencies = new HashMap<>();
    }

    public ScheduleBuilder addDependencies(Map<String, String> dependencies) {
        BiPredicate<String, String> anyTaskExists = (x, y) -> !hasTask(x) || !hasTask(y);
        boolean unknownTaskid = dependencies.entrySet()
                .stream()
                .anyMatch(x -> anyTaskExists.test(x.getKey(), x.getValue()));
        if (unknownTaskid) {
            throw new IllegalArgumentException("Some of the task ids are unknown");
        }

        dependencies.forEach(this::addDependency);
        return this;
    }

    public TaskDependencyBuilder and(String taskId) {
        return where(taskId);
    }


    public TaskDependencyBuilder where(String taskId) {
        if (!taskTemplatesByTaskId.containsKey(taskId)) {
            throw new IllegalArgumentException("Task " + taskId + " not found");
        }

        return new TaskDependencyBuilder(this, taskId);
    }

    public boolean hasTask(String taskId) {
        return taskTemplatesByTaskId.containsKey(taskId);
    }

    public boolean hasDependency(String fromTask, String toTask) {
        Supplier<Boolean> from = () -> taskDependencies.containsKey(fromTask);
        Supplier<Set<String>> depsOfFrom = () -> taskDependencies.getOrDefault(fromTask, Collections.emptySet());
        Supplier<Boolean> to = () -> depsOfFrom.get().contains(toTask);
        return from.get() && to.get();
    }

    public ScheduleBuilder addDependency(String fromTask, String toTask) {
        taskDependencies
                .computeIfAbsent(fromTask, reqTaskId -> new HashSet<>())
                .add(toTask);
        return this;
    }
}
