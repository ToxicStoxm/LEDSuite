package com.x_tornado10.Main_Window;

import com.x_tornado10.Main;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class Main_Window extends JFrame {
    public Main_Window() {
        // sets title, resizeability and default close operation
        configureAppearanceAndBehavior();

        // sets position and dimension of window
        setWindowPosAndBounds();

        // displays the window on screen
        setVisible(true);

        // display a fake loading bar if enabled in config.yaml
        if (Main.settings.isFakeLoadingBar()) {
            fakeLoadingBar();
        }
    }

    public void configureAppearanceAndBehavior() {
        // set the title of the window that will appear in the top bar of the window
        setTitle(Main.settings.getWindowTitle());
        // user is able to resize window without restrictions
        setResizable(Main.settings.isWindowResizeable());
        // program ends if window is closed
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void setWindowPosAndBounds() {
        // getting current graphical environment
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        // get a list of all graphical devices
        GraphicsDevice[] gs = ge.getScreenDevices();
        // select the primary screen (= 0)
        GraphicsDevice gd = gs[0];

        // setting the window size according to the size specified in config.yaml
        setBounds(new Rectangle(Main.settings.getWindowWidth(), Main.settings.getWindowHeight()));
        // setting the location of the window to be the top left corner of the current screen
        setLocation(gd.getDefaultConfiguration().getBounds().x,
                gd.getDefaultConfiguration().getBounds().y);

        // checking if full screen (windowed full screen) is enabled
        if (Main.settings.isWindowFullScreen()) {
            // setting the window size to match the current screens size
            setBounds(new Rectangle(getToolkit().getScreenSize().width, getToolkit().getScreenSize().height));
            // setting the location of the window to be the top left corner of the current screen
            setLocation(gd.getDefaultConfiguration().getBounds().x,
                    gd.getDefaultConfiguration().getBounds().y);

        // checking if window centering is enabled in config.yaml
        } else {
            if (Main.settings.isWindowCenter()) {
                // calculating screen center and moving window to it
                setLocation((gd.getDefaultConfiguration().getBounds().x + (getToolkit().getScreenSize().width / 2)) - (getWidth() / 2),
                        (gd.getDefaultConfiguration().getBounds().y + (getToolkit().getScreenSize().height / 2)) - (getHeight() / 2));
            }
        }
    }
    // displays a fake loading bar
    public void fakeLoadingBar() {
        // sending a few messages to console as warning because the fake loading bar can cause issues with thread interrupting
        Main.logger.info("Displaying fake loading bar...");
        Main.logger.warn("If there is an error please disable the fake loading bar!");
        final JProgressBar bar = new JProgressBar(0,100);
        // configuring dimensions and color of the loading bar
        bar.setBounds(new Rectangle(200, 10));
        bar.setBorder(new LineBorder(Color.BLACK));
        bar.setBackground(Color.decode(Main.settings.getDarkMColorSec()));
        bar.setForeground(Color.decode(Main.settings.getDarkMColorPrim()));
        // adding the loading bar to the main window
        add(bar);
        // updating the loading bar in 'natural' intervals to make it seem realer
        for(int i = bar.getMinimum(); i <= bar.getMaximum(); i++){
            final int percent = i;
            SwingUtilities.invokeLater(() -> bar.setValue(percent));
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                // sending error messages to console if the current thread gets interrupted while the fake loading bar is running
                Main.logger.error("Current thread was interrupted!");
                Main.logger.warn("Please disable fake loading bar!");
            }
        }
    }
}
