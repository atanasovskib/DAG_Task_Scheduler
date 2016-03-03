package com.atanasovski.dagscheduler.examples;

import com.atanasovski.dagscheduler.Executable;

import java.util.List;

/**
 * Created by Blagoj on 02-Mar-16.
 */
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
