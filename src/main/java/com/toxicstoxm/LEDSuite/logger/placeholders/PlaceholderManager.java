package com.toxicstoxm.LEDSuite.logger.placeholders;

public interface PlaceholderManager {
    void registerPlaceholder(Placeholder placeholder, PlaceholderReplacer replacer);
    String processPlaceholders(String stringWithPlaceholders);
}
