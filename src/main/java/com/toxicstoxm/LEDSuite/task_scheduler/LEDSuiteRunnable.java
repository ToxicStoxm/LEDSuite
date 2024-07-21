package com.toxicstoxm.LEDSuite.task_scheduler;

import com.toxicstoxm.LEDSuite.LEDSuite;
import com.toxicstoxm.LEDSuite.time.TickingSystem;

public abstract class LEDSuiteRunnable implements Runnable {
    private int taskId = -1;

    public synchronized void cancel() throws IllegalStateException {
        LEDSuite.getScheduler().cancelTask(getTaskId());
    }

    public synchronized LEDSuiteTask runTask() throws IllegalStateException {
        checkState();
        return setupId(LEDSuite.getScheduler().runTask(this));
    }

    public synchronized LEDSuiteTask runTaskAsynchronously() throws IllegalStateException {
        checkState();
        return setupId(LEDSuite.getScheduler().runTaskAsynchronously(this));
    }

    public synchronized LEDSuiteTask runTaskLater(long delay) throws IllegalStateException {
        checkState();
        return setupId(LEDSuite.getScheduler().runTaskLater(this, TickingSystem.translate(delay)));
    }

    public synchronized LEDSuiteTask runTaskLaterAsynchronously(long delay) throws IllegalStateException {
        checkState();
        return setupId(LEDSuite.getScheduler().runTaskLaterAsynchronously(this, TickingSystem.translate(delay)));
    }

    public synchronized LEDSuiteTask runTaskTimer(long delay, long period) throws IllegalStateException {
        checkState();
        return setupId(LEDSuite.getScheduler().runTaskTimer(this, TickingSystem.translate(delay), TickingSystem.translate(period)));
    }

    public synchronized LEDSuiteTask runTaskTimerAsynchronously(long delay, long period) throws IllegalStateException {
        checkState();
        return setupId(LEDSuite.getScheduler().runTaskTimerAsynchronously(this, TickingSystem.translate(delay), TickingSystem.translate(period)));
    }

    public synchronized int getTaskId() throws IllegalStateException {
        final int id = taskId;
        if (id == -1) {
            throw new IllegalStateException("Not scheduled yet");
        }
        return id;
    }

    protected void checkState() {
        if (taskId != -1) {
            throw new IllegalStateException("Already scheduled as " + taskId);
        }
    }

    protected LEDSuiteTask setupId(final LEDSuiteTask task) {
        this.taskId = task.getTaskId();
        return task;
    }
}
