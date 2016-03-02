package com.atanasovski.dagscheduler.examples;

import com.atanasovski.dagscheduler.Executable;
import com.atanasovski.dagscheduler.Scheduler;

/**
 * Created by Blagoj on 02-Mar-16.
 */
public class PrepareTemplate extends Executable {
    public PrepareTemplate(Scheduler scheduler, String id) {
        super(scheduler, id);
    }

    @Override
    public void execute() {
        //Simulate workload of reading file
        try {

            Thread.sleep(500);
        } catch (InterruptedException e) {
            error("work interrupted");
            e.printStackTrace();
        }

        produce(LogInUserSchedule.Template, "<html>something something {insert_result_here}</html>");
    }
}
