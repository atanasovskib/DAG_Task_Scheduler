package com.atanasovski.dagscheduler.examples.login;

import com.atanasovski.dagscheduler.Executable;
import com.atanasovski.dagscheduler.Schedule;

/**
 * Created by Blagoj on 02-Mar-16.
 */
public class LogInSchedule extends Schedule {
    public LogInSchedule(String userName, String passHash) {
        Executable cred = new ValidateCredentials("c.credentials")
                .addInput("credentials", userName, passHash);
        Executable prep = new PrepareTemplate("prep. template");
        Executable token = new CreateToken("cr. token");
        Executable response = new GenerateResponse("gen. response");
        this.add(cred)
                .add(prep, cred)
                .add(token, cred)
                .add(response, prep, token);
    }
}
