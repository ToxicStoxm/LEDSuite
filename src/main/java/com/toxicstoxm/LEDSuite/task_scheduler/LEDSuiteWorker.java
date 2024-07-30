package com.toxicstoxm.LEDSuite.task_scheduler;

/**
 * The `LEDSuiteWorker` interface represents a worker that executes tasks within the `LEDSuiteScheduler`.
 *
 * @since 1.0.0
 */
public interface LEDSuiteWorker {

    /**
     * Returns the unique ID of the task being executed by this worker.
     *
     * @return The task ID.
     * @since 1.0.0
     */
    int getTaskId();

    /**
     * Returns the thread in which this worker is executing the task.
     *
     * @return The thread of execution.
     * @since 1.0.0
     */
    Thread getThread();
}
