package com.atanasovski.dagscheduler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Blagoj on 02-Mar-16.
 */
public class Scheduler {
    private final boolean isBounded;
    private SchedulingAlgorithm algorithm;
    private Schedule schedule;
    private final int maxNumberOfConcurrentTasks;
    private AtomicInteger currentRunningTasks = new AtomicInteger(0);
    private final Executor executor;

    public Scheduler(SchedulingAlgorithm algorithm) {
        this.algorithm = algorithm;
        this.isBounded = false;
        this.maxNumberOfConcurrentTasks = -1;
        this.executor = Executors.newCachedThreadPool();
    }

    public Scheduler(SchedulingAlgorithm algorithm, int maxNumberOfConcurrentTasks) {
        this.algorithm = algorithm;
        this.maxNumberOfConcurrentTasks = maxNumberOfConcurrentTasks;
        this.isBounded = true;
        this.executor = Executors.newFixedThreadPool(maxNumberOfConcurrentTasks);
    }

    public void execute(Schedule s) {
        System.out.println("starting schedule execution");
        synchronized (this) {
            this.schedule = s;
        }

        while (!schedule.isDone()) {
            System.out.println("schedule not done");
            synchronized (this) {
                System.out.println("current running tasks");
                if (this.isBounded) {
                    while (currentRunningTasks.get() < maxNumberOfConcurrentTasks) {
                        Executable[] readyTasks = this.schedule.getReadyTasks();
                        Executable chosen = this.algorithm.choose(readyTasks);
                        this.schedule.removeForExecution(chosen);
                        this.currentRunningTasks.incrementAndGet();
                        this.executor.execute(chosen);
                    }
                } else {
                    Set<Executable> readyTasks = new HashSet<>();
                    readyTasks.addAll(Arrays.asList(this.schedule.getReadyTasks()));
                    System.out.println("ready tasks: " + readyTasks.size());
                    while (!readyTasks.isEmpty()) {
                        Executable chosen = this.algorithm.choose(readyTasks.toArray(new Executable[readyTasks.size()]));
                        this.schedule.removeForExecution(chosen);
                        readyTasks.remove(chosen);
                        this.currentRunningTasks.incrementAndGet();
                        System.out.println("task start exe: " + chosen.getId());

                        this.executor.execute(chosen);
                    }
                }
                try {
                    System.out.println("waiting");
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        System.out.println("execution done in schedyler");
        synchronized (this) {
            this.schedule = null;
        }
    }

    public synchronized void notifyDone(Executable task) {
        System.out.println("task done: " + task.getId());
        if (this.schedule == null) {
            throw new IllegalStateException("no schedule is executing");
        }

        this.currentRunningTasks.decrementAndGet();
        this.schedule.notifyDone(task);
        System.out.println("waking up");
        this.notifyAll();
    }

    public synchronized void notifyError(List<String> errors) {
        if (this.schedule == null) {
            throw new IllegalStateException("no schedule is executing");
        }

        this.schedule.notifyError(errors);
        System.out.println("waking up");
        this.notifyAll();
    }
}
