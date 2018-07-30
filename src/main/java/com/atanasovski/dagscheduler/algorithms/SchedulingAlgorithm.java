package com.atanasovski.dagscheduler.algorithms;

import com.atanasovski.dagscheduler.Executable;
import com.atanasovski.dagscheduler.Schedule;

public interface SchedulingAlgorithm {
    Executable choose(Executable... readyTasks);

    boolean usesPriority();

    void calculatePriorities(Schedule schedule);
}
