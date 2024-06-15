package com.x_tornado10.lccp.task_scheduler;

import java.util.List;

public interface TaskScheduler {

    void cancelTask(int taskId);

    void cancelAllTasks();

    boolean isCurrentlyRunning(int taskId);

    boolean isQueued(int taskId);

    List<LCCPWorker> getActiveWorkers();

    List<LCCPTask> getPendingTasks();

    LCCPTask runTask(Runnable task) throws IllegalStateException;

    LCCPTask runTaskAsynchronously(Runnable task) throws IllegalStateException;

    LCCPTask runTaskLater(Runnable task, long delay) throws IllegalStateException;

    LCCPTask runTaskLaterAsynchronously(Runnable task, long delay) throws IllegalStateException;

    LCCPTask runTaskTimer(Runnable task, long delay, long period) throws IllegalStateException;

    LCCPTask runTaskTimerAsynchronously(Runnable task, long delay, long period) throws IllegalStateException;
}


