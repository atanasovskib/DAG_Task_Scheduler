package com.atanasovski.dagscheduler.examples;

import com.atanasovski.dagscheduler.Executable;
import com.atanasovski.dagscheduler.Scheduler;
import com.atanasovski.dagscheduler.Schedule;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Blagoj on 02-Mar-16.
 */
public class LogInUserSchedule extends Schedule {
    public static final String CheckCredentialsResult = "credentials";
    public static final String Template = "template";
    public static final String Username = "username";
    public static final String Result = "result";

    public LogInUserSchedule(String userName) {
        Executable cred = new CheckCredentialsForUser("check credentials");
        Map<String, List<Object>> input = new HashMap<>();
        input.put(CheckCredentialsForUser.USER_NAME, Arrays.asList(userName));
        cred.addInput(input);
        this.add(cred);
        Executable prep = new PrepareTemplate("prepare template");
        this.add(prep);
        this.add(new DisplayResult("display result"), cred, prep);
    }
}
