package com.x_tornado10.lccp.task_scheduler;

import com.x_tornado10.lccp.LCCP;
import com.x_tornado10.lccp.communication.network.Networking;
import com.x_tornado10.lccp.yaml_factory.YAMLMessage;
import com.x_tornado10.lccp.yaml_factory.YAMLSerializer;

import java.util.UUID;


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
        try {
            run(yaml);
        } catch (DefaultHandleException e) {
            UUID networkID = yaml.getNetworkID();
            String message =
                    "LCCP Processor ID[" +
                            this.getTaskId() +
                            "] rejected input, message [" +
                            e.getMessage() +
                            "]. Using default fallback handler instead!";
            LCCP.logger.debug(
                    (
                            networkID != null ?
                            "[" + networkID + "] " :
                            "[INVALID NETWORK ID] "
                    ) + message
            );
            Networking.Communication.defaultHandle(yaml);
        }
    }

    public void run(YAMLMessage yaml) throws DefaultHandleException {
    }

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
