package com.toxicstoxm.LEDSuite.task_scheduler;

import com.toxicstoxm.LEDSuite.LEDSuite;
import com.toxicstoxm.LEDSuite.communication.network.Networking;
import com.toxicstoxm.LEDSuite.yaml_factory.YAMLMessage;

import java.util.UUID;

/**
 * The `LEDSuiteProcessor` class is an abstract class representing a task that processes a `YAMLMessage`
 * within the LEDSuite application.
 *
 * <p>This class extends `LEDSuiteRunnable` and provides methods to run the task
 * either asynchronously or synchronously with a given `YAMLMessage`.
 *
 * @since 1.0.0
 */
public abstract class LEDSuiteProcessor extends LEDSuiteRunnable {
    private YAMLMessage yaml;

    /**
     * Runs this task asynchronously within the LEDSuite scheduler with the provided `YAMLMessage`.
     *
     * @param yaml The YAML message to be processed.
     * @return A reference to the scheduled task.
     * @throws IllegalStateException If the task is in an illegal state to be run.
     * @since 1.0.0
     */
    public synchronized LEDSuiteTask runTaskAsynchronously(YAMLMessage yaml) throws IllegalStateException {
        checkState();
        this.yaml = yaml;
        return setupId(LEDSuite.getScheduler().runTaskAsynchronously(this));
    }

    /**
     * Runs this task synchronously within the LEDSuite scheduler with the provided `YAMLMessage`.
     *
     * @param yaml The YAML message to be processed.
     * @return A reference to the scheduled task.
     * @throws IllegalStateException If the task is in an illegal state to be run.
     * @since 1.0.0
     */
    public synchronized LEDSuiteTask runTask(YAMLMessage yaml) throws IllegalStateException {
        checkState();
        this.yaml = yaml;
        return setupId(LEDSuite.getScheduler().runTask(this));
    }

    /**
     * Executes the task by processing the `YAMLMessage`.
     *
     * <p>If an exception occurs during processing, it logs the error and uses the default fallback handler.
     *
     * @since 1.0.0
     */
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

    /**
     * The method to be implemented by subclasses to define the processing of the `YAMLMessage`.
     *
     * @param yaml The YAML message to be processed.
     * @throws DefaultHandleException If there is an error during processing.
     * @since 1.0.0
     */
    public abstract void run(YAMLMessage yaml) throws DefaultHandleException;

    /**
     * Checks the state of the task to ensure it is valid to run.
     *
     * <p>This method overrides the `checkState` method in `LEDSuiteRunnable`.
     *
     * @since 1.0.0
     */
    @Override
    public void checkState() {
        super.checkState();
    }

    /**
     * The `DefaultHandleException` is thrown when the default handler needs to process the `YAMLMessage`.
     *
     * @since 1.0.0
     */
    public static class DefaultHandleException extends Exception {
        /**
         * Constructs a new `DefaultHandleException` with the specified detail message.
         *
         * @param message The detail message.
         * @since 1.0.0
         */
        public DefaultHandleException(String message) {
            super(message);
        }
    }
}