package com.toxicstoxm.LEDSuite.logger.levels;


import java.awt.*;

public class LEDSuiteLogLevel implements LogLevel {
    private final boolean enabled;
    private final String text;
    private final Color color;

    public LEDSuiteLogLevel(boolean enabled, String text, Color color) {
        this.enabled = enabled;
        this.text = text;
        this.color = color;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public Color getColor() {
        return color;
    }
}
