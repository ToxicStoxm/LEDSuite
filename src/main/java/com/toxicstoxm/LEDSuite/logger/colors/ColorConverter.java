package com.toxicstoxm.LEDSuite.logger.colors;

import java.awt.*;

public class ColorConverter {
    public static Color getColorFromHex(String hex) {
        return Color.decode(hex);
    }
    public static Color mixColors(Color... colors) {
        float ratio = 1f / ((float) colors.length);
        int r = 0, g = 0, b = 0, a = 0;
        for (Color color : colors) {
            r += (int) (color.getRed() * ratio);
            g += (int) (color.getGreen() * ratio);
            b += (int) (color.getBlue() * ratio);
            a += (int) (color.getAlpha() * ratio);
        }
        return new Color(r, g, b, a);
    }
}
