package com.x_tornado10.lccp.task_scheduler;

public interface LCCPWorker {
    int getTaskId();

    Thread getThread();
}
