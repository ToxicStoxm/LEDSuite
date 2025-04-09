package com.toxicstoxm.LEDSuite.task_scheduler;

import com.toxicstoxm.LEDSuite.time.TickingSystem;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.YAJL.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * The `LEDSuiteRunnable` class is an abstract class that provides a base implementation
 * for tasks that can be scheduled and managed within the LEDSuite task scheduler.
 *
 * <p>This class implements are `Runnable` and provides various methods to schedule the task
 * with different execution modes, including immediate, delayed, and periodic execution.
 *
 * @since 1.0.0
 */
public abstract class LEDSuiteRunnable implements Runnable {
    private static final Logger logger = Logger.autoConfigureLogger();

    private int taskId = -1;

    /**
     * Cancels the scheduled task.
     *
     * @throws IllegalStateException If the task is not yet scheduled.
     * @since 1.0.0
     */
    public synchronized void cancel() throws IllegalStateException {
        LEDSuiteApplication.getScheduler().cancelTask(getTaskId());
    }

    /**
     * Runs this task synchronously within the LEDSuite scheduler.
     *
     * @return A reference to the scheduled task.
     * @throws IllegalStateException If the task is already scheduled or in an illegal state.
     * @since 1.0.0
     */
    public synchronized LEDSuiteTask runTask() throws IllegalStateException {
        checkState();
        return setupId(LEDSuiteApplication.getScheduler().runTask(this));
    }

    /**
     * Runs this task asynchronously within the LEDSuite scheduler.
     *
     * @return A reference to the scheduled task.
     * @throws IllegalStateException If the task is already scheduled or in an illegal state.
     * @since 1.0.0
     */
    public synchronized LEDSuiteTask runTaskAsynchronously() throws IllegalStateException {
        checkState();
        return setupId(LEDSuiteApplication.getScheduler().runTaskAsynchronously(this));
    }

    /**
     * Schedules this task to run after a specified delay.
     *
     * @param delay The delay in ticks before the task is run.
     * @return A reference to the scheduled task.
     * @throws IllegalStateException If the task is already scheduled or in an illegal state.
     * @since 1.0.0
     */
    public synchronized LEDSuiteTask runTaskLater(long delay) throws IllegalStateException {
        checkState();
        return setupId(LEDSuiteApplication.getScheduler().runTaskLater(this, TickingSystem.translate(delay)));
    }

    /**
     * Schedules this task to run asynchronously after a specified delay.
     *
     * @param delay The delay in ticks before the task is run.
     * @return A reference to the scheduled task.
     * @throws IllegalStateException If the task is already scheduled or in an illegal state.
     * @since 1.0.0
     */
    public synchronized LEDSuiteTask runTaskLaterAsynchronously(long delay) throws IllegalStateException {
        checkState();
        return setupId(LEDSuiteApplication.getScheduler().runTaskLaterAsynchronously(this, TickingSystem.translate(delay)));
    }

    /**
     * Schedules this task to run periodically after an initial delay.
     *
     * @param delay  The delay in ticks before the first execution.
     * @param period The period in ticks between later executions.
     * @return A reference to the scheduled task.
     * @throws IllegalStateException If the task is already scheduled or in an illegal state.
     * @since 1.0.0
     */
    public synchronized LEDSuiteTask runTaskTimer(long delay, long period) throws IllegalStateException {
        checkState();
        return setupId(LEDSuiteApplication.getScheduler().runTaskTimer(this, TickingSystem.translate(delay), TickingSystem.translate(period)));
    }

    /**
     * Schedules this task to run asynchronously and periodically after an initial delay.
     *
     * @param delay  The delay in ticks before the first execution.
     * @param period The period in ticks between later executions.
     * @return A reference to the scheduled task.
     * @throws IllegalStateException If the task is already scheduled or in an illegal state.
     * @since 1.0.0
     */
    public synchronized LEDSuiteTask runTaskTimerAsynchronously(long delay, long period) throws IllegalStateException {
        checkState();
        return setupId(LEDSuiteApplication.getScheduler().runTaskTimerAsynchronously(this, TickingSystem.translate(delay), TickingSystem.translate(period)));
    }

    /**
     * Retrieves the unique task ID of the scheduled task.
     *
     * @return The unique task ID.
     * @throws IllegalStateException If the task is not yet scheduled.
     * @since 1.0.0
     */
    public synchronized int getTaskId() throws IllegalStateException {
        final int id = taskId;
        if (id == -1) {
            throw new IllegalStateException("Not scheduled yet");
        }
        return id;
    }

    /**
     * Checks the state of the task to ensure it is valid to run.
     *
     * <p>This method is called before scheduling the task to ensure it is not already scheduled.
     *
     * @since 1.0.0
     */
    protected void checkState() {
        if (taskId != -1) {
            throw new IllegalStateException("Already scheduled as " + taskId);
        }
    }

    /**
     * Sets up the task ID for the scheduled task.
     *
     * @param task The scheduled task.
     * @return The scheduled task.
     * @since 1.0.0
     */
    protected LEDSuiteTask setupId(final @NotNull LEDSuiteTask task) {
        this.taskId = task.getTaskId();
        logger.verbose("Running task with id -> '{}'", taskId);
        return task;
    }
}
