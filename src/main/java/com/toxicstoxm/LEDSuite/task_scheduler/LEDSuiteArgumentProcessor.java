package com.toxicstoxm.LEDSuite.task_scheduler;

import com.toxicstoxm.LEDSuite.LEDSuite;

public abstract class LEDSuiteArgumentProcessor extends LEDSuiteRunnable {
    private String argument;
    private ArgumentValidationCallback callback;

    public synchronized LEDSuiteTask runTaskAsynchronously(String argument, ArgumentValidationCallback callback) throws IllegalStateException {
        checkState();
        this.argument = argument;
        this.callback = callback;
        return setupId(LEDSuite.getScheduler().runTaskAsynchronously(this));
    }

    public synchronized LEDSuiteTask runTask(String argument, ArgumentValidationCallback callback) throws IllegalStateException {
        checkState();
        this.argument = argument;
        this.callback = callback;
        return setupId(LEDSuite.getScheduler().runTask(this));
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
        void onValidationComplete(boolean isValid, String message, ArgumentExtraCommand argCmd);
    }
    public enum ArgumentExtraCommand {
        none,
        adwaita,
        help
    }
}
