package com.x_tornado10.Main_Window;

import com.vaadin.open.Open;
import com.x_tornado10.Main;
import com.x_tornado10.util.Paths;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Menu_Bar extends JMenuBar implements ActionListener, ItemListener {

    JMenuItem exit;
    JMenuItem info;
    JMenuItem settings;
    JMenuItem serverSettings;
    public Menu_Bar(int width, int height) {
        Main.logger.info("Loading menu bar...");

        // setting font
        this.setFont(new Font("Bahnschrift",Font.PLAIN,20));

        // creating new menu 'File'
        JMenu file = new JMenu("File");

        // setting background for the MenuBar
        setBackground(Main.cm.l1);

        // configuring aesthetics of the 'File' menu
        file.setBackground(Main.cm.l3);
        file.setForeground(Main.cm.l7);
        file.setOpaque(true);
        file.setFont(new Font("Bahnschrift", Font.PLAIN,Main.settings.scale(10)));
        file.setPreferredSize(new Dimension(width / 16, height));

        // adding 'Info' menuItem to the 'File' menu
        // click event: openURL 'https://github.com/ToxicStoxm/LED-Cube-Control-Panel'
        info = new JMenuItem("Info");
        info.setToolTipText("https://github.com/ToxicStoxm/LED-Cube-Control-Panel");
        info.addActionListener(this);
        info.setOpaque(true);
        info.setFont(file.getFont());
        info.setBackground(Main.cm.l2);
        info.setForeground(Main.cm.l7);
        file.add(info);

        // adding 'Settings' menuItem to the 'File' menu
        // click event: openSettingsMenu
        // hotkey ALT+S
        settings = new JMenuItem("Settings");
        settings.addActionListener(this);
        settings.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.ALT_DOWN_MASK));
        settings.setToolTipText("Open settings");
        settings.setBackground(Main.cm.l2);
        settings.setOpaque(true);
        settings.setFont(file.getFont());
        settings.setForeground(Main.cm.l7);
        file.add(settings);

        // adding 'Server side settings' menuItem to the 'File' menu
        // click event: openServerSideSettingsMenu
        serverSettings = new JMenuItem("Server Settings");
        serverSettings.addActionListener(this);
        serverSettings.setToolTipText("Open the server side settings");
        serverSettings.setBackground(Main.cm.l2);
        serverSettings.setOpaque(true);
        serverSettings.setFont(file.getFont());
        serverSettings.setForeground(Main.cm.l7);
        file.add(serverSettings);

        // adding a separator to 'File' menu
        file.addSeparator();

        // adding 'Exit' menuItem to 'File' menu
        // click event: exitApplication
        // hotkey ALT+Q
        exit = new JMenuItem("Exit");
        exit.addActionListener(this);
        exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.ALT_DOWN_MASK));
        exit.setToolTipText("Quit the application");
        exit.setBackground(Main.cm.l2);
        exit.setForeground(Main.cm.l7);
        exit.setFont(file.getFont());
        exit.setOpaque(true);
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
            // exiting the application
            Main.mw.exitDialog();
        }
        if (source.equals(serverSettings)) {
            // opening server side settings
            Main.mw.serverSettingsMenu();
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {

    }
}
