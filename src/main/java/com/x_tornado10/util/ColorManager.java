package com.x_tornado10.util;

import com.x_tornado10.Events.EventListener;
import com.x_tornado10.Events.Events.Event;
import com.x_tornado10.Main;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ColorManager {

    // color layers (raw codes)
    public String l0_raw = "";
    public String l1_raw = "";
    public String l2_raw = "";
    public String l3_raw = "";
    public String l4_raw = "";
    public String l5_raw = "";
    public String l6_raw = "";
    public String l7_raw = "";

    // color layers (color class)
    public Color l0 = new Color(0,0,0);
    public Color l1 = new Color(10,10,10);
    public Color l2 = new Color(20,20,20);
    public Color l3 = new Color(30,30,30);
    public Color l4 = new Color(40,40,40);
    public Color l5 = new Color(50,50,50);
    public Color l6 = new Color(60,60,60);
    public Color l7 = new Color(70,70,70);

    // color for important user info
    public static final Color info = new Color(255, 0, 0, 192);

    // get primary color from config
    private String getPrimary() {
        return (String) (Main.settings.isDarkM() ? Main.settings.getDarkModePrim(true) : Main.settings.getLightModePrim(true));
    }
    // get secondary color from config
    private String getSecondary() {
        return (String) (Main.settings.isDarkM() ? Main.settings.getDarkModeSec(true) : Main.settings.getLightModeSec(true));
    }

    public ColorManager() {
        revalidate();
    }
    // recalculate colors
    public void revalidate() {
        Main.logger.debug("Recalculating colors...");
        String co1 = getPrimary();
        String co2 = getSecondary();

        List<String> colors = generateGradient(co1, co2, 8);

        String c0 = colors.get(0);
        l0_raw = c0;
        l0 = Color.decode(c0);
        Main.logger.debug("Layer1: " + l0_raw);

        String c1 = colors.get(1);
        l1_raw = c1;
        l1 = Color.decode(c1);
        Main.logger.debug("Layer2: " + l1_raw);

        String c2 = colors.get(2);
        l2_raw = c2;
        l2 = Color.decode(c2);
        Main.logger.debug("Layer3: " + l2_raw);

        String c3 = colors.get(3);
        l3_raw = c3;
        l3 = Color.decode(c3);
        Main.logger.debug("Layer4: " + l3_raw);

        String c4 = colors.get(4);
        l4_raw = c4;
        l4 = Color.decode(c4);
        Main.logger.debug("Layer5: " + l4_raw);

        String c5 = colors.get(5);
        l5_raw = c5;
        l5 = Color.decode(c5);
        Main.logger.debug("Layer6: " + l5_raw);

        String c6 = colors.get(6);
        l6_raw = c6;
        l6 = Color.decode(c6);
        Main.logger.debug("Layer7: " + l6_raw);

        String c7 = colors.get(7);
        l7_raw = c7;
        l7 = Color.decode(c7);
        Main.logger.debug("Layer8: " + l7_raw);

        Main.logger.debug("Successfully recalculated colors!");
    }

    // gradient generator
    public static ArrayList<String> generateGradient(String c1, String c2, int numColors) {
        ArrayList<String> gradientColors = new ArrayList<>();

        // Removing '#' symbol from the input strings
        String color1 = c1.replace("#", "");
        String color2 = c2.replace("#", "");

        // Convert hexadecimal colors to RGB
        int color1_r = Integer.parseInt(color1.substring(0, 2), 16);
        int color1_g = Integer.parseInt(color1.substring(2, 4), 16);
        int color1_b = Integer.parseInt(color1.substring(4, 6), 16);

        int color2_r = Integer.parseInt(color2.substring(0, 2), 16);
        int color2_g = Integer.parseInt(color2.substring(2, 4), 16);
        int color2_b = Integer.parseInt(color2.substring(4, 6), 16);

        // Calculate the overall change in each RGB component across the entire gradient
        double delta_r = (double) (color2_r - color1_r) / (numColors + 1);
        double delta_g = (double) (color2_g - color1_g) / (numColors + 1);
        double delta_b = (double) (color2_b - color1_b) / (numColors + 1);

        // Generate the gradient colors
        for (int i = 0; i < numColors; i++) {
            int new_r = (int) (color1_r + delta_r * (i + 1));
            int new_g = (int) (color1_g + delta_g * (i + 1));
            int new_b = (int) (color1_b + delta_b * (i + 1));

            // Convert RGB to hexadecimal
            String hexColor = String.format("#%02X%02X%02X", new_r, new_g, new_b);
            gradientColors.add(hexColor);
        }

        return gradientColors;
    }


    public static Color adjustColorForIndistinguishability(Color textColor, Color backgroundColor) {
        return adjustColorForIndistinguishability(textColor, backgroundColor, 50, false);
    }

    public static Color adjustColorForIndistinguishability(Color textColor, Color backgroundColor, int range, boolean lighten) {
        // Calculate the Euclidean distance between the colors in RGB space
        double distance = Math.sqrt(
                Math.pow(textColor.getRed() - backgroundColor.getRed(), 2) +
                        Math.pow(textColor.getGreen() - backgroundColor.getGreen(), 2) +
                        Math.pow(textColor.getBlue() - backgroundColor.getBlue(), 2)
        );
        Main.logger.debug("Calculating Background: Background: " + getRGBDescriptionFromColor(backgroundColor) + " -- Text: " + getRGBDescriptionFromColor(backgroundColor));
        // Check if the distance is within the specified range
        if (distance <= range) {
            if (!lighten) {
                // If within range, adjust the target color to be more indistinguishable from the base color
                int opposingRed = 255 - textColor.getRed();
                int opposingGreen = 255 - textColor.getGreen();
                int opposingBlue = 255 - textColor.getBlue();
                Color result = new Color(opposingRed, opposingGreen, opposingBlue);
                Main.logger.debug("Background color too similar (distance in color space: " + (double) Math.round(distance * 1000) / 1000 + ")");

                Main.logger.debug("Negating background color: " + getRGBDescriptionFromColor(result));
                return result;
            } else {
                int factor = range / 2;

                int red = backgroundColor.getRed() + factor;
                int green = backgroundColor.getGreen() + factor;
                int blue = backgroundColor.getBlue() + factor;

                int r = Math.min(red, 255);
                int g = Math.min(green, 255);
                int b = Math.min(blue, 255);
                return new Color(r, g, b);

            }
        } else {
            // If not within range, return the original target color
            Main.logger.debug("Background color is indistinguishable enough! (distance in color space: " + (double) Math.round(distance * 1000) / 1000 + ")");
            return backgroundColor;
        }
    }

    public static String getRGBDescriptionFromColor(Color c) {
        return  "'" + c.getRed() + ", " + c.getGreen() + ", " + c.getBlue() + "'" ;
    }
}
