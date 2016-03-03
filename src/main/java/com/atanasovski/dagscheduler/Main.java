package com.atanasovski.dagscheduler;

import com.atanasovski.dagscheduler.examples.SampleSchedule;

import java.util.Arrays;

/**
 * Created by Blagoj on 24-Feb-16.
 */
public class Main {
    public static void main(String... args) throws InterruptedException {
        Scheduler s = new Scheduler(new DummySchedulingAlgorithm());
        Schedule schedule = new SampleSchedule(Arrays.asList(1, 2, 3, 4, 5), Arrays.asList(6, 7, 8, 9, 10));
        s.execute(schedule);
        System.out.println("results:" + schedule.getResults());
    }
}
