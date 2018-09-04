package com.atanasovski.dagscheduler.algorithms;

import com.atanasovski.dagscheduler.tasks.Task;

import java.util.List;

public interface SchedulingAlgorithm {
    List<Task> orderByPriority(List<Task> readyTasks);
}
