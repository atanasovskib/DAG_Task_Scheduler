package dev.atanasovski.dagscheduler.examples.withsharedresource;

import dev.atanasovski.dagscheduler.Executable;
import dev.atanasovski.dagscheduler.Schedule;

import java.util.concurrent.atomic.AtomicReference;

public class ScheduleWithSharedResource extends Schedule {
    public static final String Result = "result";

    public ScheduleWithSharedResource() {
        IdGenerator g = new IdGenerator();
        this.add(new A(g, "A1")).add(new A(g, "A2"));
    }
}

class IdGenerator {
    AtomicReference<Integer> serial = new AtomicReference<>(0);

    public int generate() {
        return serial.getAndAccumulate(1, Integer::sum);
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
        int newId = generator.generate();
        produce(ScheduleWithSharedResource.Result, String.format("%s: id %d", Thread.currentThread().getName(), newId));
    }
}
