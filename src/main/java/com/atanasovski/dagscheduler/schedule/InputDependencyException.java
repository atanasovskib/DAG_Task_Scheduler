package com.atanasovski.dagscheduler.schedule;

public class InputDependencyException extends RuntimeException {
    public InputDependencyException(String error) {
        super(error);
    }
}
