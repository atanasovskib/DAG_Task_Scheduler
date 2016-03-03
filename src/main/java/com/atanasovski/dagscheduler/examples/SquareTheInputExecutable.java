package com.atanasovski.dagscheduler.examples;

import com.atanasovski.dagscheduler.Executable;

import java.util.List;

/**
 * Created by Blagoj on 24-Feb-16.
 */
public class SquareTheInputExecutable extends Executable {
    public SquareTheInputExecutable(String id) {
        super(id);
    }

    @Override
    public void execute() {
        List<? extends Object> a = this.get(SampleSchedule.Input_Square);
        a.stream().forEach(k -> {
            int aa = ((Integer) k).intValue();
            aa = aa * aa;
            produce(SampleSchedule.Result_Square, aa);
        });
    }
}
