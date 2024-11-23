package com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.menu_error;

import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Severity levels for {@link MenuErrorPacket}.
 * Represents different levels of error severity in the system.
 * @since 1.0.0
 */
@Getter
public enum Severity {
    UNDEFINED(0),  // Default fallback severity
    MILD(1),
    MEDIUM(2),
    SEVERE(3),
    FATAL(4);

    private final int value;
    private static final Map<Integer, Severity> VALUE_TO_SEVERITY = new HashMap<>();

    static {
        // Initialize map for fast lookup
        for (Severity severity : Severity.values()) {
            VALUE_TO_SEVERITY.put(severity.value, severity);
        }
    }

    Severity(int value) {
        this.value = value;
    }

    /**
     * Converts an integer value to the corresponding Severity level.
     * @param value the integer value to convert
     * @return the corresponding Severity enum, or UNDEFINED if the value is not recognized
     */
    static @NotNull Severity fromValue(int value) {
        return VALUE_TO_SEVERITY.getOrDefault(value, UNDEFINED);
    }

    /**
     * Provides a human-readable string for the Severity level.
     * @return the name of the severity level
     */
    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return switch (this) {
            case MILD -> "Mild";
            case MEDIUM -> "Medium";
            case SEVERE -> "Severe";
            case FATAL -> "Fatal";
            default -> "Undefined";
        };
    }
}
