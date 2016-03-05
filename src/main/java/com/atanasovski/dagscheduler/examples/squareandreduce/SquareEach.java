package com.atanasovski.dagscheduler.examples.squareandreduce;

import com.atanasovski.dagscheduler.Executable;

import java.util.List;

/**
 * Created by Blagoj on 24-Feb-16.
 */
public class SquareEach extends Executable {
    public SquareEach(String id, int numberOfInputElements) {
        super(id, numberOfInputElements);
    }

    @Override
    public void execute() {
        List<? extends Object> a = this.get(SampleSchedule.Input_Square);
        a.stream().forEach(k -> {
            int aa = (Integer) k;
            produce(SampleSchedule.Result_Square, aa * aa);
        });
    }
}
