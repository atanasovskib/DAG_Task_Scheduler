package com.atanasovski.dagscheduler.schedule;

import com.atanasovski.dagscheduler.algorithms.SchedulingAlgorithm;
import com.atanasovski.dagscheduler.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

public class Scheduler<Output> {
    private final Logger log = LoggerFactory.getLogger(Scheduler.class);

    private final ExecutorService schedulingExecutor;
    private final ExecutorService taskExecutor;
    private final Schedule<Output> schedule;
    private final CompletableFuture<Output> outputFuture;
    private final SchedulingAlgorithm algorithm;
    private final String sinkTaskId;
    private AtomicBoolean alreadyStartedOnce = new AtomicBoolean(false);
    private AtomicBoolean errorOccurred = new AtomicBoolean(false);

    public Scheduler(Schedule<Output> schedule, SchedulingAlgorithm schedulingAlgorithm,
                     int maxNumberOfExecutionThreads) {
        this.schedule = Objects.requireNonNull(schedule);
        this.algorithm = Objects.requireNonNull(schedulingAlgorithm);
        this.schedulingExecutor = Executors.newSingleThreadExecutor();
        this.taskExecutor = Executors.newFixedThreadPool(maxNumberOfExecutionThreads);
        this.outputFuture = schedule.onScheduleComplete();
        this.sinkTaskId = schedule.sinkTask.taskId;
    }

    public Future<Output> start() {
        log.debug("Starting to execute schedule");
        boolean alreadyStarted = this.alreadyStartedOnce.getAndSet(true);
        if (alreadyStarted) {
            throw new IllegalStateException("Scheduler has already executed");
        }

        this.schedulingExecutor.execute(this::checkForReadyTasks);
        return this.outputFuture;
    }

    private void checkForReadyTasks() {
        if (errorOccurred.get()) {
            log.info("An error has occurred, skipping scheduling");
            return;
        }

        // Because the same executor is used to complete a task as the ones to check for ready
        // tasks, then no changes in ready tasks can happen between choosing which next task
        // to be scheduled by the algorithm
        List<Task> readyTasks = this.schedule.getReady();
        log.debug("Found [{}] ready tasks", readyTasks.size());

        List<Task> tasksByPriority = this.algorithm.orderByPriority(readyTasks);

        tasksByPriority.forEach(this::scheduleReadyTask);
    }

    private void scheduleReadyTask(Task task) {
        if (errorOccurred.get()) {
            log.info("An error has occurred, skipping scheduling");
            return;
        }

        this.schedule.setRunning(task.taskId);

        Function<Throwable, Void> stopExecution = this.createErrorHandler(task.taskId);

        CompletableFuture<Void> taskFuture;
        taskFuture = CompletableFuture.supplyAsync(this.executeTask(task), this.taskExecutor)
                             .thenAcceptAsync(this::notifyTaskComplete, this.schedulingExecutor)
                             .exceptionally(stopExecution);
        if (task.taskId.equals(this.sinkTaskId)) {
            taskFuture.thenRunAsync(this::shutDownExecutors, this.schedulingExecutor);
        } else {
            taskFuture.thenRunAsync(this::checkForReadyTasks, this.schedulingExecutor);
        }
    }

    private void shutDownExecutors() {
        this.taskExecutor.shutdown();
        this.schedulingExecutor.shutdown();
    }

    private Function<Throwable, Void> createErrorHandler(String taskId) {
        return exceptionThrown -> {
            log.error("Execution failed in task [{}]", taskId, exceptionThrown);
            this.stopExecution(taskId, exceptionThrown);
            return null;
        };
    }

    private void stopExecution(String taskId, Throwable exceptionThrown) {
        this.taskExecutor.shutdown();
        this.schedulingExecutor.shutdown();
        this.errorOccurred.set(true);
        String errorMessage = "Schedule execution failed in task [" + taskId + "] Caused by "
                                      + exceptionThrown.getMessage();
        RuntimeException runtimeException = new RuntimeException(errorMessage, exceptionThrown);
        outputFuture.completeExceptionally(runtimeException);
    }

    private Supplier<String> executeTask(Task readyTask) {
        return () -> {
            String taskId = readyTask.taskId;
            log.info("Starting task [{}] execution", taskId);
            readyTask.compute();
            log.info("Ending task [{}] execution", taskId);
            return taskId;
        };
    }

    private void notifyTaskComplete(String completeTask) {
        log.debug("Notifying that task is complete [{}]", completeTask);
        this.schedule.onTaskComplete(completeTask);
    }
}
