package dev.atanasovski.dagscheduler.examples.morethan3tasks;

import dev.atanasovski.dagscheduler.Executable;
import dev.atanasovski.dagscheduler.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Blagoj on 05-Mar-16.
 */
public class LongerSchedule extends Schedule {
    public LongerSchedule() {
        T a = new T("a", 2);
        T b = new T("b", 4);
        T c = new T("c", 3);
        T d = new T("d", 8);
        T e = new T("e", 1);
        T f = new T("f", 1);
        this.add(a).add(b).add(c, a, b).add(d, a).add(e, c).add(f, d);
    }
}

class T extends Executable {
    final Logger logger = LoggerFactory.getLogger(T.class);

    protected T(String id, int executionTimeEstimate) {
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