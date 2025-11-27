package com.example.executorservice.task;

public interface Task {
    void execute();
    String getTaskId();
    int getPriority();
}
