package com.atanasovski.dagscheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * Created by Blagoj on 29-Feb-16.
 */
public class SharedResource<T> {
    final Logger logger = LoggerFactory.getLogger(SharedResource.class);
    private T value;
    private ReentrantLock lock = new ReentrantLock();

    public SharedResource(T value) {
        this.value = value;
    }

    public SharedResource<T> lock() {
        logger.info("Attempting to lock shared resource {}", Thread.currentThread().getName());
        lock.lock();
        logger.info("Lock acquired; {}", Thread.currentThread().getName());
        return this;
    }

    private T get() throws IllegalAccessException {
        if (lock.tryLock()) {
            logger.info("get method of SharedResource called; {}", Thread.currentThread().getName());
            lock.unlock();
            return value;
        }

        throw new IllegalAccessException("Resource locked by another thread. Call lock first");
    }

    public SharedResource<T> set(T t) throws InterruptedException, IllegalAccessException {
        if (lock.tryLock()) {
            logger.info("Setting new value: {}; {}", t.toString(), Thread.currentThread().getName());
            this.value = t;
            lock.unlock();
            return this;
        }

        throw new IllegalAccessException("Resource locked by another thread. Call lock first");
    }

    public SharedResource<T> unlock() throws IllegalAccessException {
        logger.info("Unlock called; {}", Thread.currentThread().getName());
        if (lock.tryLock()) {
            logger.info("Unlocking;");
            lock.unlock();
            lock.unlock();
            return this;
        }

        throw new IllegalAccessException("Resource was locked by another thread");
    }

    public <Result> ResourceOperation<Result> createSafeGet(Function<T, Result> operation) {
        if (operation == null) {
            throw new IllegalArgumentException("operation must not be null");
        }

        ResourceOperation ro = new ResourceOperation(this, operation);
        return ro;
    }

    public class ResourceOperation<Result> {
        private final Function<T, Result> doSomething;
        private SharedResource<T> resource;

        private ResourceOperation(SharedResource<T> resource, Function<T, Result> doSomething) {
            this.resource = resource;
            this.doSomething = doSomething;
        }

        public Result getResult() throws IllegalAccessException {
            if (this.resource == null) {
                throw new IllegalStateException("SharedResource that is supposed to be used is not set");
            }

            if (this.doSomething == null) {
                throw new IllegalStateException("Function that needs to be applied to resource is Null");
            }

            if (this.resource.privateTryLock()) {
                logger.info("Locked shared resource");
                Result result = doSomething.apply(resource.get());
                resource.privateUnlock();
                return result;
            }

            throw new IllegalAccessException("Resource was locked by another thread. Call lock before operating on the resource");
        }
    }

    private boolean privateTryLock() {
        logger.info("Trying to lock shared resource from {}", ResourceOperation.class.getSimpleName());
        return this.lock.tryLock();
    }

    private void privateUnlock() {
        logger.info("Unlocking shared resource from {}", ResourceOperation.class.getSimpleName());
        this.lock.unlock();
    }
}
