package com.atanasovski.dagscheduler.examples.login;

import com.atanasovski.dagscheduler.Executable;

import java.util.List;

/**
 * Created by Blagoj on 02-Mar-16.
 */
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
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            error("thread interrupted");
        }

        produce(LogInUserSchedule.CheckCredentialsResult, true);
        produce(LogInUserSchedule.Username, input.get(0));
    }


}
