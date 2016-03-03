package com.atanasovski.dagscheduler.examples;

import com.atanasovski.dagscheduler.Executable;

import java.util.List;

/**
 * Created by Blagoj on 24-Feb-16.
 */
public class SumTheInputExecutable extends Executable {
    public SumTheInputExecutable(String id) {
        super(id);
    }

    @Override
    public void execute() {
        List<? extends Object> inputParams = this.get(SampleSchedule.Result_Square);
        int result = inputParams.stream()
                .map(element -> ((Integer) element).intValue())
                .reduce(0, (k, l) -> k.intValue() + l.intValue());
        produce(SampleSchedule.Final_Result, result);
    }
}
