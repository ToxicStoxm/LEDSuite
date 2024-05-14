package com.x_tornado10.Main_Window;

import com.x_tornado10.Events.EventListener;
import com.x_tornado10.Events.Events.Event;
import com.x_tornado10.Main;
import com.x_tornado10.Settings.Server_Settings;
import com.x_tornado10.Settings.Local_Settings;
import com.x_tornado10.Settings.Settings;
import com.x_tornado10.util.ColorManager;
import com.x_tornado10.util.Networking;
import com.x_tornado10.util.Paths;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.plaf.ToolTipUI;
import javax.swing.plaf.basic.BasicToolTipUI;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main_Window extends JFrame implements EventListener {
    //private WINDOW_STATE current_State = WINDOW_STATE.NONE;
    private final JPanel main;
    private double north = 0;
    private double south = 0;
    private double east = 0;
    private double west = 0;
    private final JPanel bN;
    private final JPanel bS;
    private final JPanel bE;
    private final JPanel bW;
    public boolean windows = false;
    public Main_Window(boolean windows, boolean first) {
        Main.logger.debug("Loading main window...");
        this.windows = windows;

        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            Main.logger.fatal("Failed to load GTK look and feel!");
            Main.logger.error("Invalid look and feel!");
            Main.logger.warn("Please check if your system supports GTK!");
            if (!windows) Main.exit(0);
            else Main.logger.warn("Loading default look and feel! This will break some menus and / or buttons!");
        }

        // sets title, resizeability and default close operation
        Main.logger.debug("Configuring appearance...");
        configureAppearanceAndBehavior();
        Main.logger.debug("Successfully configured appearance!");

        // sets position and dimension of window
        Main.logger.debug("Calculating window position and bounds...");
        setWindowPosAndBounds();
        Main.logger.debug("Successfully calculated and set window position and bounds!");

        setBackground(Main.cm.l0);

        // displays the window on screen
        setVisible(true);

        // initialize main panel
        Main.logger.debug("Creating main content panel...");
        main = new JPanel();
        main.setBackground(Main.cm.l0);

        main.setFont(new Font("Bahnschrift", Font.PLAIN, Main.settings.scale(20)));

        Main.logger.debug("Adding borders to window...");
        // initialize borders and add them to the window
        bN = new JPanel();
        bS = new JPanel();
        bE = new JPanel();
        bW = new JPanel();

        bN.setBackground(Main.cm.l0);
        bS.setBackground(Main.cm.l0);
        bE.setBackground(Main.cm.l0);
        bW.setBackground(Main.cm.l0);

        add(bN, BorderLayout.NORTH);
        add(bS, BorderLayout.SOUTH);
        add(bE, BorderLayout.EAST);
        add(bW, BorderLayout.WEST);

        // add main panel to window
        add(main, BorderLayout.CENTER);

        // update borders to prevent bugs
        updateBorder();

        final long[] last = {System.currentTimeMillis() - 8};

        // checking if window is resizeable before unnecessarily loading this listener
        if (Main.settings.isWindowResizeable()) {
            Main.logger.debug("Loading resize listener...");
            // adding a Component listener to the window to detect window resizes and update borders
            addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    if (System.currentTimeMillis() - last[0] >= 8) {
                        updateBorder();
                        last[0] = System.currentTimeMillis();
                    }
                }
            });
        }

        Main.logger.debug("Successfully loaded main content panel!");

        // display a fake loading bar if enabled in config.yaml
        if (Main.settings.isFakeLoadingBar()) {
            fakeLoadingBar();
        }

        // resetting borders and clearing main panel
        resetScreen();

        if (first) Main.started();

        mainMenu();
    }

    public void configureAppearanceAndBehavior() {
        // set the title of the window that will appear in the top bar of the window
        setTitle(Main.settings.getWindowTitle());
        // user is able to resize window without restrictions
        setResizable(Main.settings.isWindowResizeable());
        // setting minimum width and height to prevent gui elements glitching
        if (Main.settings.isWindowResizeable()) {
            int minHeight = 600;
            int minWidth = 775;
            setMinimumSize(new Dimension(minWidth,minHeight));
        }
        // program ends if window is closed
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
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
        //current_State = WINDOW_STATE.LOADING_BAR;
        // sending a few messages to console as warning because the fake loading bar can cause issues with thread interrupting
        Main.logger.debug("Displaying fake loading bar...");

        // setting correct border to center the loading bar
        setBorder("north",15.0 / 32.0); // ~ 46%

        JProgressBar bar = new JProgressBar(0,100);
        bar.setFont(new Font("Bahnschrift",Font.PLAIN,20));
        bar.setStringPainted(true);

        // configuring dimensions and color of the loading bar

        bar.setBorder(new LineBorder(Main.cm.l0, 3,true));
        bar.setBackground(Main.cm.l7);
        bar.setForeground(Main.cm.l1);
        bar.setOpaque(false);
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
            Main.logger.debug("FakeLoadingBar: " + result + "%");

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

    // displaying main menu
    private void mainMenu() {
        //current_State = WINDOW_STATE.MAIN;
        // resetting / clearing screen
        resetScreen();

        Main.logger.debug("Loading main menu...");
        // setting new layout for the main content pane
        BorderLayout bl = new BorderLayout();
        bl.setVgap(Main.settings.scale(10));
        bl.setHgap(Main.settings.scale(10));
        main.setLayout(bl);

        // creating a new menu bar and adding it to the main content pane
        Menu_Bar mb = new Menu_Bar(getWidth(), getHeight());
        mb.setBackground(Main.cm.l1);
        mb.setForeground(Main.cm.l7);
        mb.setPreferredSize(new Dimension(0, Main.settings.scale(30)));
        main.add(mb, BorderLayout.NORTH);
        // revalidating main content pane to ensure correct displaying of the menu bar
        main.revalidate();


        JButton jB = new JButton();
        jB.setFocusable(false);
        jB.setForeground(Main.cm.l2);
        jB.setBackground(Main.cm.l0);
        Font f = new Font("Bahnschrift", Font.PLAIN, 20);
        jB.setFont(f);
        jB.setText("Upload new Animation");

        jB.addActionListener((e) -> {
            FileDialog fd = new FileDialog(this, Main.settings.getWindowTitle(), FileDialog.LOAD);
            fd.setDirectory(Main.settings.getSelectionDir());
            fd.setVisible(true);
            String currentDir = fd.getDirectory();
            String filePath = currentDir + fd.getFile();
            if (fd.getFile() == null) {
                Main.logger.info("No file was selected!");
            } else {
                Main.logger.info("Selected: " + currentDir + filePath);
                Main.settings.setSelectionDir(currentDir);
                File file2 = new File(filePath);
                Networking.FileSender.sendFileToServer(Main.server_settings.getIPv4(), Main.server_settings.getPort(), file2.getAbsolutePath());
            }
        });

        GridLayout gL = new GridLayout(3,3);

        JPanel mainGrid = new JPanel();
        mainGrid.setOpaque(false);
        mainGrid.setLayout(gL);
        mainGrid.setBackground(Main.cm.l0);
        mainGrid.setOpaque(true);
        jB.setOpaque(true);
        jB.setOpaque(false);

        for (int i = 0; i < 9; i++) {
            if (i != 4) {
                JPanel filler = new JPanel();
                filler.setOpaque(false);
                //filler.setBackground(new Color(i * 10, i * 10, i * 10));
                mainGrid.add(filler);
            } else {
                mainGrid.add(jB);
            }
        }

        main.add(mainGrid, BorderLayout.CENTER);
        main.revalidate();

        Main.logger.debug("Successfully loaded main menu!");
    }

    // displaying settings menu
    public void settingsMenu() {
        //current_State = WINDOW_STATE.SETTINGS;
        Main.logger.debug("Loading settings menu...");
        resetScreen();

        Local_Settings changes = new Local_Settings();
        changes.copy(Main.settings);
        changes.setName("Settings-User-Changes");

        JPanel jp = new JPanel();
        jp.setOpaque(false);
        jp.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JButton applyB = new JButton("Apply");
        applyB.setPreferredSize(new Dimension(Main.settings.scale(getWidth() / 14), Main.settings.scale(getHeight() / 20)));
        JButton okB = new JButton("OK");
        okB.setPreferredSize(new Dimension(Main.settings.scale(getWidth() / 14), Main.settings.scale(getHeight() / 20)));
        JButton resetB = new JButton("Reset");
        resetB.setPreferredSize(new Dimension(Main.settings.scale(getWidth() / 14), Main.settings.scale(getHeight() / 20)));

        okB.addActionListener(e -> {
            Main.logger.debug("Ok button pressed!");
            // check if there are any unsaved changes to the settings
            if (!Main.settings.equals(changes)) {
                JOptionPane optionPane = new JOptionPane();

                // setting options, displayed question and title, default option and question type
                String[] options = new String[2];
                options[1] = "Save";
                options[0] = "Don't save";
                optionPane.setOptions(options);
                optionPane.setInitialSelectionValue(options[1]);
                optionPane.setMessageType(JOptionPane.QUESTION_MESSAGE);
                optionPane.setMessage("Do you want to apply / save changes before closing settings menu?");

                // creating the popup and displaying it
                JDialog dialog = optionPane.createDialog("Save before quitting?");
                dialog.setFocusable(false);
                dialog.setVisible(true);

                // checking witch option the user selected
                Object selectedValue = optionPane.getValue();
                // checking if the user closed the window
                // in that case do nothing
                if (selectedValue != null) {
                    Main.logger.debug("Nothing selected!");
                    // checking if the user clicked
                    if (selectedValue.equals(options[1])) {
                        // saving the new settings and closing settings menu
                        // handling potential config type mismatch gracefully with config warn message and error popup
                        if (!applyNewSettings(changes)) {
                            Main.logger.error("Failed to save config changes!");
                            Main.logger.error_popup("Failed to save config changes! We're sorry for the inconvenience! \n" +
                                    "Please try restarting the application and if the error persists we recommend opening an issue on the projects GitHub: " + Paths.Links.Project_GitHub);
                        }
                        // loading main menu
                        mainMenu();
                    } else {
                        Main.logger.debug("Quitting without saving...");
                        // loading main menu without saving
                        mainMenu();
                    }
                }
            } else {
                Main.logger.debug("No changes were made. Quitting without saving...");
                //loading main menu
                mainMenu();
            }
        });
        applyB.addActionListener(e -> {
            Main.logger.debug("Apply button pressed!");
            if (!applyNewSettings(changes)) {
                Main.logger.error("Failed to save config changes!");
                Main.logger.error_popup("Failed to save config changes! We're sorry for the inconvenience! \n" +
                        "Please try restarting the application and if the error persists we recommend opening an issue on the projects GitHub: " + Paths.Links.Project_GitHub);
                mainMenu();
            }
        });
        resetB.addActionListener(e -> {
            Main.logger.debug("Reset button pressed!");

            JOptionPane optionPane = new JOptionPane();

            String[] options = new String[2];
            options[1] = "Yes";
            options[0] = "Cancel";
            optionPane.setOptions(options);
            optionPane.setInitialSelectionValue(options[0]);
            optionPane.setMessageType(JOptionPane.QUESTION_MESSAGE);
            optionPane.setMessage("Do you want to reset all settings to default?");

            // creating the popup and displaying it
            JDialog dialog = optionPane.createDialog("Reset settings?");
            dialog.setFocusable(false);
            dialog.setVisible(true);

            // checking witch option the user selected
            Object selectedValue = optionPane.getValue();

            if (selectedValue != null) {
                if (selectedValue.equals(options[1])) {
                    try {
                        Main.settings.reset();
                    } catch (IOException | NullPointerException ex) {
                        Main.logger.error("Failed to reset config settings!");
                        Main.logger.warn("Please restart the application to prevent any further issues!");
                    }

                }
            }

        });

        // hacky workaround to prevent TextArea not applying changes
        applyB.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {

                Component comp = getFocusOwner();
                if (comp != null && comp.getName() != null && comp.getName().contains(Paths.Placeholders.LOOSE_FOCUS)) {
                    for (FocusListener fl : comp.getFocusListeners()) {
                        JLabel temp = new JLabel();
                        fl.focusLost(new FocusEvent(comp, FocusEvent.FOCUS_LOST, false, temp));
                        fl.focusGained(new FocusEvent(comp, FocusEvent.FOCUS_GAINED, false, temp));
                    }
                }
            }
        });

        // hacky workaround to prevent TextArea not applying changes
        okB.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {

                Component comp = getFocusOwner();
                if (comp != null && comp.getName() != null && comp.getName().contains(Paths.Placeholders.LOOSE_FOCUS)) {
                    for (FocusListener fl : comp.getFocusListeners()) {
                        JLabel temp = new JLabel();
                        fl.focusLost(new FocusEvent(comp, FocusEvent.FOCUS_LOST, false, temp));
                        fl.focusGained(new FocusEvent(comp, FocusEvent.FOCUS_GAINED, false, temp));
                    }
                }
            }
        });


        applyB.setFocusable(false);
        okB.setFocusable(false);
        resetB.setFocusable(false);

        applyB.setBackground(Main.cm.l7);
        okB.setBackground(Main.cm.l7);
        resetB.setBackground(Main.cm.l7);

        applyB.setForeground(Main.cm.l2);
        okB.setForeground(Main.cm.l2);
        resetB.setForeground(Main.cm.l2);

        Font f0 = new Font("Bahnschrift", Font.PLAIN, Main.settings.scale(10));

        applyB.setFont(f0);
        okB.setFont(f0);
        resetB.setFont(f0);

        jp.add(applyB);
        jp.add(okB);

        JPanel bottomPanel = new JPanel();

        GridLayout gL0 = new GridLayout(1,2);

        bottomPanel.setLayout(gL0);

        JPanel jp0 = new JPanel();
        jp0.setOpaque(false);
        jp0.setLayout(new FlowLayout(FlowLayout.LEFT));

        jp0.add(resetB);

        bottomPanel.setOpaque(false);
        bottomPanel.add(jp0);
        bottomPanel.add(jp);


        main.add(bottomPanel, BorderLayout.SOUTH);


        JPanel settingsPanel = new JPanel();
        settingsPanel.setBackground(Main.cm.l1);

        GridLayout gL = new GridLayout(2, 2);
        gL.setHgap(8);
        gL.setVgap(8);
        settingsPanel.setLayout(gL);

        List<JPanel> sections = generateSections(changes);

        for (JPanel p : sections) {
            p.setBackground(Main.cm.l2);
            p.setBorder(BorderFactory.createLineBorder(Main.cm.l5, 5, true));
            JLabel jL = new JLabel(p.getName());
            jL.setForeground(Main.cm.l7);
            Font f = main.getFont();
            jL.setFont(new Font(f.getFontName(),f.getStyle(),20));
            JPanel jpp = new JPanel();
            jpp.setOpaque(false);
            jpp.add(jL);
            p.add(jpp, BorderLayout.NORTH);
            settingsPanel.add(p);
        }


        main.add(settingsPanel, BorderLayout.CENTER);

        Main.logger.debug("Successfully loaded settings menu!");
    }

    private static List<JPanel> generateSections(Local_Settings changes) {
        Main.logger.debug("Generating settings sections...");
        Font f = new Font("Bahnschrift", Font.PLAIN, 20);

        Main.logger.debug("--- appearance ---");
        JPanel appearance = new JPanel();
        appearance.setLayout(new BorderLayout());
        appearance.setName("Appearance");

        JPanel aMContent = new JPanel();
        aMContent.setOpaque(false);

        JPanel centering = new JPanel(new GridLayout(3,1));
        centering.setOpaque(false);

        JPanel restartNotice = new JPanel();
        JLabel restartNoticeLabel = new JLabel("Adjusting these settings will trigger a restart!");
        restartNoticeLabel.setForeground(ColorManager.info);
        restartNoticeLabel.setBackground(ColorManager.adjustColorForIndistinguishability(restartNoticeLabel.getForeground(), Main.cm.l2, 100, true));
        restartNoticeLabel.setVerticalAlignment(SwingConstants.CENTER);
        restartNoticeLabel.setFont(f);
        restartNoticeLabel.setOpaque(true);
        restartNotice.add(restartNoticeLabel);

        restartNotice.setOpaque(false);

        JPanel placeholder = new JPanel();
        placeholder.setOpaque(false);

        centering.add(placeholder);
        centering.add(aMContent);
        centering.add(restartNotice);

        appearance.add(centering, BorderLayout.CENTER);

        JToggleButton dmSwitch = new JToggleButton();
        dmSwitch.setSelected(changes.isDarkM());

        dmSwitch.setText("Dark Mode");
        dmSwitch.setFont(f);
        dmSwitch.setFocusable(false);
        aMContent.add(dmSwitch);

        Font f1 = new Font(f.getFontName(), f.getStyle(), 18);

        JPanel colorSetters = new JPanel();
        colorSetters.setOpaque(false);
        JLabel jl0 = new JLabel("Accent Colors: ");
        jl0.setFont(f1);
        jl0.setForeground(Main.cm.l7);
        colorSetters.add(jl0);
        JTextArea dmCP = new JTextArea();
        dmCP.setName("dmCP - " + Paths.Placeholders.LOOSE_FOCUS);

        dmCP.setFont(new Font(f1.getFontName(), Font.BOLD, f1.getSize()));
        dmCP.setOpaque(true);
        dmCP.setText(changes.isDarkM() ? changes.getDarkMColorPrim() : changes.getLightMColorPrim());
        Color targetColor = Color.decode(changes.isDarkM() ? changes.getDarkMColorPrim() : changes.getLightMColorPrim());
        dmCP.setForeground(targetColor);
        dmCP.setBackground(ColorManager.adjustColorForIndistinguishability(
                targetColor,
                Main.cm.l2
        ));
        dmCP.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                Main.logger.debug("TextArea dmCP gained Focus!");
                dmCP.setText(dmSwitch.isSelected() ? changes.getDarkMColorPrim() : changes.getLightMColorPrim());
                Color targetColor = Color.decode(dmSwitch.isSelected() ? changes.getDarkMColorPrim() : changes.getLightMColorPrim());
                        dmCP.setForeground(targetColor);
                dmCP.setBackground(ColorManager.adjustColorForIndistinguishability(
                        targetColor,
                        Main.cm.l2
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                Main.logger.debug("TextArea dmCP lost Focus!");
                String t = dmCP.getText();
                Main.logger.debug("TextArea dmCP content changed: '" + t + "'");
                Color c;
                try {
                   c = Color.decode(t);
                } catch (NumberFormatException ex) {
                    Main.logger.debug("Invalid color input for " + (dmSwitch.isSelected() ? "darkModeColorPrimary" : "lightModeColorPrimary") + ": '" + t + "'");
                    dmCP.setText(dmSwitch.isSelected() ? changes.getDarkMColorPrim() : changes.getLightMColorPrim());
                    Color targetColor = Color.decode(dmSwitch.isSelected() ? changes.getDarkMColorPrim() : changes.getLightMColorPrim());
                    dmCP.setForeground(targetColor);
                    dmCP.setBackground(ColorManager.adjustColorForIndistinguishability(
                            targetColor,
                            Main.cm.l2
                    ));

                    Main.logger.debug("Triggered system beep!");
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    return;
                }
                dmCP.setForeground(c);
                dmCP.setBackground(ColorManager.adjustColorForIndistinguishability(
                        c,
                        Main.cm.l2
                ));
                if (dmSwitch.isSelected()) {
                    changes.setDarkMColorPrim(t);
                } else changes.setLightMColorPrim(t);
            }
        });
        colorSetters.add(dmCP);

        JTextArea dmCS = new JTextArea();
        dmCS.setName("dmCS - " + Paths.Placeholders.LOOSE_FOCUS);
        dmCS.setFont(new Font(f1.getFontName(), Font.BOLD, f1.getSize()));
        dmCS.setOpaque(true);
        dmCS.setText(changes.isDarkM() ? changes.getDarkMColorSec() : changes.getLightMColorSec());
        Color targetColor1 = Color.decode(changes.isDarkM() ? changes.getDarkMColorSec() : changes.getLightMColorSec());
        dmCS.setForeground(targetColor1);
        dmCS.setBackground(ColorManager.adjustColorForIndistinguishability(
                targetColor1,
                Main.cm.l2
        ));
        dmCS.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                Main.logger.debug("TextArea dmCS gained Focus!");
                dmCS.setText(dmSwitch.isSelected() ? changes.getDarkMColorSec() : changes.getLightMColorSec());
                Color targetColor1 = Color.decode(dmSwitch.isSelected() ? changes.getDarkMColorSec() : changes.getLightMColorSec());
                dmCS.setForeground(targetColor1);
                dmCS.setBackground(ColorManager.adjustColorForIndistinguishability(
                        targetColor1,
                        Main.cm.l2
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                Main.logger.debug("TextArea dmCS lost Focus!");
                String t = dmCS.getText();
                Main.logger.debug("TextArea dmCS content changed: '" + t + "'");
                Color c;
                try {
                    c = Color.decode(t);
                } catch (NumberFormatException ex) {
                    Main.logger.debug("Invalid color input for " + (dmSwitch.isSelected() ? "darkModeColorSecondary" : "lightModeColorSecondary") + ": '" + t + "'");
                    dmCS.setText(dmSwitch.isSelected() ? changes.getDarkMColorSec() : changes.getLightMColorSec());
                    Color targetColor1 = Color.decode(dmSwitch.isSelected() ? changes.getDarkMColorSec() : changes.getLightMColorSec());
                    dmCS.setForeground(targetColor1);
                    dmCS.setBackground(ColorManager.adjustColorForIndistinguishability(
                            targetColor1,
                            Main.cm.l2
                    ));

                    Main.sysBeep();
                    return;
                }
                dmCS.setForeground(c);
                dmCS.setBackground(ColorManager.adjustColorForIndistinguishability(
                        c,
                        Main.cm.l2
                ));
                if (dmSwitch.isSelected()) {
                    changes.setDarkMColorSec(t);
                } else changes.setLightMColorSec(t);
            }
        });
        colorSetters.add(dmCS);
        aMContent.add(colorSetters);

        dmSwitch.addActionListener(a -> {
            Main.logger.debug("DarkMode Switch: " + dmSwitch.isSelected());
            changes.setDarkM(dmSwitch.isSelected());
            dmCP.setText(dmSwitch.isSelected() ? changes.getDarkMColorPrim() : changes.getLightMColorPrim());
            dmCP.setForeground(Color.decode(dmSwitch.isSelected() ? changes.getDarkMColorPrim() : changes.getLightMColorPrim()));
            dmCS.setText(dmSwitch.isSelected() ? changes.getDarkMColorSec() : changes.getLightMColorSec());
            dmCS.setForeground(Color.decode(dmSwitch.isSelected() ? changes.getDarkMColorSec() : changes.getLightMColorSec()));
        });
        Main.logger.debug("--- done ---");


        Main.logger.debug("--- window ---");
        JPanel window = new JPanel();
        window.setLayout(new BorderLayout());
        window.setName("Window");

        JPanel wMainContent = new JPanel();
        wMainContent.setOpaque(false);

        JPanel centering0 = new JPanel();
        centering0.setLayout(new GridLayout(3,1));
        centering0.setOpaque(false);

        JPanel windowTitle = new JPanel();
        windowTitle.setOpaque(false);

        JLabel wTitleLabel = new JLabel("Window Title: ");
        wTitleLabel.setFont(f1);
        wTitleLabel.setForeground(Main.cm.l7);


        JTextArea wT = new JTextArea(changes.getWindowTitleRaw());

        wT.setName("wT - " + Paths.Placeholders.LOOSE_FOCUS);
        wT.setFont(new Font(f1.getFontName(), Font.BOLD, f1.getSize()));
        wT.setForeground(Main.cm.l7);
        wT.setBackground(ColorManager.adjustColorForIndistinguishability(wT.getForeground(), Main.cm.l2));
        wT.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                String title = wT.getText();
                if (title.isBlank()) {
                    Main.sysBeep();
                    wT.setText(changes.getWindowTitleRaw());
                } else changes.setWindowTitle(title);
            }

            @Override
            public void focusLost(FocusEvent e) {
                String title = wT.getText();
                if (title.isBlank()) {
                    Main.sysBeep();
                    wT.setText(changes.getWindowTitleRaw());
                    return;
                }
                changes.setWindowTitle(title);
            }
        });


        windowTitle.add(wTitleLabel);

        windowTitle.add(wT);
        wMainContent.add(windowTitle);
        centering0.add(wMainContent);

        window.add(centering0, BorderLayout.CENTER);

        Main.logger.debug("--- done ---");



        Main.logger.debug("--- technical ---");
        JPanel technical = new JPanel();
        technical.setLayout(new BorderLayout());
        technical.setName("Technical");

        Main.logger.debug("--- done ---");



        Main.logger.debug("--- easterEggs ---");
        JPanel easterEggs = new JPanel();
        easterEggs.setLayout(new BorderLayout());
        easterEggs.setName("Easter Eggs");

        Main.logger.debug("--- done ---");

        List<JPanel> sections = new ArrayList<>();
        sections.add(appearance);
        sections.add(window);
        sections.add(technical);
        sections.add(easterEggs);
        Main.logger.debug("Successfully generated settings sections!");
        return sections;
    }

    public void serverSettingsMenu() {

        //current_State = WINDOW_STATE.SERVER_SETTINGS;
        Main.logger.debug("Loading server settings menu...");
        resetScreen();

        Server_Settings changes = new Server_Settings();
        changes.copy(Main.server_settings);
        changes.setName("Server-Settings-User-Changes");

        JPanel jp = new JPanel();
        jp.setOpaque(false);
        jp.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JButton applyB = new JButton("Apply");
        applyB.setPreferredSize(new Dimension(Main.settings.scale(getWidth() / 14), Main.settings.scale(getHeight() / 20)));
        JButton okB = new JButton("OK");
        okB.setPreferredSize(new Dimension(Main.settings.scale(getWidth() / 14), Main.settings.scale(getHeight() / 20)));

        okB.addActionListener(e -> {

            // check if there are any unsaved changes to the settings
            if (!Main.server_settings.equals(changes)) {
                JOptionPane optionPane = new JOptionPane();

                // setting options, displayed question and title, default option and question type
                String[] options = new String[2];
                options[1] = "Save";
                options[0] = "Don't save";
                optionPane.setOptions(options);
                optionPane.setInitialSelectionValue(options[1]);
                optionPane.setMessageType(JOptionPane.QUESTION_MESSAGE);
                optionPane.setMessage("Do you want to apply / save changes before closing settings menu?");

                // creating the popup and displaying it
                JDialog dialog = optionPane.createDialog("Save before quitting?");
                dialog.setFocusable(false);
                dialog.setVisible(true);

                // checking witch option the user selected
                Object selectedValue = optionPane.getValue();
                // checking if the user closed the window
                // in that case do nothing
                if (selectedValue != null) {
                    // checking if the user clicked
                    if (selectedValue.equals(options[1])) {
                        // saving the new settings and closing settings menu
                        // handling potential config type mismatch gracefully with console message and error popup
                        if (!applyNewSettings(changes)) {
                            Main.logger.error("Failed to save config changes!");
                            Main.logger.error_popup("Failed to save config changes! We're sorry for the inconvenience! \n" +
                                    "Please try restarting the application and if the error persists we recommend opening an issue on the projects GitHub: " + Paths.Links.Project_GitHub);
                        }
                        // loading main menu
                        mainMenu();
                    } else {
                        // loading main menu without saving
                        mainMenu();
                    }
                }
            } else {
                //loading main menu
                mainMenu();
            }
        });
        applyB.addActionListener(e -> {
            if (!applyNewSettings(changes)) {
                Main.logger.error("Failed to save config changes!");
                Main.logger.error_popup("Failed to save config changes! We're sorry for the inconvenience! \n" +
                        "Please try restarting the application and if the error persists we recommend opening an issue on the projects GitHub: " + Paths.Links.Project_GitHub);
                mainMenu();
            }
        });

        applyB.setFocusable(false);
        okB.setFocusable(false);

        applyB.setBackground(Main.cm.l7);
        okB.setBackground(Main.cm.l7);

        applyB.setForeground(Main.cm.l2);
        okB.setForeground(Main.cm.l2);

        applyB.setFont(new Font("Bahnschrift", Font.PLAIN, Main.settings.scale(10)));
        okB.setFont(new Font("Bahnschrift", Font.PLAIN, Main.settings.scale(10)));

        jp.add(applyB);
        jp.add(okB);

        main.add(jp, BorderLayout.SOUTH);

        Main.logger.debug("Successfully loaded settings menu!");
    }

    private boolean applyNewSettings(Settings changes) {
        Main.logger.debug("Applying and saving new settings...");
        switch (changes.getType()) {
            case LOCAL -> {
                Local_Settings changes0 = (Local_Settings) changes;
                boolean restart = Main.settings.requiresRestart(changes0);
                Main.settings.copy(changes);
                if (restart) Main.eventManager.sendReload(Settings.Type.LOCAL);
            }
            case SERVER -> Main.server_settings.copy(changes);
            case UNDEFINED -> {
                Main.logger.error("Can't save config values. Config type is undefined!");
                Main.logger.warn("Please try restarting the application and if the problem persists please seek support on github!");
                return false;
            }
            default -> {
                Main.logger.error("Unknown config type! Type: " + changes.getType());
                Main.logger.warn("Please try restarting the application and if the problem persists please seek support on github!");
                return false;
            }
        }
        Main.logger.debug("Successfully saved new config settings!");
        return true;
    }

    // resetting borders and clearing main panel
    private void resetScreen() {
        Main.logger.debug("Resetting window...");
        setBorder("*", 0);
        main.setLayout(new BorderLayout());
        main.removeAll();
        main.revalidate();
        // built in window update function
        repaint();
        Main.logger.debug("Successfully cleared and reset window!");
    }

    private void setBorder(String border , double value) {
        Main.logger.debug("Setting new borders...");
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
        Main.logger.debug("Reloading borders...");
        updateBorder();
        Main.logger.debug("Successfully set new borders!");
    }
    // updating borders to match latest values specified by north, south, east and west double variables
    private void updateBorder() {
        boolean repaint = false;
        if (!(bN.getHeight() == 0 && north == 0)) {
            bN.setPreferredSize(new Dimension(0, (int) Math.round(getHeight() * north)));
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

    public void exit(int status) {
        Main.logger.debug("Closing main window...");
        this.dispose();
        Main.logger.debug("Successfully closed main window!");
        Main.exit(status);
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        switch (e.getID()) {
            case WindowEvent.WINDOW_CLOSING -> exitDialog();
            case WindowEvent.WINDOW_LOST_FOCUS -> Main.logger.info("Main window lost focus!");
            case WindowEvent.WINDOW_GAINED_FOCUS -> Main.logger.info("Main window gained focus!");
        }
    }

    public void exitDialog() {

        // don't display the exit dialog on mobile
        if (Main.settings.isMobileFriendly()) {
            Main.mw.exit(0);
            return;
        }

        // creating new confirm popup before quitting application
        JOptionPane optionPane = new JOptionPane();

        // setting options, displayed question and title, default option and question type
        Object[] options = new Object[]{"Confirm","Cancel"};
        optionPane.setOptions(options);
        optionPane.setInitialSelectionValue(options[1]);
        optionPane.setMessageType(JOptionPane.QUESTION_MESSAGE);
        optionPane.setMessage("Do you want to close the application and all associated processes?");

        // creating the popup and displaying it
        JDialog dialog = optionPane.createDialog("Exit?");
        dialog.setFocusable(false);
        dialog.setVisible(true);

        // checking witch option the user selected
        Object selectedValue = optionPane.getValue();
        // checking if the user closed the window
        // in that case do nothing
        if (selectedValue != null) {
            // checking if the user clicked
            if (selectedValue.equals(options[0])) {
                // closing the window and exiting application with status code 0 (= no errors)
                Main.mw.exit(0);
            }
        }
    }

    @Override
    public void onEvent(Event event) {
        switch (event.getType()) {
            case RELOAD -> {
                main.revalidate();
                main.repaint();
                repaint();
                revalidate();
            }
            case STARTUP -> Main.logger.info("Starting main window...");
        }
    }

    /*
    public enum WINDOW_STATE {
        NONE,
        LOADING_BAR,
        MAIN,
        SETTINGS,
        SERVER_SETTINGS,
        FILE_PICKER
    }

     */
}

