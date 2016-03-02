package com.atanasovski.dagscheduler;

/**
 * Created by Blagoj on 02-Mar-16.
 */
public interface SchedulingAlgorithm {
    Executable choose(Executable... readyTasks);
}
