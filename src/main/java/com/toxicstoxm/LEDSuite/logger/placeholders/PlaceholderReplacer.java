package com.toxicstoxm.LEDSuite.logger.placeholders;

@FunctionalInterface
public interface PlaceholderReplacer {
    String onPlaceholderRequest(String stringWithPlaceholder, Placeholder placeholder);
}
