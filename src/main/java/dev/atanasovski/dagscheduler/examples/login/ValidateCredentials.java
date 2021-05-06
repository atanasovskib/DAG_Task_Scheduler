package dev.atanasovski.dagscheduler.examples.login;

import dev.atanasovski.dagscheduler.Executable;

import java.util.List;

/**
 * Created by Blagoj on 02-Mar-16.
 */
public class ValidateCredentials extends Executable {

    public ValidateCredentials(String id) {
        super(id);
    }

    @Override
    public void execute() {
        List<? extends Object> input = this.get("credentials");
        if (input == null || input.size() != 2) {
            error("Wrong input");
            return;
        }

        //Simulate checking of credentials in database
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Pass username to PrepareTemplate and GenerateToken
        produce("username", input.get(0));
    }


}
