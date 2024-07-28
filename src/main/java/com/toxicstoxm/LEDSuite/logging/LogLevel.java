package com.toxicstoxm.LEDSuite.logging;

/**
 * Interface representing a logging level.
 *
 * <p>Implementations of this interface define the behavior of different log levels, including
 * whether they are enabled based on the current configuration.</p>
 *
 * @since 1.0.0
 */
public interface LogLevel {

    /**
     * Checks if the current log level is enabled.
     *
     * <p>This method determines whether log messages of this level should be recorded
     * or displayed, depending on the log level configuration.</p>
     *
     * @return {@code true} if this log level is enabled; {@code false} otherwise.
     * @since 1.0.0
     */
    boolean isEnabled();
}