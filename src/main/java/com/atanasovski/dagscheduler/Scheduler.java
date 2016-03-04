package com.atanasovski.dagscheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Blagoj on 02-Mar-16.
 */
public class Scheduler {
    private final Logger logger = LoggerFactory.getLogger(Scheduler.class);
    private final boolean isBounded;
    private SchedulingAlgorithm algorithm;
    private Schedule schedule;
    private final int maxNumberOfConcurrentTasks;
    private AtomicInteger currentRunningTasks = new AtomicInteger(0);
    private ExecutorService executor;

    public Scheduler(SchedulingAlgorithm algorithm) {
        this.algorithm = algorithm;
        this.isBounded = false;
        this.maxNumberOfConcurrentTasks = -1;
    }

    public Scheduler(SchedulingAlgorithm algorithm, int maxNumberOfConcurrentTasks) {
        this.algorithm = algorithm;
        this.maxNumberOfConcurrentTasks = maxNumberOfConcurrentTasks;
        this.isBounded = true;
    }

    public void execute(Schedule schedule) {
        this.executor = this.isBounded ? Executors.newFixedThreadPool(this.maxNumberOfConcurrentTasks) :
                Executors.newCachedThreadPool();
        logger.info("starting schedule execution");
        synchronized (this) {
            this.schedule = schedule;
        }

        while (!this.schedule.isDone()) {
            logger.info("schedule not done");
            synchronized (this) {
                logger.info("current running tasks");
                if (this.isBounded) {
                    try {
                        if (!this.schedule.isDone()) {
                            logger.info("waiting: {}", Thread.currentThread().getName());
                            this.wait();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    while (currentRunningTasks.get() < maxNumberOfConcurrentTasks) {
                        Executable[] readyTasks = this.schedule.getReadyTasks();
                        Executable chosen = this.algorithm.choose(readyTasks);
                        this.schedule.removeForExecution(chosen);
                        this.currentRunningTasks.incrementAndGet();
                        chosen.setScheduler(this);
                        this.executor.execute(chosen);
                    }
                } else {
                    Set<Executable> readyTasks = new HashSet<>();
                    readyTasks.addAll(Arrays.asList(this.schedule.getReadyTasks()));
                    logger.info("ready tasks: " + readyTasks.size());
                    while (!readyTasks.isEmpty()) {
                        Executable chosen = this.algorithm.choose(readyTasks.toArray(new Executable[readyTasks.size()]));
                        this.schedule.removeForExecution(chosen);
                        readyTasks.remove(chosen);
                        this.currentRunningTasks.incrementAndGet();
                        logger.info("task start exe: {}", chosen.getId());
                        chosen.setScheduler(this);
                        this.executor.execute(chosen);
                    }
                }

                if (!this.schedule.isDone()) {
                    try {
                        logger.info("Waiting: {}", Thread.currentThread().getName());
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

        logger.info("execution done in scheduler");
        synchronized (this) {
            this.schedule = null;
        }
        
        this.executor.shutdown();
    }

    public synchronized void notifyDone(Executable task) {
        logger.info("task done: {}", task.getId());
        if (this.schedule == null) {
            throw new IllegalStateException("no schedule is executing");
        }

        this.currentRunningTasks.decrementAndGet();
        this.schedule.notifyDone(task);
        logger.info("waking up: {}", Thread.currentThread().getName());
        this.notifyAll();
        logger.info("task done: {}", task.getId());
    }

    public synchronized void notifyError(List<String> errors) {
        if (this.schedule == null) {
            throw new IllegalStateException("no schedule is executing");
        }

        this.schedule.notifyError(errors);
        logger.info("waking up");
        this.notifyAll();

    }
}
