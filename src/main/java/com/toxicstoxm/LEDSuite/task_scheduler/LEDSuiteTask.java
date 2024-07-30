package com.toxicstoxm.LEDSuite.task_scheduler;

import com.toxicstoxm.LEDSuite.LEDSuite;
import com.toxicstoxm.LEDSuite.yaml_factory.YAMLMessage;

/**
 * The `LEDSuiteTask` class represents a task to be scheduled and executed by the `LEDSuiteScheduler`.
 * It implements both the `Task` and `Runnable` interfaces and manages task execution, scheduling, and cancellation.
 *
 * @since 1.0.0
 */
public class LEDSuiteTask implements Task, Runnable {
    private volatile LEDSuiteTask next = null;
    /**
     * -1 means no repeating <br>
     * -2 means cancel <br>
     * -3 means processing for Future <br>
     * -4 means done for Future <br>
     * Never 0 <br>
     * >0 means number of ticks to wait between each execution
     *
     * @since 1.0.0
     */
    private volatile long period;
    private long nextRun;
    private final Runnable task;
    private YAMLMessage yaml;
    private final int id;

    /**
     * Constructs an empty LEDSuiteTask.
     *
     * @since 1.0.0
     */
    LEDSuiteTask() {
        this(null, -1, -1);
    }

    /**
     * Constructs a LEDSuiteTask with a specified task.
     *
     * @param task The task to be executed.
     * @since 1.0.0
     */
    LEDSuiteTask(final Runnable task) {
        this(task, -1, -1);
    }

    /**
     * Constructs a LEDSuiteTask with a specified task, ID, and period.
     *
     * @param task The task to be executed.
     * @param id The unique ID of the task.
     * @param period The period between task executions.
     * @since 1.0.0
     */
    LEDSuiteTask(final Runnable task, final int id, final long period) {
        this(task, id, period, null);
    }

    /**
     * Constructs a LEDSuiteTask with a specified task, ID, period, and YAML message.
     *
     * @param task The task to be executed.
     * @param id The unique ID of the task.
     * @param period The period between task executions.
     * @param yaml The YAML message associated with the task.
     * @since 1.0.0
     */
    LEDSuiteTask(final Runnable task, final int id, final long period, YAMLMessage yaml) {
        this.task = task;
        this.id = id;
        this.period = period;
        this.yaml = yaml;
    }

    /**
     * Returns the unique ID of the task.
     *
     * @return The task ID.
     * @since 1.0.0
     */
    public final int getTaskId() {
        return id;
    }

    /**
     * Indicates whether the task is synchronous.
     *
     * @return True if the task is synchronous, false otherwise.
     * @since 1.0.0
     */
    public boolean isSync() {
        return true;
    }

    /**
     * Executes the task.
     *
     * @since 1.0.0
     */
    public void run() {
        task.run();
    }

    /**
     * Returns the period between task executions.
     *
     * @return The period between executions.
     * @since 1.0.0
     */
    long getPeriod() {
        return period;
    }

    /**
     * Sets the period between task executions.
     *
     * @param period The period between executions.
     * @since 1.0.0
     */
    void setPeriod(long period) {
        this.period = period;
    }

    /**
     * Returns the next run time of the task.
     *
     * @return The next run time.
     * @since 1.0.0
     */
    long getNextRun() {
        return nextRun;
    }

    /**
     * Sets the next run time of the task.
     *
     * @param nextRun The next run time.
     * @since 1.0.0
     */
    void setNextRun(long nextRun) {
        this.nextRun = nextRun;
    }

    /**
     * Returns the next task in the sequence.
     *
     * @return The next task.
     * @since 1.0.0
     */
    LEDSuiteTask getNext() {
        return next;
    }

    /**
     * Sets the next task in the sequence.
     *
     * @param next The next task.
     * @since 1.0.0
     */
    void setNext(LEDSuiteTask next) {
        this.next = next;
    }

    /**
     * Returns the class of the task.
     *
     * @return The class of the task.
     * @since 1.0.0
     */
    Class<? extends Runnable> getTaskClass() {
        return task.getClass();
    }

    /**
     * Cancels the task.
     *
     * @since 1.0.0
     */
    public void cancel() {
        LEDSuite.getScheduler().cancelTask(id);
    }

    /**
     * This method properly sets the status to cancelled, synchronizing when required.
     *
     * @return false if it is a craft future task that has already begun execution, true otherwise
     * @since 1.0.0
     */
    boolean cancel0() {
        setPeriod(-2L);
        return true;
    }
}
