package com.toxicstoxm.lccp.task_scheduler;

public interface Task {
    int getTaskId();

    boolean isSync();

    void cancel();
}

