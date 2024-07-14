package com.toxicstoxm.lccp.task_scheduler;

import com.toxicstoxm.lccp.LCCP;
import com.toxicstoxm.lccp.communication.network.Networking;
import com.toxicstoxm.lccp.yaml_factory.YAMLMessage;

import java.util.UUID;


public abstract class LCCPArgumentProcessor extends LCCPRunnable {
    private String argument;
    private ArgumentValidationCallback callback;

    public synchronized LCCPTask runTaskAsynchronously(String argument, ArgumentValidationCallback callback) throws IllegalStateException {
        checkState();
        this.argument = argument;
        this.callback = callback;
        return setupId(LCCP.getScheduler().runTaskAsynchronously(this));
    }

    public synchronized LCCPTask runTask(String argument, ArgumentValidationCallback callback) throws IllegalStateException {
        checkState();
        this.argument = argument;
        this.callback = callback;
        return setupId(LCCP.getScheduler().runTask(this));
    }

    @Override
    public void run() {
        run(argument, callback);
    }

    public void run(String argument, ArgumentValidationCallback callback) {
    }

    @Override
    public void checkState() {
        super.checkState();
    }
    public interface ArgumentValidationCallback {
        void onValidationComplete(boolean isValid, String message);
    }
}
