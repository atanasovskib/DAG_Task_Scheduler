# DAG_Task_Scheduler
A Java library for defining tasks that have directed acyclic dependencies and executing them with various scheduling algorithms. 

This is supposed to be a library that will allow a developer to quickly define executable tasks, define the dependencies between tasks. The library takes care of passing arguments between the tasks.

The library also gives a way to easily define a resource that is supposed to be shared between multiple threads, providing the user with a mechanism to lock, modify and unlock the resource (See example 3)

## Current state
1. Only one scheduling algorithm is implemented, selects the first of available tasks
2. Performance of the scheduler has not been taken into consideration

##Implemented algorithms
* Wu, Min-You, and Daniel D. Gajski. "Hypertool: A programming aid for message-passing systems." IEEE Transactions on Parallel & Distributed Systems 3 (1990): 330-343.
* Sih, Gilbert C., and Edward Lee. "A compile-time scheduling heuristic for interconnection-constrained heterogeneous processor architectures." Parallel and Distributed Systems, IEEE Transactions on 4.2 (1993): 175-187.
* Ahmad, Ishfaq, and Yu-Kwong Kwok. "A new approach to scheduling parallel programs using task duplication." Parallel Processing, 1994. ICPP 1994 Volume 2. International Conference on. Vol. 2. IEEE, 1994.
* Kwok, Yu-Kwong, and Lshfaq Ahmad. "Dynamic critical-path scheduling: An effective technique for allocating task graphs to multiprocessors." Parallel and Distributed Systems, IEEE Transactions on 7.5 (1996): 506-521.


1. DummySchedulingAlgorithm
..* Takes an array of ready tasks, schedules the first one
2. Highest Levels First with Estimated Times (HLFET) 
..* This algorithm assigns each task/node in the computation DAG a level, or priority for scheduling. The level of node ni is defined as the largest sum of execution times along any directed path from ni to an end node of the graph, over all end nodes of the graph. Since these levels remain constant for the entire scheduling duration, we refer to these levels as being static, and denote the static level of a node ni as SL(ni). The list scheduling algorithm is then invoked using SL as priority measure.
3. Modified Critical Path (MCP)
..* The Modified Critical-Path (MCP) algorithm is designed based on an attribute called latest possible start time of a task (strand/node in the computation DAG). A task’s latest possible start time is determined through the as-late-as-possible (ALAP) binding, which is done by traversing the task graph upward from the exit nodes to the entry nodes and by pulling the nodes downwards as much as possible constrained by the length of the critical path (CP). The MCP algorithm first computes all the latest possible start times for all nodes. Then, each node is associated with a list of latest possible start times which consists of the latest possible start time of the node itself, followed by a decreasing order of the latest possible start times of its children nodes. The MCP algorithm then constructs a list of nodes in an increasing lexicographical order of the latest possible start times lists. At each scheduling step, the first node is removed from the list and scheduled to a processor that allows for the earliest start time. 
..* Wu, Min-You, and Daniel D. Gajski. "Hypertool: A programming aid for message-passing systems." IEEE Transactions on Parallel & Distributed Systems 3 (1990): 330-343.
4. Mobility Directed (MD) 
..* The Mobility Directed (MD) algorithm selects a node at each step for scheduling based on an attribute called the relative mobility. Mobility of a node is defined as the difference between a node’s earliest start time and latest start time. Similar to the ALAP binding in MCP, the earliest possible start time is assigned to each node through the as-soon-as-possible (ASAP) binding which is done by traversing the task graph downward from the entry nodes to the exit nodes and by pulling the nodes upward as much as possible. Relative mobility is obtained by dividing the mobility with the node’s computation cost. Essentially, a node with zero mobility is a node on the CP. At each step, the MPD algorithm schedules the node with the smallest mobility to the first processor which has a large enough time slot to accommodate the node without considering the minimization of the node’s start time. After a node is scheduled, all the relative mobilities are updated.

## Examples
### Example 1: 
A sample workflow in an application server
* **Task CheckUserCredentials** - go to DB and check if credentials are ok
* **Task PrepareTemplate** - read some file that contains a web site template
* **Task DisplayResult** - if credentials are ok, put username in template, else put error
 
