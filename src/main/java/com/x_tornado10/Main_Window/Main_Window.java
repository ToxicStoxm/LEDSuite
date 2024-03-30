package com.x_tornado10.Main_Window;

import com.x_tornado10.Main;
import lombok.RequiredArgsConstructor;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class Main_Window extends JFrame {
    private final JPanel main;
    private double north = 0;
    private double south = 0;
    private double east = 0;
    private double west = 0;
    private final JPanel bN;
    private final JPanel bS;
    private final JPanel bE;
    private final JPanel bW;
    public Main_Window() {
        // sets title, resizeability and default close operation
        configureAppearanceAndBehavior();

        // sets position and dimension of window
        setWindowPosAndBounds();

        // displays the window on screen
        setVisible(true);

        // initialize main panel
        main = new JPanel();

        // initialize borders and add them to the window
        bN = new JPanel();
        bN.setBackground(Color.BLUE);
        bS = new JPanel();
        bE = new JPanel();
        bW = new JPanel();

        add(bN, BorderLayout.NORTH);
        add(bS, BorderLayout.SOUTH);
        add(bE, BorderLayout.EAST);
        add(bW, BorderLayout.WEST);

        // add main panel to window
        add(main, BorderLayout.CENTER);

        // update borders to prevent bugs
        updateBorder();

        final long[] last = {System.currentTimeMillis() - 8};

        // adding a Component listener to the window to detect window resizes and update borders
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                if (System.currentTimeMillis() - last[0] >= 8) {
                    updateBorder();
                    last[0] = System.currentTimeMillis();
                }
            }
        });

        // display a fake loading bar if enabled in config.yaml
        if (Main.settings.isFakeLoadingBar()) {
            fakeLoadingBar();
        }

        // resetting borders and clearing main panel
        resetScreen();

        //settingsMenu();
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
        // selecting screen specified in config.yaml
        // if the screen does not exist the primary screen is selected instead and an error message is displayed in the console
        GraphicsDevice gd;
        if (gs.length - 1 < Main.settings.getWindowInitialScreen()) {
            Main.logger.error("Screen " + Main.settings.getWindowInitialScreen() + " does not exist! Max value = " + (gs.length - 1));
            Main.logger.warn("Primary screen is used instead!");
            gd = gs[0];
        } else {
            gd = gs[Main.settings.getWindowInitialScreen()];
        }

        // check if full screen mode is active
        if (Main.settings.isWindowFullScreen()) {
            // setting the window to full screen mode on the specified screen
            gd.setFullScreenWindow(this);
        } else {
            // setting the window size according to the size specified in config.yaml
            setBounds(new Rectangle(Main.settings.getWindowWidth(), Main.settings.getWindowHeight()));
            // setting the location of the window to be the top left corner of the current screen
            setLocation(gd.getDefaultConfiguration().getBounds().x,
                    gd.getDefaultConfiguration().getBounds().y);

            // checking if full screen (windowed full screen) is enabled
            if (Main.settings.isWindowedFullScreen()) {
                // setting the window size to match the current screens size
                setBounds(new Rectangle(gd.getDisplayMode().getWidth(), gd.getDisplayMode().getHeight()));
                // setting the location of the window to be the top left corner of the current screen
                setLocation(gd.getDefaultConfiguration().getBounds().x,
                        gd.getDefaultConfiguration().getBounds().y);

                // checking if window centering is enabled in config.yaml
            } else {
                if (Main.settings.isWindowCenter()) {
                    // calculating screen center and moving window to it
                    setLocation((gd.getDefaultConfiguration().getBounds().x + (gd.getDefaultConfiguration().getBounds().width / 2)) - (getWidth() / 2),
                            (gd.getDefaultConfiguration().getBounds().y + (gd.getDefaultConfiguration().getBounds().height / 2)) - (getHeight() / 2));
                }
            }
        }
    }
    // displays a fake loading bar
    public void fakeLoadingBar() {
        // sending a few messages to console as warning because the fake loading bar can cause issues with thread interrupting
        Main.logger.info("Displaying fake loading bar...");
        Main.logger.warn("If there are any errors please disable the fake loading bar!");

        // setting correct border to center the loading bar
        setBorder("north",15.0 / 32.0); // ~ 46%

        JProgressBar bar = new JProgressBar(0,100);
        bar.setFont(new Font("Bahnschrift",Font.PLAIN,20));
        bar.setStringPainted(true);

        // configuring dimensions and color of the loading bar
        bar.setBorder(new LineBorder(Color.BLACK));
        bar.setBackground(Main.settings.getDarkModeSec());
        bar.setForeground(Main.settings.getDarkModePrim());
        bar.setPreferredSize(new Dimension(getWidth()/20 * 19, getHeight() / 16));

        // adding the loading bar to the main window
        main.add(bar);
        // updating the loading bar in 'natural' intervals to make it seem realer
        for(int i = bar.getMinimum(); i <= bar.getMaximum(); i++){
            bar.setValue(i);
            // updating the percent display and adding random decimals between 0.0 and 1.0 to make it seem more realistic
            double temp = ((double) i / bar.getMaximum()) * 100;
            if (i < bar.getMaximum()) temp = temp + Math.random();
            double result = (double) Math.round((temp * 10)) / 10;
            bar.setString(result + "%");

            try {
                Thread.sleep((long) (25 + Math.random()*10));
            } catch (InterruptedException e) {
                // sending error messages to console if the current thread gets interrupted while the fake loading bar is running
                Main.logger.error("Current thread was interrupted!");
                Main.logger.warn("Please disable fake loading bar!");
            }
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // sending error messages to console if the current thread gets interrupted while the fake loading bar is running
            Main.logger.error("Current thread was interrupted!");
            Main.logger.warn("Please disable fake loading bar!");
        }

    }

    private void settingsMenu() {
        JToggleButton jTB = new JToggleButton();
        jTB.setFocusable(false);
        jTB.setText("Test");
        this.add(jTB, BorderLayout.SOUTH);
    }

    // resetting borders and clearing main panel
    private void resetScreen() {
        setBorder("*", 0);
        main.removeAll();
        main.revalidate();
        // built in window update function
        repaint();
    }

    private void setBorder(String border , double value) {
        switch (border.toLowerCase()) {
            // change all borders
            case "*": {
                south = value;
                north = value;
                east = value;
                west = value;
            }
            // change borders individually
            case "south": south = value;
            break;
            case "north": north = value;
            break;
            case "east": east = value;
            break;
            case "west": west = value;
            break;
            // if a wrong border argument is specified an IllegalArgumentException is thrown
            default: throw new IllegalArgumentException();
        }
        // update the border to match the new value
        updateBorder();
    }
    // updating borders to match latest values specified by north, south, east and west double variables
    private void updateBorder() {
        boolean repaint = false;
        if (!(bN.getHeight() == 0 && north == 0)) {
            bN.setPreferredSize(new Dimension(0, (int) Math.round(getHeight() * north)));
            Main.logger.info("Calculated: " + (int) Math.round(getHeight() * north));
            Main.logger.info("Current: " + bN.getHeight());
            bN.revalidate();
            repaint = true;
        }
        if (!(bS.getHeight() == 0 && south == 0)) {
            bS.setPreferredSize(new Dimension(0, (int) Math.round(getHeight() * south)));
            repaint = true;
        }
        if (!(bW.getHeight() == 0 && west == 0)) {
            bW.setPreferredSize(new Dimension((int) Math.round(getWidth() * west), 0));
            repaint = true;
        }
        if (!(bE.getHeight() == 0 && east == 0)) {
            bE.setPreferredSize(new Dimension((int) Math.round(getWidth() * east), 0));
            repaint = true;
        }

        // updating window to display new (resized) borders
        if (repaint) repaint();
    }

}

