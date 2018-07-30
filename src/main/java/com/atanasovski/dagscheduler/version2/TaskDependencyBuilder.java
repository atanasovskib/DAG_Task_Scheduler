package com.atanasovski.dagscheduler.version2;

public class TaskDependencyBuilder {
    private final String depender;
    private final ScheduleBuilder scheduleBuilder;

    public TaskDependencyBuilder(ScheduleBuilderImpl scheduleBuilderImpl, String depender) {
        this.scheduleBuilder = scheduleBuilderImpl;
        this.depender = depender;
    }

    public ScheduleBuilder dependsOn(String taskId) {
        if (!scheduleBuilder.hasTask(taskId)) {
            throw new IllegalArgumentException("Task with ID " + taskId + " not found");
        }

        if (scheduleBuilder.hasDependency(taskId, depender)) {
            throw new IllegalArgumentException("Circular dependency detected between " + depender + " and " + taskId);
        }

        return scheduleBuilder.addDependency(depender, taskId);
    }
}

