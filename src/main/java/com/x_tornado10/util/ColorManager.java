package com.x_tornado10.util;

import com.x_tornado10.Events.EventListener;
import com.x_tornado10.Main;

import java.util.ArrayList;
import java.util.List;

public class ColorManager implements EventListener {
    // color layers
    public String l0 = "";
    public String l1 = "";
    public String l2 = "";
    public String l3 = "";
    public String l4 = "";
    public String l5 = "";
    public String l6 = "";
    public String l7 = "";

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
    private void revalidate() {
        Main.logger.info("Recalculating colors...");
        String c1 = getPrimary();
        String c2 = getSecondary();

        List<String> colors = generateGradient(c1, c2, 8);
        l0 = colors.get(0);
        Main.logger.info("Layer1: " + l0);
        l1 = colors.get(1);
        Main.logger.info("Layer2: " + l1);
        l2 = colors.get(2);
        Main.logger.info("Layer3: " + l2);
        l3 = colors.get(3);
        Main.logger.info("Layer4: " + l3);
        l4 = colors.get(4);
        Main.logger.info("Layer5: " + l4);
        l5 = colors.get(5);
        Main.logger.info("Layer6: " + l5);
        l6 = colors.get(6);
        Main.logger.info("Layer7: " + l6);
        l7 = colors.get(7);
        Main.logger.info("Layer8: " + l7);
        Main.logger.info("Successfully recalculated colors!");
    }

    // gradient generator
    public static ArrayList<String> generateGradient(String c1, String c2, int numColors) {
        ArrayList<String> gradientColors = new ArrayList<>();

        // removing '#' symbol from the input strings
        String color1 = c1.replace("#","");
        String color2 = c2.replace("#","");

        // Convert hexadecimal colors to RGB
        int color1_r = Integer.parseInt(color1.substring(0, 2), 16);
        int color1_g = Integer.parseInt(color1.substring(2, 4), 16);
        int color1_b = Integer.parseInt(color1.substring(4, 6), 16);

        int color2_r = Integer.parseInt(color2.substring(0, 2), 16);
        int color2_g = Integer.parseInt(color2.substring(2, 4), 16);
        int color2_b = Integer.parseInt(color2.substring(4, 6), 16);

        // Calculate the difference between each RGB component
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

    // trigger recalculation on reload
    @Override
    public void onReload() {
        revalidate();
    }
}
