package com.atanasovski.dagscheduler.tasks;

import com.atanasovski.dagscheduler.ExecutionStatus;

public interface Task {

    ExecutionStatus compute();
}
