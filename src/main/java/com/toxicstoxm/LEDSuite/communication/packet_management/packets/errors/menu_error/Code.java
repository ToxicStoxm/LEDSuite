package com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.menu_error;

import org.jetbrains.annotations.NotNull;

public enum Code {

    PARSE_ERROR(0),
    KEY_MISSING(1),
    ILLEGAL_WIDGET_CONTENT(2),
    ILLEGAL_WIDGET_PROPERTY(3);

    final int value;

    Code(int value) {
        this.value = value;
    }

    static @NotNull Code fromValue(int value) {
        for (Code code : Code.values()) {
            if (code.value == value) return code;
        }
        throw new IllegalArgumentException("Couldn't find enum element for value '" + value + "'!");
    }
}