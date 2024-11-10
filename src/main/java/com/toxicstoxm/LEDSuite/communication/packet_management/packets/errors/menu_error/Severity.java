package com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.menu_error;

import org.jetbrains.annotations.NotNull;

public enum Severity {
    UNDEFINED(0),
    MILD(1),
    MEDIUM(2),
    SEVERE(3),
    FATAL(4);

    final int value;

    Severity(int value) {
        this.value = value;
    }

    static @NotNull Severity fromValue(int value) {
        for (Severity severity : Severity.values()) {
            if (severity.value == value) return severity;
        }
        return UNDEFINED;
    }

}
