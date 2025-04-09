package com.toxicstoxm.LEDSuite.logger;

import com.toxicstoxm.YAJL.level.LogLevel;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public enum LEDSuiteLogLevels implements LogLevel {
    COMMUNICATION_IN("COMMUNICATION_IN", Color.GRAY, -30),
    COMMUNICATION_OUT("COMMUNICATION_OUT", Color.GRAY, -31);

    private final String name;
    private final Color color;
    private final int level;

    LEDSuiteLogLevels(String name, Color color, int level) {
        this.name = name;
        this.color = color;
        this.level = level;
    }

    @Contract(pure = true)
    @Override
    public @NotNull String getName() {
        return "";
    }

    @Contract(pure = true)
    @Override
    public @Nullable Color getColor() {
        return null;
    }

    @Override
    public int getLevel() {
        return 0;
    }
}
