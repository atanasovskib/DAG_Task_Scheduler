package com.atanasovski.dagscheduler.algorithms;

import com.atanasovski.dagscheduler.Executable;
import com.atanasovski.dagscheduler.Schedule;

/**
 * Created by Blagoj on 02-Mar-16.
 */
public interface SchedulingAlgorithm {
    Executable choose(Executable... readyTasks);

    boolean usesPriority();

    void calculatePriorities(Schedule schedule);
}
