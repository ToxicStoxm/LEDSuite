package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.status_reply;

/**
 * Enum representing the possible states of a lid.
 * <p>This enum is used to describe the current state of a lid, which can be one of the following:</p>
 * <ul>
 *     <li><b>open</b> - The lid is in open.</li>
 *     <li><b>closed</b> - The lid is closed.</li>
 * </ul>
 *
 * @since 1.0.0
 */
public enum LidState {
    /**
     * Indicates that the lid is open.
     */
    open,

    /**
     * Indicates that the lid is closed.
     */
    closed;

    // Default constructor, not required but explicitly defined for clarity.
    LidState() {}

    /**
     * Converts a boolean value to a corresponding {@link LidState}.
     * <p>If the boolean is true, it returns {@link LidState#closed}, otherwise {@link LidState#open}.</p>
     *
     * @param lidState boolean representing the lid state (true for closed, false for open)
     * @return corresponding {@link LidState}
     */
    public static LidState fromBool(boolean lidState) {
        return lidState ? closed : open;
    }

    /**
     * Converts the current {@link LidState} to a boolean value.
     * <p>This method returns true if the lid is closed and false if it is open.</p>
     *
     * @return boolean representation of the lid state (true for closed, false for open)
     */
    public boolean asBool() {
        return this.name().equals(closed.name());
    }
}
