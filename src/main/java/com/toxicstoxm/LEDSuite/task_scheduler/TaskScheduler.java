package com.toxicstoxm.LEDSuite.task_scheduler;

import com.toxicstoxm.LEDSuite.yaml_factory.YAMLMessage;
import java.util.List;

/**
 * Interface for scheduling and managing tasks within the LEDSuite system.
 * Provides methods to schedule tasks with various timing and execution
 * options, check task statuses, and manage active and pending tasks.
 *
 * @since 1.0.0
 */
public interface TaskScheduler {

    /**
     * Cancels a task with the specified ID.
     *
     * @param taskId The ID of the task to cancel.
     * @since 1.0.0
     */
    void cancelTask(int taskId);

    /**
     * Cancels all currently scheduled tasks.
     *
     * @since 1.0.0
     */
    void cancelAllTasks();

    /**
     * Checks if a task with the specified ID is currently running.
     *
     * @param taskId The ID of the task to check.
     * @return {@code true} if the task is currently running, {@code false} otherwise.
     * @since 1.0.0
     */
    boolean isCurrentlyRunning(int taskId);

    /**
     * Checks if a task with the specified ID is queued for execution.
     *
     * @param taskId The ID of the task to check.
     * @return {@code true} if the task is queued, {@code false} otherwise.
     * @since 1.0.0
     */
    boolean isQueued(int taskId);

    /**
     * Retrieves a list of currently active workers.
     *
     * @return A list of {@link LEDSuiteWorker} instances representing the active workers.
     * @since 1.0.0
     */
    List<LEDSuiteWorker> getActiveWorkers();

    /**
     * Retrieves a list of currently pending tasks.
     *
     * @return A list of {@link LEDSuiteTask} instances representing the pending tasks.
     * @since 1.0.0
     */
    List<LEDSuiteTask> getPendingTasks();

    /**
     * Schedules a task to run immediately.
     *
     * @param task The task to be scheduled.
     * @return The scheduled {@link LEDSuiteTask} instance.
     * @throws IllegalStateException If the task cannot be scheduled.
     * @since 1.0.0
     */
    LEDSuiteTask runTask(Runnable task) throws IllegalStateException;

    /**
     * Schedules a task to run asynchronously.
     *
     * @param task The task to be scheduled.
     * @return The scheduled {@link LEDSuiteTask} instance.
     * @throws IllegalStateException If the task cannot be scheduled.
     * @since 1.0.0
     */
    LEDSuiteTask runTaskAsynchronously(Runnable task) throws IllegalStateException;

    /**
     * Schedules a task to run asynchronously with an associated YAML message.
     *
     * @param runnable The task to be scheduled.
     * @param yaml The YAML message associated with the task.
     * @return The scheduled {@link LEDSuiteTask} instance.
     * @since 1.0.0
     */
    LEDSuiteTask runTaskAsynchronously(Runnable runnable, YAMLMessage yaml);

    /**
     * Schedules a task to run after a specified delay.
     *
     * @param task The task to be scheduled.
     * @param delay The delay in ticks before the task runs.
     * @return The scheduled {@link LEDSuiteTask} instance.
     * @throws IllegalStateException If the task cannot be scheduled.
     * @since 1.0.0
     */
    LEDSuiteTask runTaskLater(Runnable task, long delay) throws IllegalStateException;

    /**
     * Schedules a task to run asynchronously after a specified delay.
     *
     * @param task The task to be scheduled.
     * @param delay The delay in ticks before the task runs.
     * @return The scheduled {@link LEDSuiteTask} instance.
     * @throws IllegalStateException If the task cannot be scheduled.
     * @since 1.0.0
     */
    LEDSuiteTask runTaskLaterAsynchronously(Runnable task, long delay) throws IllegalStateException;

    /**
     * Schedules a task to run asynchronously after a specified delay with an associated YAML message.
     *
     * @param runnable The task to be scheduled.
     * @param delay The delay in ticks before the task runs.
     * @param yaml The YAML message associated with the task.
     * @return The scheduled {@link LEDSuiteTask} instance.
     * @since 1.0.0
     */
    LEDSuiteTask runTaskLaterAsynchronously(Runnable runnable, long delay, YAMLMessage yaml);

    /**
     * Schedules a task to run repeatedly with a specified delay and period.
     *
     * @param task The task to be scheduled.
     * @param delay The delay in ticks before the first run.
     * @param period The period in ticks between subsequent runs.
     * @return The scheduled {@link LEDSuiteTask} instance.
     * @throws IllegalStateException If the task cannot be scheduled.
     * @since 1.0.0
     */
    LEDSuiteTask runTaskTimer(Runnable task, long delay, long period) throws IllegalStateException;

    /**
     * Schedules a task to run asynchronously and repeatedly with a specified delay and period.
     *
     * @param task The task to be scheduled.
     * @param delay The delay in ticks before the first run.
     * @param period The period in ticks between subsequent runs.
     * @return The scheduled {@link LEDSuiteTask} instance.
     * @throws IllegalStateException If the task cannot be scheduled.
     * @since 1.0.0
     */
    LEDSuiteTask runTaskTimerAsynchronously(Runnable task, long delay, long period) throws IllegalStateException;

    /**
     * Schedules a task to run asynchronously and repeatedly with a specified delay and period,
     * with an associated YAML message.
     *
     * @param runnable The task to be scheduled.
     * @param delay The delay in ticks before the first run.
     * @param period The period in ticks between subsequent runs.
     * @param yaml The YAML message associated with the task.
     * @return The scheduled {@link LEDSuiteTask} instance.
     * @since 1.0.0
     */
    LEDSuiteTask runTaskTimerAsynchronously(Runnable runnable, long delay, long period, YAMLMessage yaml);
}
