package dev.atanasovski.dagscheduler.examples.squareandreduce;

import dev.atanasovski.dagscheduler.Executable;

import java.util.List;

/**
 * Created by Blagoj on 24-Feb-16.
 */
public class ReduceWithSum extends Executable {
    public ReduceWithSum(String id, int numberOfElements) {
        super(id, numberOfElements);
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
