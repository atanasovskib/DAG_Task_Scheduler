package com.atanasovski.dagscheduler.examples;

import com.atanasovski.dagscheduler.Executable;
import com.atanasovski.dagscheduler.Scheduler;

import java.util.List;

/**
 * Created by Blagoj on 24-Feb-16.
 */
public class SquareTheInputExecutable extends Executable {
    public static String Input = "InputParametersName";

    public SquareTheInputExecutable(Scheduler scheduler, String id) {
        super(scheduler, id);
    }

    @Override
    public void execute() {
        List<Object> a = this.get(SquareTheInputExecutable.Input);
        System.out.println("input params for " + getId() + ": " + a.toString());

        a.stream().forEach(k -> {
            int aa = ((Integer) k).intValue();
            aa = aa * aa;
            produce(SumTheInputExecutable.InputParametersName, aa);
        });
    }
}
