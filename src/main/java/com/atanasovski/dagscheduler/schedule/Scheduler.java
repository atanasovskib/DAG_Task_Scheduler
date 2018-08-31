package com.atanasovski.dagscheduler.schedule;

import com.atanasovski.dagscheduler.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class Scheduler<Output> {
    private final Logger log = LoggerFactory.getLogger(Scheduler.class);

    private final ExecutorService schedulingExecutor;
    private final ExecutorService taskExecutor;
    private final Schedule<Output> schedule;
    private final CompletableFuture<Output> outputFuture;

    private AtomicBoolean alreadyStartedOnce = new AtomicBoolean(false);

    public Scheduler(Schedule<Output> schedule, int maxNumberOfExecutionThreads) {
        this.schedule = schedule;
        this.schedulingExecutor = Executors.newSingleThreadExecutor();
        this.taskExecutor = Executors.newFixedThreadPool(maxNumberOfExecutionThreads);
        this.outputFuture = schedule.onComplete().thenApply(output -> {
            this.taskExecutor.shutdown();
            this.schedulingExecutor.shutdown();
            return output;
        });

    }

    public Future<Output> start() {

        log.debug("Starting to execute schedule");
        boolean alreadyStarted = this.alreadyStartedOnce.getAndSet(true);
        if (alreadyStarted) {
            throw new IllegalStateException("Scheduler has already executed it's schedule");
        }

        this.schedulingExecutor.execute(this::checkForReadyTasks);
        return this.outputFuture;
    }


    private void checkForReadyTasks() {
        List<Task> readyTasks = this.schedule.getReady();
        log.debug("Found [{}] ready tasks", readyTasks.size());
        readyTasks.forEach(this::scheduleReadyTask);
    }

    private void scheduleReadyTask(Task task) {
        this.schedule.setRunning(task.taskId);
        CompletableFuture.supplyAsync(this.executeTask(task), this.taskExecutor)
                .thenAcceptAsync(this::notifyTaskComplete, this.schedulingExecutor)
                .thenRunAsync(this::checkForReadyTasks, this.schedulingExecutor);
    }

    private Supplier<String> executeTask(Task readyTask) {
        return () -> {
            log.debug("Starting task execution", readyTask.taskId);
            readyTask.compute();
            log.debug("Ending task [{}] execution", readyTask.taskId);
            return readyTask.taskId;
        };
    }

    private void notifyTaskComplete(String completeTask) {
        log.debug("Notifying that task is complete [{}]", completeTask);
        this.schedule.onTaskComplete(completeTask);
    }
}
