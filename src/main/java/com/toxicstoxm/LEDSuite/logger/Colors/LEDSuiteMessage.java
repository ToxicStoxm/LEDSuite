package com.toxicstoxm.LEDSuite.logger.Colors;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class LEDSuiteMessage implements ColoredMessage {

    private static final String RESET = "\33[m";
    private static final String COLOR = "\33[38;2;" + "%RED%" + ";" + "%GREEN%" + ";" + "%BLUE%";
    private static final String MESSAGE = "m" + "%MESSAGE%";
    private boolean colored = false;

    private static String getColorCode(@NotNull Color color) {
        return COLOR
                .replace("%RED%", String.valueOf(color.getRed()))
                .replace("%GREEN%", String.valueOf(color.getGreen()))
                .replace("%BLUE%", String.valueOf(color.getBlue()));
    }

    private static String getMessageCode(@NotNull String message) {
        return MESSAGE
                .replace("%MESSAGE%", message);
    }

    StringBuilder message;

    public LEDSuiteMessage() {
        message = new StringBuilder();
    }
    public LEDSuiteMessage(@NotNull String base) {
        message = new StringBuilder(base);
    }
    public LEDSuiteMessage(@NotNull Color base) {
        message = new StringBuilder().append(getColorCode(base));
    }

    public static LEDSuiteMessage builder() {
        return new LEDSuiteMessage();
    }

    /**
     * Doesn't reset color after the message! If you want that use {@link #colorMessage(String, Color)}
     * @param baseColor Color to use
     * @param baseMessage Message to color
     */
    public LEDSuiteMessage(@NotNull Color baseColor, @NotNull String baseMessage) {
        message = new StringBuilder().append(getColorCode(baseColor)).append(getMessageCode(baseMessage));
    }

    @Override
    public String colorMessage(@NonNull String message, @NonNull Color color) {
        return new LEDSuiteMessage(color, message).reset().getMessage();
    }

    @Override
    public LEDSuiteMessage color(@NotNull Color color) {
        colored = true;
        message.append(getColorCode(color));
        return this;
    }

    @Override
    public LEDSuiteMessage text(@NonNull String string) {
        message.append(colored ? getMessageCode(string) : string);
        return this;
    }

    @Override
    public LEDSuiteMessage reset() {
        colored = false;
        message.append(RESET);
        return this;
    }

    @Override
    public String getMessage() {
        return message.toString();
    }

    @Override
    public String toString() {
        if (message != null && message.isEmpty()) {
            return message.toString();
        }
        return super.toString();
    }

    public String build() {
        return this.getMessage();
    }
}
