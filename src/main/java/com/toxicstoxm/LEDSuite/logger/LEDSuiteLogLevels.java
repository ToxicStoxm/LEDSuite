package com.toxicstoxm.LEDSuite.logger;

import com.toxicstoxm.YAJL.core.level.LogLevel;
import lombok.Getter;

import java.awt.*;

@Getter
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
}
