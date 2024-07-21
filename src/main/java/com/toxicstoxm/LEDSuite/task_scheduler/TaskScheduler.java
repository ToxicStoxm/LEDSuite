package com.toxicstoxm.LEDSuite.task_scheduler;

import com.toxicstoxm.LEDSuite.yaml_factory.YAMLMessage;

import java.util.List;

public interface TaskScheduler {
    void cancelTask(int taskId);

    void cancelAllTasks();

    boolean isCurrentlyRunning(int taskId);

    boolean isQueued(int taskId);

    List<LEDSuiteWorker> getActiveWorkers();

    List<LEDSuiteTask> getPendingTasks();

    LEDSuiteTask runTask(Runnable task) throws IllegalStateException;

    LEDSuiteTask runTaskAsynchronously(Runnable task) throws IllegalStateException;

    LEDSuiteTask runTaskAsynchronously(Runnable runnable, YAMLMessage yaml);

    LEDSuiteTask runTaskLater(Runnable task, long delay) throws IllegalStateException;

    LEDSuiteTask runTaskLaterAsynchronously(Runnable task, long delay) throws IllegalStateException;

    LEDSuiteTask runTaskLaterAsynchronously(Runnable runnable, long delay, YAMLMessage yaml);

    LEDSuiteTask runTaskTimer(Runnable task, long delay, long period) throws IllegalStateException;

    LEDSuiteTask runTaskTimerAsynchronously(Runnable task, long delay, long period) throws IllegalStateException;

    LEDSuiteTask runTaskTimerAsynchronously(Runnable runnable, long delay, long period, YAMLMessage yaml);
}
