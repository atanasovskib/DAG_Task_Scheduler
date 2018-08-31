package com.atanasovski.dagscheduler.tasks;

import java.util.concurrent.CompletableFuture;

public abstract class Sink<T> extends Task {

    public final CompletableFuture<T> future;

    public Sink(String taskId) {
        super(taskId);
        future = new CompletableFuture<>();
    }

    public abstract T getResult();


    @Override
    public void compute() {
        future.complete(getResult());
    }
}
