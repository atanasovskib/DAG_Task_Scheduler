package dev.atanasovski.dagscheduler.algorithms;

import dev.atanasovski.dagscheduler.Executable;
import dev.atanasovski.dagscheduler.Schedule;

public class DummySchedulingAlgorithm implements SchedulingAlgorithm {
    @Override
    public Executable choose(Executable... readyTasks) {
        if (readyTasks.length == 0) {
            throw new IllegalArgumentException("Pass at least one task");
        }

        return readyTasks[0];
    }
}
