package com.atanasovski.dagscheduler.examples.example3;

import com.atanasovski.dagscheduler.Executable;
import com.atanasovski.dagscheduler.Schedule;
import com.atanasovski.dagscheduler.SharedResource;

import java.util.function.Function;

/**
 * Created by Blagoj on 04-Mar-16.
 */
public class ScheduleWithSharedResource extends Schedule {
    public static final String Result = "result";

    public ScheduleWithSharedResource() {
        IdGenerator g = new IdGenerator();
        this.add(new A(g, "A1")).add(new A(g, "A2"));
    }
}

class IdGenerator {
    SharedResource<Integer> serial = new SharedResource<>(0);

    public int generate() throws IllegalAccessException, InterruptedException {
        SharedResource<Integer>.ResourceOperation<Integer> getter = serial.createSafeGet(Function.identity());
        serial.lock();
        int newId = getter.getResult();
        serial.set(newId + 1).unlock();
        return newId;
    }
}

class A extends Executable {

    private final IdGenerator generator;

    protected A(IdGenerator generator, String id) {
        super(id);
        this.generator = generator;
    }

    @Override
    public void execute() {
        try {
            int newId = generator.generate();
            produce(ScheduleWithSharedResource.Result, String.format("%s: id %d", Thread.currentThread().getName(), newId));
        } catch (IllegalAccessException | InterruptedException e) {
            if (e instanceof IllegalAccessException) {
                error("resource not locked");
            } else {
                error("thread interrupted while requesting lock");
            }
        }
    }
}
