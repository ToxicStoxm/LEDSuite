package com.toxicstoxm.LEDSuite.communication.websocket;

/**
 * Typically indicates that some preparations are finished.
 * Can be useful for initialization logic.
 * @since 1.0.0
 */
public interface IsReadyCallback {
    boolean isReady();
}
