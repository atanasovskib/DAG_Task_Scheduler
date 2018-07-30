package com.atanasovski.dagscheduler.version2;

import static java.util.Objects.requireNonNull;

public class Schedule {
    public static ScheduleBuilderImpl with(TaskTemplate<? extends Task>... tasks) {
        return new ScheduleBuilderImpl(requireNonNull(tasks));
    }
}
