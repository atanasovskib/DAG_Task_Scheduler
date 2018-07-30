package com.atanasovski.dagscheduler.algorithms;

import com.atanasovski.dagscheduler.Executable;
import com.atanasovski.dagscheduler.Schedule;

public class DummySchedulingAlgorithm implements SchedulingAlgorithm {
    @Override
    public Executable choose(Executable... readyTasks) {
        if (readyTasks.length == 0) {
            throw new IllegalArgumentException("Pass at least one task");
        }

        return readyTasks[0];
    }

    @Override
    public boolean usesPriority() {
        return false;
    }

    @Override
    public void calculatePriorities(Schedule schedule) {
        return;
    }
}
