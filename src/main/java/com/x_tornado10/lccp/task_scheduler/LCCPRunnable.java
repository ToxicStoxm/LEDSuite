package com.x_tornado10.lccp.task_scheduler;

import com.x_tornado10.lccp.LCCP;

public abstract class LCCPRunnable implements Runnable {
    private int taskId = -1;

    public synchronized void cancel() throws IllegalStateException {
        LCCP.getScheduler().cancelTask(getTaskId());
    }

    public synchronized LCCPTask runTask() throws IllegalStateException {
        checkState();
        return setupId(LCCP.getScheduler().runTask(this));
    }

    public synchronized LCCPTask runTaskAsynchronously() throws IllegalStateException {
        checkState();
        return setupId(LCCP.getScheduler().runTaskAsynchronously(this));
    }

    public synchronized LCCPTask runTaskLater(long delay) throws IllegalStateException {
        checkState();
        return setupId(LCCP.getScheduler().runTaskLater(this, translate(delay)));
    }

    public synchronized LCCPTask runTaskLaterAsynchronously(long delay) throws IllegalStateException {
        checkState();
        return setupId(LCCP.getScheduler().runTaskLaterAsynchronously(this, translate(delay)));
    }

    public synchronized LCCPTask runTaskTimer(long delay, long period) throws IllegalStateException {
        checkState();
        return setupId(LCCP.getScheduler().runTaskTimer(this, translate(delay), translate(period)));
    }

    public synchronized LCCPTask runTaskTimerAsynchronously(long delay, long period) throws IllegalStateException {
        checkState();
        return setupId(LCCP.getScheduler().runTaskTimerAsynchronously(this, translate(delay), translate(period)));
    }

    public synchronized int getTaskId() throws IllegalStateException {
        final int id = taskId;
        if (id == -1) {
            throw new IllegalStateException("Not scheduled yet");
        }
        return id;
    }

    public synchronized long translate(long val) {
        return val / 10;
    }

    protected void checkState() {
        if (taskId != -1) {
            throw new IllegalStateException("Already scheduled as " + taskId);
        }
    }

    protected LCCPTask setupId(final LCCPTask task) {
        this.taskId = task.getTaskId();
        return task;
    }
}