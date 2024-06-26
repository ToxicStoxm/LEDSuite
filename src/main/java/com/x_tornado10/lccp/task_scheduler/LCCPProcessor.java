package com.x_tornado10.lccp.task_scheduler;

import com.x_tornado10.lccp.LCCP;

import java.io.InputStream;

public abstract class LCCPProcessor extends LCCPRunnable {
    private InputStream is;
    private int taskId = -1;

    public synchronized LCCPTask runTaskAsynchronously(InputStream is) throws IllegalStateException {
        this.is = is;
        checkState();
        return setupId(LCCP.getScheduler().runTaskAsynchronously(this));
    }

    private void checkState() {
        if (taskId != -1) {
            throw new IllegalStateException("Already scheduled as " + taskId);
        }
    }

    private LCCPTask setupId(final LCCPTask task) {
        this.taskId = task.getTaskId();
        return task;
    }

    @Override
    public void run() {
        run(is);
    }

    public void run(InputStream is) {

    }
}
