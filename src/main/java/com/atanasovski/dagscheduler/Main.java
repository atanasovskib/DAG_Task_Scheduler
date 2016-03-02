package com.atanasovski.dagscheduler;

import com.atanasovski.dagscheduler.examples.LogInUserSchedule;

/**
 * Created by Blagoj on 24-Feb-16.
 */
public class Main {
    public static void main(String... args) throws InterruptedException {
        Scheduler s = new Scheduler(new DummySchedulingAlgorithm());
        Schedule schedule = new LogInUserSchedule(s, "user_name");
//        Executable sq1 = new SquareTheInputExecutable(s, "square1");
//        Executable sq2 = new SquareTheInputExecutable(s, "square2");
//        Map<String, List<Object>> input1 = new HashMap<>();
//        Map<String, List<Object>> input2 = new HashMap<>();
//        input1.put(SquareTheInputExecutable.Input, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
//        input2.put(SquareTheInputExecutable.Input, Arrays.asList(11, 12, 13, 14, 15, 16, 17, 18, 19, 20));
//        sq1.addInputParameters(input1);
//        sq2.addInputParameters(input2);
//        s.schedule(sq1);
//        s.schedule(sq2);
//        Executable sum1 = new SumTheInputExecutable(s, "sum1");
//        s.schedule(sum1, sq1);
//        Executable sum2 = new SumTheInputExecutable(s, "sum2");
//        s.schedule(sum2, sq2);
//        s.schedule(new SumTheInputExecutable(s, "sum3"), sum1, sum2);
//        s.run();
        s.execute(schedule);
        System.out.println("results:" + schedule.getResults());
    }
}
