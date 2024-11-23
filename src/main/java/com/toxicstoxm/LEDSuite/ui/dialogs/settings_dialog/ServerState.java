package com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog;

/**
 * Enum representing the various states of server connectivity.
 * <p>
 * The {@code ServerState} enum is used to track and represent the current
 * connection status of a server, indicating whether the server is connected,
 * disconnected, in the process of connecting, or disconnecting.
 * </p>
 *
 * @since 1.0.0
 */
public enum ServerState {

    /**
     * Represents the state when the server is successfully connected.
     */
    CONNECTED,

    /**
     * Represents the state when the server is not connected.
     */
    DISCONNECTED,

    /**
     * Represents the state when the server is in the process of connecting.
     */
    CONNECTING,

    /**
     * Represents the state when the server is in the process of disconnecting.
     */
    DISCONNECTING
}
