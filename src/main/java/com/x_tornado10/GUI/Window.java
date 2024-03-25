package com.x_tornado10.GUI;

import javax.swing.*;

// wrapper to create a new window
public class Window extends JFrame {
    public Window() {}
    // creates a new window with the specified width, height, title, x- and y-position, window close operation
    public Window(int x, int y, int width, int height, String title, int windowCloseOperation) {
        setBounds(x, y, width, height);
        setTitle(title);
        setDefaultCloseOperation(windowCloseOperation);
    }
}
