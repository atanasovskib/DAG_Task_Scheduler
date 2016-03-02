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
    public SquareTheInputExecutable(Scheduler scheduler, String id) {
        super(scheduler, id);
    }

    @Override
    public void execute() {
        List<Object> input = this.get(SampleSchedule.Input_Square);
        input.stream().forEach(k -> {
            int a = ((Integer) k).intValue();
            produce(SampleSchedule.Result_Square, a*a);
        });
    }
}

public class SumTheInputExecutable extends Executable {
    public SumTheInputExecutable(Scheduler scheduler, String id) {
        super(scheduler, id);
    }

    @Override
    public void execute() {
        List<Object> inputParams = this.get(SampleSchedule.Result_Square);
        int result = inputParams.stream()
                .map(inputElement -> ((Integer) inputElement).intValue())
                .reduce(0, (k, l) -> k.intValue() + l.intValue());
        produce(SampleSchedule.Final_Result, result);
    }
}

public class SampleSchedule extends Schedule {
    public static final String Input_Square = "input_square";
    public static final String Result_Square = "result_square";
    public static final String Final_Result = "final_result";
}

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

void main(String... args){
  Scheduler s = new Scheduler(new DummySchedulingAlgorithm());
  Schedule schedule = new SampleSchedule(s, Arrays.asList(1,2,3,4,5), Arrays.asList(6,7,8,9,10);
  s.execute(schedule);
  System.out.println(s.getResults());
}
```
