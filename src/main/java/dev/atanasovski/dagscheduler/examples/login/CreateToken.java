package dev.atanasovski.dagscheduler.examples.login;

import dev.atanasovski.dagscheduler.Executable;

/**
 * Created by Blagoj on 09-Mar-16.
 */
public class CreateToken extends Executable {
    protected CreateToken(String id) {
        super(id);
    }

    @Override
    public void execute() {
        String userName = get("username").get(0).toString();
        int token = userName.hashCode();
        produce("token", token);
    }
}
