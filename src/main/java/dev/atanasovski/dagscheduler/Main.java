package dev.atanasovski.dagscheduler;

import dev.atanasovski.dagscheduler.algorithms.DummySchedulingAlgorithm;
import dev.atanasovski.dagscheduler.examples.login.LogInSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Blagoj on 24-Feb-16.
 */
public class Main {
    static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) throws InterruptedException {
        Schedule schedule = new LogInSchedule("some user", "pass hash");
        Scheduler s = new Scheduler(new DummySchedulingAlgorithm());
        s.execute(schedule);
        logger.info("Done!");
        logger.info(schedule.getResults().toString());
    }
}