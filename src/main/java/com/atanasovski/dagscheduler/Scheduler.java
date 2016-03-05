package com.atanasovski.dagscheduler;

import com.atanasovski.dagscheduler.algorithms.SchedulingAlgorithm;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
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
        Objects.requireNonNull(schedule);
        CycleDetector<Executable, DefaultEdge> cycleDetector = new CycleDetector<>(schedule.getDependencies());
        if (cycleDetector.detectCycles()) {
            throw new IllegalArgumentException("Schedule contains cyclic dependencies between executable tasks");
        }

        this.executor = this.isBounded ?
                Executors.newFixedThreadPool(this.maxNumberOfConcurrentTasks) :
                Executors.newCachedThreadPool();
        logger.info("Starting schedule execution");
        this.schedule = schedule;
        if (this.algorithm.usesPriority()) {
            this.algorithm.calculatePriorities(this.schedule);
        }

        while (!this.schedule.isDone()) {
            logger.info("Schedule not done");
            synchronized (this) {
                logger.info("Current running tasks");
                // TODO: Blagoj, implement this like a proper programmer
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
                        this.currentRunningTasks.incrementAndGet();
                        chosen.setScheduler(this);
                        this.schedule.setAsStarted(chosen);
                        this.executor.execute(chosen);
                    }
                } else {
                    Set<Executable> readyTasks = new HashSet<>();
                    readyTasks.addAll(Arrays.asList(this.schedule.getReadyTasks()));
                    logger.info("ready tasks: " + readyTasks.size());
                    while (!readyTasks.isEmpty()) {
                        Executable chosen = this.algorithm.choose(readyTasks.toArray(new Executable[readyTasks.size()]));
                        readyTasks.remove(chosen);
                        this.currentRunningTasks.incrementAndGet();
                        logger.info("task start exe: {}", chosen.getId());
                        chosen.setScheduler(this);
                        this.schedule.setAsStarted(chosen);
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
        Objects.requireNonNull(this.schedule);

        this.currentRunningTasks.decrementAndGet();
        this.schedule.notifyDone(task);
        logger.info("Waking up: {}", Thread.currentThread().getName());
        this.notifyAll();
        logger.info("Task done: {}", task.getId());
    }

    public synchronized void notifyError(List<String> errors) {
        Objects.requireNonNull(this.schedule);
        this.schedule.notifyError(errors);
        logger.info("waking up");
        this.notifyAll();

    }
}
