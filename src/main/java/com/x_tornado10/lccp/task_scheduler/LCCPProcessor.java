package com.x_tornado10.lccp.task_scheduler;

import com.x_tornado10.lccp.LCCP;
import com.x_tornado10.lccp.yaml_factory.YAMLMessage;


public abstract class LCCPProcessor extends LCCPRunnable {
    private YAMLMessage yaml;

    public synchronized LCCPTask runTaskAsynchronously(YAMLMessage yaml) throws IllegalStateException {
        checkState();
        this.yaml = yaml;
        return setupId(LCCP.getScheduler().runTaskAsynchronously(this));
    }

    public synchronized LCCPTask runTask(YAMLMessage yaml) throws IllegalStateException {
        checkState();
        this.yaml = yaml;
        return setupId(LCCP.getScheduler().runTask(this));
    }

    @Override
    public void run() {
        run(yaml);
    }

    public void run(YAMLMessage yaml) {
    }

    @Override
    public void checkState() {
        super.checkState();
    }
}
