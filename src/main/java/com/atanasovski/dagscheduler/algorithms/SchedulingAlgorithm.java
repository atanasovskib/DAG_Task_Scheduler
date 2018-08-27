package com.atanasovski.dagscheduler.algorithms;

import com.atanasovski.dagscheduler.Executable;
import com.atanasovski.dagscheduler.schedule.Schedule;

public interface SchedulingAlgorithm {
    Executable choose(Executable... readyTasks);

    boolean usesPriority();

    void calculatePriorities(Schedule schedule);
}
