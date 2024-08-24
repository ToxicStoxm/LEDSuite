package com.toxicstoxm.LEDSuite.logger.colors;

import java.awt.*;

public class ColorConverter {
    public static Color getColorFromHex(String hex) {
        return Color.decode(hex);
    }
}
