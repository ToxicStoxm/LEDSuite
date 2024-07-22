package com.toxicstoxm.LEDSuite.task_scheduler;

import com.toxicstoxm.LEDSuite.LEDSuite;
import org.gnome.glib.GLib;

public abstract class LEDSuiteGuiRunnable extends LEDSuiteRunnable {

    public synchronized LEDSuiteTask runTaskAsynchronously() throws IllegalStateException {
        checkState();
        return setupId(LEDSuite.getScheduler().runTaskAsynchronously(this));
    }

    public synchronized LEDSuiteTask runTask() throws IllegalStateException {
        checkState();
        return setupId(LEDSuite.getScheduler().runTask(this));
    }

    @Override
    public void checkState() {
        super.checkState();
    }

    @Override
    public void run() {
        GLib.idleAddOnce(this::processGui);
    }
    public abstract void processGui();
}
