package com.toxicstoxm.LEDSuite.logger.areas;

import java.awt.*;
import java.util.List;

public interface LogArea {
    String getName();
    List<String> getParents();
    Color getColor();
}
