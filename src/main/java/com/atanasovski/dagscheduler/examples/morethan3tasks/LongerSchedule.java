package com.atanasovski.dagscheduler.examples.morethan3tasks;

import com.atanasovski.dagscheduler.Executable;
import com.atanasovski.dagscheduler.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Blagoj on 05-Mar-16.
 */
public class LongerSchedule extends Schedule {
    public LongerSchedule() {
        A a = new A("a", 4);
        A b = new A("b", 2);
        A c = new A("c", 3);
        A d = new A("d", 8);
        A e = new A("e", 1);
        A f = new A("f", 1);
        this.add(a).add(b).add(c, a, b).add(d, b).add(e, c).add(f, d);
    }
}

class A extends Executable {
    final Logger logger = LoggerFactory.getLogger(A.class);

    protected A(String id, int executionTimeEstimate) {
        super(id, executionTimeEstimate);
    }

    @Override
    public void execute() {
        logger.info("Doing something in: {} for {} seconds", getId(), getExecutionTime());
        try {
            Thread.sleep(getExecutionTime() * 1000);
        } catch (InterruptedException e) {
            error("interrupted " + getId());
            e.printStackTrace();
        }
    }
}