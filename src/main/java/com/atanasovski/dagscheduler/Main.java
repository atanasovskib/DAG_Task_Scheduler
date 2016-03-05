package com.atanasovski.dagscheduler;

import com.atanasovski.dagscheduler.algorithms.HLFETSchedulingAlgorithm;
import com.atanasovski.dagscheduler.examples.example2.SampleSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by Blagoj on 24-Feb-16.
 */
public class Main {
    static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) throws InterruptedException {
        Scheduler s = new Scheduler(new HLFETSchedulingAlgorithm());
        Schedule schedule = new SampleSchedule(Arrays.asList(1, 1, 1), Arrays.asList(1, 1, 1, 1));
        s.execute(schedule);
        logger.info("Done!");
        logger.info(schedule.getResults().toString());
    }
}
