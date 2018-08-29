package com.atanasovski.dagscheduler;

import com.atanasovski.dagscheduler.annotations.TaskOutput;
import com.atanasovski.dagscheduler.schedule.Schedule;
import com.atanasovski.dagscheduler.schedule.ScheduleBuilder;
import com.atanasovski.dagscheduler.schedule.Scheduler;
import com.atanasovski.dagscheduler.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atanasovski.dagscheduler.tasks.TaskBuilder.task;

public class ExampleClass extends Task {
    @TaskOutput(outputName = "output")
    public String b;

    public ExampleClass(String taskId) {
        super(taskId);
    }

    public static void main(String[] args) {
        Schedule schedule = ScheduleBuilder.startWith(task(ExampleClass.class).called("One task"))
                                    .build();

        Scheduler scheduler = new Scheduler(schedule, 1);
        scheduler.start();
    }

    @Override
    public void compute() {
        this.b = "Симона ТЕ САКАМ!!!";
        System.out.println(b);
    }
}
