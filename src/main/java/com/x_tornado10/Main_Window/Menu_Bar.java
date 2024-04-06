package com.x_tornado10.Main_Window;

import com.vaadin.open.Open;
import com.x_tornado10.Main;
import com.x_tornado10.Utilities.Paths;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Menu_Bar extends JMenuBar implements ActionListener, ItemListener {

    JMenuItem exit;
    JMenuItem info;
    JMenuItem settings;
    public Menu_Bar() {
        Main.logger.info("Loading menu bar...");

        // setting font
        this.setFont(new Font("Bahnschrift",Font.PLAIN,20));

        // creating new menu 'File'
        JMenu file = new JMenu("File");

        // adding 'Info' menuItem to the 'File' menu
        // click event: openURL 'https://github.com/ToxicStoxm/LED-Cube-Control-Panel'
        info = new JMenuItem("Info");
        info.setToolTipText("https://github.com/ToxicStoxm/LED-Cube-Control-Panel");
        info.addActionListener(this);
        file.add(info);

        // adding 'Settings' menuItem to the 'File' menu
        // click event: openSettingsMenu
        // hotkey ALT+S
        settings = new JMenuItem("Settings");
        settings.addActionListener(this);
        settings.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.ALT_DOWN_MASK));
        settings.setToolTipText("Open settings");
        file.add(settings);

        // adding a separator to 'File' menu
        file.addSeparator();

        // adding 'Exit' menuItem to 'File' menu
        // click event: exitApplication
        // hotkey ALT+Q
        exit = new JMenuItem("Exit");
        exit.addActionListener(this);
        exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.ALT_DOWN_MASK));
        exit.setToolTipText("Quit the application");
        file.add(exit);

        // adding 'File' menu to the menu bar
        this.add(file);
        Main.logger.info("Successfully loaded menu bar!");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // get the event source
        Object source = e.getSource();
        // checking witch menu item was clicked / triggered
        if (source.equals(info)) {
            // opening url with default system browser
            Main.logger.info("Opening: " + Paths.Links.Project_GitHub);
            Open.open(Paths.Links.Project_GitHub);
        }
        if (source.equals(settings)) {
            // opening settings menu
            Main.mw.settingsMenu();
        }
        if (source.equals(exit)) {
            Main.mw.exitDialog();
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {

    }
}
