package com.toxicstoxm.LEDSuite.task_scheduler;

import com.toxicstoxm.LEDSuite.LEDSuite;
import com.toxicstoxm.LEDSuite.communication.network.Networking;
import com.toxicstoxm.LEDSuite.yaml_factory.YAMLMessage;

import java.util.UUID;

public abstract class LEDSuiteProcessor extends LEDSuiteRunnable {
    private YAMLMessage yaml;

    public synchronized LEDSuiteTask runTaskAsynchronously(YAMLMessage yaml) throws IllegalStateException {
        checkState();
        this.yaml = yaml;
        return setupId(LEDSuite.getScheduler().runTaskAsynchronously(this));
    }

    public synchronized LEDSuiteTask runTask(YAMLMessage yaml) throws IllegalStateException {
        checkState();
        this.yaml = yaml;
        return setupId(LEDSuite.getScheduler().runTask(this));
    }

    @Override
    public void run() {
        try {
            run(yaml);
        } catch (DefaultHandleException e) {
            UUID networkID = yaml.getNetworkID();
            String message =
                    "LEDSuite Processor ID[" +
                            this.getTaskId() +
                            "] rejected input, message [" +
                            e.getMessage() +
                            "]. Using default fallback handler instead!";
            LEDSuite.logger.debug(
                    (
                            networkID != null ?
                                    "[" + networkID + "] " :
                                    "[INVALID NETWORK ID] "
                    ) + message
            );
            Networking.Communication.defaultHandle(yaml);
        }
    }

    public abstract void run(YAMLMessage yaml) throws DefaultHandleException;

    public static class DefaultHandleException extends Exception {
        public DefaultHandleException(String message) {
            super(message);
        }
    }

    @Override
    public void checkState() {
        super.checkState();
    }
}
