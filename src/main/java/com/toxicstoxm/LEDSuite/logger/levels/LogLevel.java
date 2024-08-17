package com.toxicstoxm.LEDSuite.logger.levels;

import java.awt.*;

public interface LogLevel {
    boolean isEnabled();
    String getText();
    Color getColor();
}