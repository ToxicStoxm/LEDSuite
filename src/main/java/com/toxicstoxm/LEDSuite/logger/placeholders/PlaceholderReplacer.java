package com.toxicstoxm.LEDSuite.logger.placeholders;

@FunctionalInterface
public interface PlaceholderReplacer {
    String onPlaceholderRequest(Placeholder placeholder);
}
