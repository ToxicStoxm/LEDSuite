package com.toxicstoxm.LEDSuite.task_scheduler;

/**
 * Represents a task that can be scheduled and executed within the LEDSuite scheduler.
 * This interface provides methods to get the task ID, check if the task is synchronous,
 * and cancel the task.
 *
 * @since 1.0.0
 */
public interface Task {

    /**
     * Gets the unique ID of this task.
     *
     * @return The unique task ID.
     * @since 1.0.0
     */
    int getTaskId();

    /**
     * Checks if the task is synchronous.
     *
     * @return {@code true} if the task is synchronous, {@code false} otherwise.
     * @since 1.0.0
     */
    boolean isSync();

    /**
     * Cancels this task.
     *
     * @since 1.0.0
     */
    void cancel();
}
