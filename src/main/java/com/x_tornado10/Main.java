package com.x_tornado10;


import com.x_tornado10.GUI.Window;

import javax.swing.*;
import java.awt.*;

public class Main{
    public static boolean dark_mode = true;
    public static void main(String[] args) {

        // create new window (1080x720) position (500|500) with title and exit program on window close option
        Window welcomeScreen = new Window(500, 500, 1080, 720, "LED-Cube Control Panel 1.0", WindowConstants.EXIT_ON_CLOSE);
        // create new button
        JButton jB = new JButton();
        // add button to the screen
        welcomeScreen.add(jB);
        // set dimensions and other properties for the button
        jB.setBounds(0,0,128,32);
        jB.setOpaque(true);
        jB.setFocusable(false);
        // Assign a new BorderLayout Manager to the window
        welcomeScreen.setLayout(new BorderLayout());

        // create new Label
        JLabel jL = new JLabel();
        // set Text, Font, Alignment and dimensions for the label
        jL.setText("Hello World!");
        jL.setFont(new Font("Bahnschrift",Font.PLAIN,20));
        jL.setHorizontalAlignment(SwingConstants.CENTER);
        jL.setVerticalAlignment(SwingConstants.CENTER);
        jL.setOpaque(true);
        jL.setBounds(500, 500, 256,256);
        // add the label to the window
        welcomeScreen.add(jL);
        // add an action listener to the button that triggers switchMode if pressed via Lambda function
        jB.addActionListener(e -> switchMode(jB, welcomeScreen, jL));

        // invert dark mode bol before initializing to negate the invert in the init function
        dark_mode = !dark_mode;
        switchMode(jB, welcomeScreen, jL);
        // toggle the window's visibility
        welcomeScreen.setVisible(true);
    }
    // switches between light- and dark-mode
    public static void switchMode(JButton jB, Window welcomeScreen, JLabel jL) {
        // toggle
        dark_mode = !dark_mode;
        // set new button text
        if (!dark_mode) {
            jB.setText("Light Mode ☀️");
            jB.setToolTipText("Click to switch to Dark Mode");
        }
        else {
            jB.setText("Dark Mode \uD83C\uDF19");
            jB.setToolTipText("Click to switch to Light Mode");
        }
        // log the switch
        System.out.println("Switched to dark-mode: " + dark_mode);
        // adjust all colours to match the new mode
        jB.setBackground(new Color(dark_mode ? 0 : 255, dark_mode ? 0 : 255, dark_mode ? 0 : 255));
        jB.setForeground(new Color(dark_mode ? 255 : 0, dark_mode ? 255 : 0, dark_mode ? 255 : 0));
        welcomeScreen.setBackground(new Color(dark_mode ? 0 : 255, dark_mode ? 0 : 255, dark_mode ? 0 : 255));
        welcomeScreen.setForeground(new Color(dark_mode ? 255 : 0, dark_mode ? 255 : 0, dark_mode ? 255 : 0));
        jL.setBackground(new Color(dark_mode ? 0 : 255, dark_mode ? 0 : 255, dark_mode ? 0 : 255));
        jL.setForeground(new Color(dark_mode ? 255 : 0, dark_mode ? 255 : 0, dark_mode ? 255 : 0));
        // repaint / update the screen to display the newest values
        welcomeScreen.repaint();
    }
}