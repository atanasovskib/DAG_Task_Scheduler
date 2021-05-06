package dev.atanasovski.dagscheduler.examples.login;

import dev.atanasovski.dagscheduler.Executable;

/**
 * Created by Blagoj on 02-Mar-16.
 */
public class PrepareTemplate extends Executable {
    public PrepareTemplate(String id) {
        super(id);
    }

    @Override
    public void execute() {
        //Simulate workload of creating html
        String userName = (String) get("username").get(0);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        produce("template", String.format("<html>something something %s</html>", userName));
    }
}
