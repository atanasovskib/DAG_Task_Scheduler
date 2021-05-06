package dev.atanasovski.dagscheduler.algorithms;

import dev.atanasovski.dagscheduler.Executable;
import dev.atanasovski.dagscheduler.Schedule;

public interface SchedulingAlgorithm {
    Executable choose(Executable... readyTasks);

    default boolean usesPriority() {
        return false;
    }

    default void calculatePriorities(Schedule schedule){
        // no priorities calculated
    }
}
