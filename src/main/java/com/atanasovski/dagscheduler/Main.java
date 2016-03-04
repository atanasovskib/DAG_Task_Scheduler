package com.atanasovski.dagscheduler;

import com.atanasovski.dagscheduler.examples.example3.ScheduleWithSharedResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Blagoj on 24-Feb-16.
 */
public class Main {
    static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) throws InterruptedException {
        Scheduler s = new Scheduler(new DummySchedulingAlgorithm());
        Schedule schedule = new ScheduleWithSharedResource();
        s.execute(schedule);
        logger.info("Done!");
    }
}
