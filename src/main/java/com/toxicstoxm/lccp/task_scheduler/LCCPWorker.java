package com.toxicstoxm.lccp.task_scheduler;

public interface LCCPWorker {
    int getTaskId();

    Thread getThread();
}
