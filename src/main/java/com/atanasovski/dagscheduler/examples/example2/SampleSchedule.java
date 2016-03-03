package com.atanasovski.dagscheduler.examples.example2;

import com.atanasovski.dagscheduler.Executable;
import com.atanasovski.dagscheduler.Schedule;

import java.util.List;

/**
 * Created by Blagoj on 02-Mar-16.
 */
public class SampleSchedule extends Schedule {
    public static final String Input_Square = "input_square";
    public static final String Result_Square = "result_square";
    public static final String Final_Result = "final_result";

    public SampleSchedule(List<Integer> input1, List<Integer> input2) {
        Executable sq1 = new SquareEach("Square1").addInput(Input_Square, input1);
        Executable sq2 = new SquareEach("Square2").addInput(Input_Square, input2);
        this.add(sq1)
                .add(sq2)
                .add(new ReduceWithSum("Sum"), sq1, sq2);
    }
}
