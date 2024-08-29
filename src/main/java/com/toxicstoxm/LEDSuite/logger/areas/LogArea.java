package com.toxicstoxm.LEDSuite.logger.areas;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public interface LogArea {
    default String getName() {
        return Arrays.stream(Arrays.stream(getClass().getName().split("\\.")).toList().getLast().split("\\$")).toList().getLast();
    }
    void setColor(Color color);
    boolean isEnabled();
    void setEnabled(boolean enabled);
    void setParents(List<String> parents);
    List<String> getParents();
    Color getColor();
}
