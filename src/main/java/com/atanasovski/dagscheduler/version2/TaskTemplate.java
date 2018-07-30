package com.atanasovski.dagscheduler.version2;

public class TaskTemplate<T extends Task> {
    public final String taskId;
    public final Class<T> taskType;

    public TaskTemplate(String taskId, Class<T> taskType) {
        this.taskId = taskId;
        this.taskType = taskType;
    }

    public String taskId(){
        return taskId;
    }
}
