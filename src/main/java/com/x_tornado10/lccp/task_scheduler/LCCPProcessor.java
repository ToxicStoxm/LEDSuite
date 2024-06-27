package com.x_tornado10.lccp.task_scheduler;

import com.x_tornado10.lccp.LCCP;

import java.io.InputStream;

public class LCCPProcessor extends LCCPRunnable {
    private InputStream is;

    public synchronized LCCPTask runTaskAsynchronously(InputStream is) throws IllegalStateException {
        checkState();
        this.is = is;
        return setupId(LCCP.getScheduler().runTaskAsynchronously(this));
    }

    public synchronized LCCPTask runTask(InputStream is) throws IllegalStateException {
        checkState();
        this.is = is;
        return setupId(LCCP.getScheduler().runTask(this));
    }

    @Override
    public void run() {
        run(is);
    }

    public void run(InputStream is) {
    }

    @Override
    public void checkState() {
        super.checkState();
    }
}
