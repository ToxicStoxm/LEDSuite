package com.toxicstoxm.LEDSuite.tools;

import org.jetbrains.annotations.NotNull;

public interface NullSaveGetter<T> {
    default @NotNull T getInstance() {
        return isAvailable() ? value() : defaultValue();
    }
    T value();
    default boolean isAvailable() {
        return value() != null;
    }
    @NotNull T defaultValue();
}
