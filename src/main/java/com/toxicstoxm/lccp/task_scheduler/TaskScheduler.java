package com.toxicstoxm.lccp.task_scheduler;

import com.toxicstoxm.lccp.yaml_factory.YAMLMessage;

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

    LCCPTask runTaskAsynchronously(Runnable runnable, YAMLMessage yaml);

    LCCPTask runTaskLater(Runnable task, long delay) throws IllegalStateException;

    LCCPTask runTaskLaterAsynchronously(Runnable task, long delay) throws IllegalStateException;

    LCCPTask runTaskLaterAsynchronously(Runnable runnable, long delay, YAMLMessage yaml);

    LCCPTask runTaskTimer(Runnable task, long delay, long period) throws IllegalStateException;

    LCCPTask runTaskTimerAsynchronously(Runnable task, long delay, long period) throws IllegalStateException;

    LCCPTask runTaskTimerAsynchronously(Runnable runnable, long delay, long period, YAMLMessage yaml);
}


