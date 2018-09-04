package com.atanasovski.dagscheduler.algorithms;

import com.atanasovski.dagscheduler.tasks.Task;

import java.util.List;

public class DummySchedulingAlgorithm implements SchedulingAlgorithm {
    @Override
    public List<Task> orderByPriority(List<Task> readyTasks) {
        return readyTasks;
    }
}
