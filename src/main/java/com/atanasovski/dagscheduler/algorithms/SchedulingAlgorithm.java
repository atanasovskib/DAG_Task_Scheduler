package com.atanasovski.dagscheduler.algorithms;

import com.atanasovski.dagscheduler.schedule.Schedule;

import java.util.Map;

public interface SchedulingAlgorithm {
//    Executable choose(Executable... readyTasks);

    boolean usesPriority();

    Map<String, Long> calculatePriorities(Schedule schedule);
}
