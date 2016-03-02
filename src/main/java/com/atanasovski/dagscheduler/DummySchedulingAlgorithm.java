package com.atanasovski.dagscheduler;

/**
 * Created by Blagoj on 02-Mar-16.
 */
public class DummySchedulingAlgorithm implements SchedulingAlgorithm {
    @Override
    public Executable choose(Executable... readyTasks) {
        if (readyTasks.length == 0) {
            throw new IllegalArgumentException("Pass at least one task");
        }

        return readyTasks[0];
    }
}
