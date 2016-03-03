package com.atanasovski.dagscheduler.examples.example1;

import com.atanasovski.dagscheduler.Executable;
import com.atanasovski.dagscheduler.Schedule;

/**
 * Created by Blagoj on 02-Mar-16.
 */
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
