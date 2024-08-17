package com.toxicstoxm.LEDSuite.logger.colors;

import lombok.NonNull;

import java.awt.*;

public interface ColoredMessage {
    default String colorMessage(@NonNull String message, @NonNull Color color) {
        return "\33[38;2;" + color.getRed() + ";" + color.getGreen() + ";" + color.getBlue() + "m" + message + "\33[m";
    }
    default ColoredMessage color(@NonNull Color color) {
        throw new UnsupportedOperationException("This colors implementation doesn't provide a message factory!");
    }
    default ColoredMessage text(@NonNull String string) {
        throw new UnsupportedOperationException("This colors implementation doesn't provide a message factory!");
    }
    default ColoredMessage reset() {
        throw new UnsupportedOperationException("This colors implementation doesn't provide a message factory!");
    }
    default String getMessage() {
        throw new UnsupportedOperationException("This colors implementation doesn't provide a message factory!");
    }
}
