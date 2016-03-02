package com.atanasovski.dagscheduler.examples;

import com.atanasovski.dagscheduler.Executable;
import com.atanasovski.dagscheduler.Scheduler;

import java.util.List;

/**
 * Created by Blagoj on 24-Feb-16.
 */
public class SumTheInputExecutable extends Executable {
    public static String InputParametersName = "InputParametersName";

    public SumTheInputExecutable(Scheduler scheduler, String id) {
        super(scheduler, id);
    }

    @Override
    public void execute() {
        List<Object> inputParams = this.get(InputParametersName);
        System.out.println("Input params for " + this.getId() + ": " + inputParams.toString());
        int result = inputParams.stream()
                .map(inputElement -> ((Integer) inputElement).intValue())
                .reduce(0, (k, l) -> k.intValue() + l.intValue());
        System.out.println("result from " + this.getId() + ": " + result);
        produce(SumTheInputExecutable.InputParametersName, result);
    }
}
