package com.toxicstoxm.LEDSuite.logger.placeholders;

public interface Placeholder {
    String getText();
    char getRegex();
    default String getPlaceholder() {
        return getRegex() + getText() + getRegex();
    }
}
