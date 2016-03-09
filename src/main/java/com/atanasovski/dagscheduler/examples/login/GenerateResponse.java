package com.atanasovski.dagscheduler.examples.login;

import com.atanasovski.dagscheduler.Executable;

/**
 * Created by Blagoj on 09-Mar-16.
 */
public class GenerateResponse extends Executable {

    protected GenerateResponse(String id) {
        super(id);
    }

    @Override
    public void execute() {
        String template = get("template").get(0).toString();
        int token = (Integer) get("token").get(0);
        Response r = new Response();
        r.header = template;
        r.body = Integer.toString(token);
        produce("response", r);
    }
}

class Response {
    String header;
    String body;

    @Override
    public String toString() {
        return "Response{" +
                "header='" + header + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}