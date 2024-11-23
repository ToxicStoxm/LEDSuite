package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.status_reply;

/**
 * Enum representing the possible states of a file in the system.
 * <p>This enum is used to describe the current status of a file or animation, which can be one of the following:</p>
 * <ul>
 *     <li><b>playing</b> - The current animation is playing.</li>
 *     <li><b>paused</b> - The current animation is paused.</li>
 *     <li><b>idle</b> - The server is in an idle state, no file is currently loaded.</li>
 * </ul>
 *
 * @since 1.0.0
 */
public enum FileState {
    /**
     * Indicates that the current animation is playing.
     */
    playing,

    /**
     * Indicates that the current animation is paused.
     */
    paused,

    /**
     * Indicates that the server is in an idle state, and no file is currently loaded.
     */
    idle
}
