package dev.atanasovski.dagscheduler;

import dev.atanasovski.dagscheduler.algorithms.SchedulingAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Scheduler {
    private final Logger logger = LoggerFactory.getLogger(Scheduler.class);
    private final boolean isBounded;
    private final SchedulingAlgorithm algorithm;
    private Schedule schedule;
    private final int maxNumberOfConcurrentTasks;
    private final AtomicInteger currentRunningTasks = new AtomicInteger(0);
    private final ExecutorService executor;

    public Scheduler(SchedulingAlgorithm algorithm) {
        this(algorithm, -1);
    }

    public Scheduler(SchedulingAlgorithm algorithm, int maxNumberOfConcurrentTasks) {
        this.algorithm = algorithm;
        this.maxNumberOfConcurrentTasks = maxNumberOfConcurrentTasks;
        this.isBounded = maxNumberOfConcurrentTasks > 0;

        this.executor = this.isBounded ?
                Executors.newFixedThreadPool(this.maxNumberOfConcurrentTasks) :
                Executors.newCachedThreadPool();
    }

    public void execute(Schedule schedule) {
        Objects.requireNonNull(schedule);

        logger.debug("Starting schedule execution");
        this.schedule = schedule;
        if (this.algorithm.usesPriority()) {
            this.algorithm.calculatePriorities(this.schedule);
        }

        while (!this.schedule.isDone()) {
            logger.debug("Schedule not done, more tasks to be scheduled or still executing");
            synchronized (this) {
                // TODO: Blagoj, implement this like a proper programmer
                if (this.isBounded) {
                    while (currentRunningTasks.get() < maxNumberOfConcurrentTasks) {
                        Executable[] readyTasks = this.schedule.getReadyTasks();
                        if (readyTasks.length == 0) {
                            break;
                        }

                        Executable chosen = this.algorithm.choose(readyTasks);
                        this.currentRunningTasks.incrementAndGet();
                        chosen.setScheduler(this);
                        this.schedule.setAsStarted(chosen);
                        this.executor.execute(chosen);
                    }
                } else {
                    Set<Executable> readyTasks = new HashSet<>(Arrays.asList(this.schedule.getReadyTasks()));
                    logger.debug("Ready tasks: " + readyTasks.size());
                    while (!readyTasks.isEmpty()) {
                        Executable chosen = this.algorithm.choose(readyTasks.toArray(new Executable[0]));
                        readyTasks.remove(chosen);
                        this.currentRunningTasks.incrementAndGet();
                        logger.info("Starting task: {}", chosen.getId());
                        chosen.setScheduler(this);
                        this.schedule.setAsStarted(chosen);
                        this.executor.execute(chosen);
                    }
                }

                if (!this.schedule.isDone()) {
                    try {
                        logger.debug("Waiting called from: {}", Thread.currentThread().getName());
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

        logger.debug("Execution done in scheduler");
        synchronized (this) {
            this.schedule = null;
        }

        this.executor.shutdown();
    }

    public synchronized void notifyDone(Executable task) {
        Objects.requireNonNull(this.schedule);
        this.currentRunningTasks.decrementAndGet();
        this.schedule.notifyDone(task);
        logger.debug("Waking up scheduler from: {}", Thread.currentThread().getName());
        this.notifyAll();
        logger.info("Task done: {}", task.getId());
    }

    public synchronized void notifyError(List<String> errors) {
        Objects.requireNonNull(this.schedule);
        this.schedule.notifyError(errors);
        logger.debug("Error in task {}, waking up scheduler", errors.toString());
        this.notifyAll();
    }
}
