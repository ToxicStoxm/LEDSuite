package com.toxicstoxm.LEDSuite.task_scheduler;

public interface Task {
    int getTaskId();

    boolean isSync();

    void cancel();
}
