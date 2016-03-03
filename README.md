# DAG_Task_Scheduler
A Java library for defining tasks that have directed acyclic dependencies and executing them with various scheduling algorithms. 

This is supposed to be a library that will allow a developer to quickly define executable tasks, define the dependencies between tasks. The library takes care of passing arguments between the tasks.

## Current state
1. Only one scheduling algorithm is implemented, selects the first of available tasks
2. Performance of the scheduler has not been taken into consideration
3. Not tested for concurrency errors, deadlocks or anything else

## Example
Task of type Square = takes a list of integers, squares them
Task of type Sum = takes a list of integers, reduces them by addition
Example: Task Square1 - take numbers from 1 to 5, square them
         Task Square2 - take numbers from 6 to 10 square them
         Task Sum - take results from Square1 and Square2, apply reduction by addition and produce result
```java
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

public class SampleSchedule extends Schedule {
    public static final String Input_Square = "input_square";
    public static final String Result_Square = "result_square";
    public static final String Final_Result = "final_result";

    public SampleSchedule(List<Integer> input1, List<Integer> input2) {
        Executable sq1 = new SquareTheInputExecutable("Square1").addInput(Input_Square, input1);
        Executable sq2 = new SquareTheInputExecutable("Square2").addInput(Input_Square, input2);
        this.add(sq1)
            .add(sq2)
            .add(new SumTheInputExecutable("Sum"), sq1, sq2);
    }
}

public static void main(String... args){
    Scheduler s = new Scheduler(new DummySchedulingAlgorithm());
    Schedule schedule = new SampleSchedule(Arrays.asList(1, 2, 3, 4, 5), Arrays.asList(6, 7, 8, 9, 10));
    s.execute(schedule);
    System.out.println("results:" + schedule.getResults());
}
```
