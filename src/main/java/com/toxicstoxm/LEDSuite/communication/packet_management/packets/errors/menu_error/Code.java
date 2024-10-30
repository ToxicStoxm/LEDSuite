package com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.menu_error;

public enum Code {
    UNDEFINED(0),
    MILD(1),
    MEDIUM(2),
    SEVERE(3),
    FATAL(4);

    final int value;

    Code(int value) {
        this.value = value;
    }

    static Code fromValue(int value) {
        for (Code code : Code.values()) {
            if (code.value == value) return code;
        }
        throw new IllegalArgumentException("Couldn't find enum element for value '" + value + "'!");
    }

}