Dependencies DAG
```
CheckUserCredentials---→DisplayResult
PrepareTemplate--------↗
```
```java
public class CheckCredentialsForUser extends Executable {

    public CheckCredentialsForUser(String id) {
        super(id);
    }

    @Override
    public void execute() {
        List<? extends Object> input = this.get(LogInUserSchedule.Username);
        if (input == null || input.size() != 1) {
            error("wrong input");
        }

        //Simulate checking of credentials in database
        Thread.sleep(1000);
        produce(LogInUserSchedule.CheckCredentialsResult, true);
        produce(LogInUserSchedule.Username, input.get(0));
    }
}

public class PrepareTemplate extends Executable {
    public PrepareTemplate(String id) {
        super(id);
    }

    @Override
    public void execute() {
        //Simulate workload of reading file
        Thread.sleep(500);
        produce(LogInUserSchedule.Template, "<html>something something {insert_result_here}</html>");
    }
}

public class DisplayResult extends Executable {
    public DisplayResult(String id) {
        super(id);
    }

    @Override
    public void execute() {
        List<? extends Object> credOk = get(LogInUserSchedule.CheckCredentialsResult);
        if (true == (Boolean) credOk.get(0)) {
            String username = (String) get(LogInUserSchedule.Username).get(0);
            String template = (String) get(LogInUserSchedule.Template).get(0);
            produce(LogInUserSchedule.Result, template.replaceAll("\\{insert_result_here\\}", username));
        } else {
            String template = (String) get(LogInUserSchedule.Template).get(0);
            produce(LogInUserSchedule.Result, template.replaceAll("\\{insert_result_here\\}", "Wrong credentials!!!"));
        }
    }
}

public class LogInUserSchedule extends Schedule {
    public static final String CheckCredentialsResult = "credentials";
    public static final String Template = "template";
    public static final String Username = "username";
    public static final String Result = "result";

    public LogInUserSchedule(String userName) {
        Executable cred = new CheckCredentialsForUser("check credentials")
                .addInput(Username, userName);
        Executable prep = new PrepareTemplate("prepare template");
        this.add(cred)
            .add(prep)
            .add(new DisplayResult("display result"), cred, prep);
    }
}
```
### Example 2: 
Task of type Square = takes a list of integers, squares them

Task of type Sum = takes a list of integers, reduces them by addition
* **Task Square1** - take numbers from 1 to 5, square them
* **Task Square2** - take numbers from 6 to 10, square them
* **Task Sum** - take results from Square1 and Square2, apply reduction by addition and produce result
 
Dependencies DAG
```
Square1---→Sum
Square2---↗
```
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
### Example - 3 Using a shared resource
Two parallel tasks use the same id generator
```java

class A extends Executable {
    private final IdGenerator generator;

    protected A(IdGenerator generator, String id) {
        super(id);
        this.generator = generator;
    }

    @Override
    public void execute() {
        try {
            int newId = generator.generate();
            produce(ScheduleWithSharedResource.Result, String.format("%s: id %d", Thread.currentThread().getName(), newId));
        } catch (IllegalAccessException | InterruptedException e) {
            if (e instanceof IllegalAccessException) {
                error("resource not locked");
            } else {
                error("thread interrupted while requesting lock");
            }
        }
    }
}

class IdGenerator {
    SharedResource<Integer> serial = new SharedResource<>(0);

    public int generate() throws IllegalAccessException, InterruptedException {
        SharedResource<Integer>.ResourceOperation<Integer> getter = serial.createOperation(Function.identity());
        serial.lock();
        int newId = getter.getResult();
        serial.set(newId + 1).unlock();
        return newId;
    }
}

public class ScheduleWithSharedResource extends Schedule {
    public static final String Result = "result";

    public ScheduleWithSharedResource() {
        IdGenerator g = new IdGenerator();
        this.add(new A(g, "A1")).add(new A(g, "A2"));
    }
}

public static void main(String... args) throws InterruptedException {
    Scheduler s = new Scheduler(new DummySchedulingAlgorithm());
    Schedule schedule = new ScheduleWithSharedResource();
    s.execute(schedule);
}
```
### Example - 4 Using different scheduling algorithms
The schedule with the tasks and the dependencies between them, the numbers in the parenthesis are the execution time estimates for each of the nodes:
```
b(4) ---> c(3)----->e(1)
a(2)------↑--->d(8)--->f(1)
```
```java
public class LongerSchedule extends Schedule {
    public LongerSchedule() {
        T a = new T("a", 2);
        T b = new T("b", 4);
        T c = new T("c", 3);
        T d = new T("d", 8);
        T e = new T("e", 1);
        T f = new T("f", 1);
        this.add(a).add(b).add(c, a, b).add(d, a).add(e, c).add(f, d);
    }
}

class T extends Executable {
    final Logger logger = LoggerFactory.getLogger(T.class);

    protected T(String id, int executionTimeEstimate) {
        super(id, executionTimeEstimate);
    }

    @Override
    public void execute() {
        logger.info("Doing something in: {} for {} seconds", getId(), getExecutionTime());
        Thread.sleep(getExecutionTime() * 1000);
    }
}

void main(){
    Schedule schedule = new LongerSchedule();
    Scheduler s = new Scheduler(new MCPSchedulingAlgorithm());
    s.execute(schedule);
    Scheduler s1 = new Scheduler(new HLFETSchedulingAlgorithm());
    s1.execute(schedule);
    logger.info("Done!");
}
```
