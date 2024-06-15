package com.x_tornado10.lccp.task_scheduler;

public interface Task {
    int getTaskId();

    boolean isSync();

    void cancel();
}

