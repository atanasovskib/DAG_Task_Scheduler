package com.atanasovski.dagscheduler;

import com.atanasovski.dagscheduler.algorithms.MCPSchedulingAlgorithm;
import com.atanasovski.dagscheduler.examples.morethan3tasks.LongerSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Blagoj on 24-Feb-16.
 */
public class Main {
    static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) throws InterruptedException {
        Scheduler s = new Scheduler(new MCPSchedulingAlgorithm(), 1);
        Schedule schedule = new LongerSchedule();
        s.execute(schedule);
        logger.info("Done!");
        logger.info(schedule.getResults().toString());
    }
}
