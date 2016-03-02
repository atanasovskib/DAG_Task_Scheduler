# DAG_Task_Scheduler
A Java library for defining tasks that have directed acyclic dependencies and executing them with various scheduling algorithms. 

This is supposed to be a library that will allow a developer to quickly define executable tasks, define the dependencies between tasks. The library takes care of passing arguments between the tasks.
## Current state
1. Only one scheduling algorithm is implemented, selects the first of available tasks
2. Performance of the scheduler has not been taken into consideration
3. Not tested for concurrency errors, deadlocks or anything else
