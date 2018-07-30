package com.atanasovski.dagscheduler.version2;

import java.util.Map;

public interface ScheduleBuilder {
    ScheduleBuilder addDependencies(Map<String, String> dependencies);

    TaskDependencyBuilder and(String taskId);

    TaskDependencyBuilder where(String taskId);

    boolean hasTask(String taskId);

    boolean hasDependency(String dependent, String dependedOn);

    ScheduleBuilder addDependency(String dependent, String dependedOn);
}
