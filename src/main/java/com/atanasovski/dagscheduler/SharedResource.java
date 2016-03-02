package com.atanasovski.dagscheduler;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Blagoj on 29-Feb-16.
 */
public class SharedResource<T> {
    private T value;
    private ReentrantLock s = new ReentrantLock();

    public SharedResource(T value) {
        this.value = value;
    }

    public void lock() {
        s.lock();
    }

    public T get() throws IllegalAccessException {
        if (s.tryLock()) {
            s.unlock();
            return value;
        }

        throw new IllegalAccessException("Resource locked by another thread. Call lock first");
    }

    public void set(T t) throws InterruptedException, IllegalAccessException {
        if (s.tryLock()) {
            System.out.println("setting: " + t + "; " + Thread.currentThread().getName());
            this.value = t;
            s.unlock();
        }

        throw new IllegalAccessException("Resource locked by another thread. Call lock first");
    }

    public void unlock() throws IllegalAccessException {
        if (s.tryLock()) {
            s.unlock();
            s.unlock();
            return;
        }

        throw new IllegalAccessException("Resource was locked by another thread");
    }
}
