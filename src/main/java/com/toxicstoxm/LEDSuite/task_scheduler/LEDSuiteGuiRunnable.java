package com.toxicstoxm.LEDSuite.task_scheduler;

import com.toxicstoxm.LEDSuite.LEDSuite;
import org.gnome.glib.GLib;

/**
 * The `LEDSuiteGuiRunnable` class is an abstract class that represents a task
 * which can be run within the GUI context of the LEDSuite application.
 *
 * <p>This class extends `LEDSuiteRunnable` and provides methods to run the task
 * either asynchronously or synchronously within the GUI context.
 *
 * @since 1.0.0
 */
public abstract class LEDSuiteGuiRunnable extends LEDSuiteRunnable {

    /**
     * Runs this task asynchronously within the LEDSuite scheduler.
     *
     * @return A reference to the scheduled task.
     * @throws IllegalStateException If the task is in an illegal state to be run.
     * @since 1.0.0
     */
    public synchronized LEDSuiteTask runTaskAsynchronously() throws IllegalStateException {
        checkState();
        return setupId(LEDSuite.getScheduler().runTaskAsynchronously(this));
    }

    /**
     * Runs this task synchronously within the LEDSuite scheduler.
     *
     * @return A reference to the scheduled task.
     * @throws IllegalStateException If the task is in an illegal state to be run.
     * @since 1.0.0
     */
    public synchronized LEDSuiteTask runTask() throws IllegalStateException {
        checkState();
        return setupId(LEDSuite.getScheduler().runTask(this));
    }

    /**
     * Checks the state of the task to ensure it is valid to run.
     *
     * <p>This method overrides the `checkState` method in `LEDSuiteRunnable`.
     *
     * @since 1.0.0
     */
    @Override
    public void checkState() {
        super.checkState();
    }

    /**
     * Runs the task within the GUI context using GLib's idle add mechanism.
     *
     * <p>This method schedules the `processGui` method to be run when the GUI is idle.
     *
     * @since 1.0.0
     */
    @Override
    public void run() {
        GLib.idleAddOnce(this::processGui);
    }

    /**
     * The method to be implemented by subclasses to define the GUI-related processing.
     *
     * <p>This method will be called within the GUI context when the task is run.
     *
     * @since 1.0.0
     */
    public abstract void processGui();
}
