package com.toxicstoxm.LEDSuite.communication.packet_management.packets.enums;

/**
 * Possible lid states.
 * @since 1.0.0
 */
public enum LidState {
    open,
    closed;

    LidState() {}

    public static LidState fromBool(boolean lidState) {
        return lidState ? closed : open;
    }

    public boolean asBool() {
        return this.name().equals(closed.name());
    }
}
