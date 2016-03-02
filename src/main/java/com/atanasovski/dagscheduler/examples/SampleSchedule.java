package com.atanasovski.dagscheduler.examples;

import com.atanasovski.dagscheduler.Executable;
import com.atanasovski.dagscheduler.Schedule;
import com.atanasovski.dagscheduler.Scheduler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Blagoj on 02-Mar-16.
 */
public class SampleSchedule extends Schedule {
    public static final String Input_Square = "input_square";
    public static final String Result_Square = "result_square";
    public static final String Final_Result = "final_result";

    public SampleSchedule(Scheduler s, List<Integer> input1, List<Integer> input2) {
        Executable sq1 = new SquareTheInputExecutable(s, "Square1");
        Executable sq2 = new SquareTheInputExecutable(s, "Square2");
        Map<String, List<? extends Object>> sq1Input = new HashMap<>();
        Map<String, List<? extends Object>> sq2Input = new HashMap<>();
        sq1Input.put(Input_Square, input1);
        sq2Input.put(Input_Square, input2);
        sq1.addInputParameters(sq1Input);
        sq2.addInputParameters(sq2Input);
        this.add(sq1);
        this.add(sq2);
        this.add(new SumTheInputExecutable(s, "Sum"), sq1, sq2);
    }
}
