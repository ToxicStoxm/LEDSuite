package com.x_tornado10;

import com.x_tornado10.Events.EventListener;
import com.x_tornado10.Events.EventManager;
import com.x_tornado10.Events.Events.Event;
import com.x_tornado10.Events.Events.ReloadEvent;
import com.x_tornado10.Events.Events.SaveEvent;
import com.x_tornado10.Events.Events.StartupEvent;
import com.x_tornado10.Logger.Logger;
import com.x_tornado10.Main_Window.Main_Window;
import com.x_tornado10.Settings.Local_Settings;
import com.x_tornado10.Settings.Server_Settings;
import com.x_tornado10.Settings.Settings;
import com.x_tornado10.util.ColorManager;
import com.x_tornado10.util.Paths;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.gnome.adw.AboutDialog;
import org.gnome.adw.Application;
import org.gnome.adw.ApplicationWindow;
import org.gnome.adw.HeaderBar;
import org.gnome.gio.ApplicationFlags;
import org.gnome.adw.*;
import org.gnome.gtk.*;
import org.gnome.gtk.Label;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static java.awt.Toolkit.getDefaultToolkit;


public class Main {
    public static Local_Settings settings;
    public static Server_Settings server_settings;
    public static Logger logger;
    private static long start;
    private final Application app;


    public static void main(String[] args) {
        start = System.currentTimeMillis();
        logger = new Logger();
        logger.info("Welcome back!");
        logger.info("Starting Program...");

        main_two();
        new Main(args);
        started();
    }

    public Main(String[] args) {
        app = new Application("com.x_tornado10.led_cube_control_panel", ApplicationFlags.DEFAULT_FLAGS);
        app.onActivate(this::activate);
        app.run(args);
    }

    public void activate() {
        var window = new ApplicationWindow(app);
        window.setTitle(settings.getWindowTitleRaw().replace(Paths.Placeholders.VERSION, ""));
        window.setDefaultSize(1280, 720);

        var box = Box.builder()
                .setOrientation(Orientation.VERTICAL)
                .build();

        var headerBar = new HeaderBar();

        var aDialog = new AboutDialog();
        aDialog.setDevelopers(new String[]{"x_Tornado10"});
        aDialog.setVersion(version);
        aDialog.setLicense("GPL-3.0");
        aDialog.setDeveloperName("x_Tornado10");
        aDialog.setWebsite("https://github.com/ToxicStoxm/LED-Cube-Control-Panel");
        aDialog.setApplicationName(settings.getWindowTitleRaw().replace(Paths.Placeholders.VERSION, ""));

        var sbutton = new ToggleButton();
        var toastOverlay = new ToastOverlay();
        sbutton.setIconName("system-search-symbolic");
        sbutton.onToggled(() -> {
            var wipToast = new Toast("Work in progress!");
            wipToast.setTimeout(1);
            toastOverlay.addToast(wipToast);
        });



        var mbutton = new MenuButton();
        mbutton.setAlwaysShowArrow(false);
        mbutton.setIconName("open-menu-symbolic");

        var listBox = new ListBox();
        var settingsRow = ListBoxRow.builder()
                .setChild(Label.builder()
                        .setLabel("Settings")
                        .setHalign(Align.START)
                        .setMarginEnd(10)
                        .setMarginBottom(5)
                        .build())
                .setName("settings")
                .setSelectable(false)
                .build();
        var statusRow = ListBoxRow.builder()
                .setChild(Label.builder()
                        .setLabel("Status")
                        .setHalign(Align.START)
                        .setMarginEnd(10)
                        .setMarginBottom(5)
                        .build())
                .setName("status")
                .setSelectable(false)
                .build();
        var aboutRow = ListBoxRow.builder()
                .setChild(Label.builder()
                        .setLabel("About")
                        .setHalign(Align.START)
                        .setMarginEnd(10)
                        .build())
                .setName("about")
                .setSelectable(false)
                .build();

        listBox.setSelectionMode(SelectionMode.SINGLE);

        listBox.append(statusRow);
        listBox.append(settingsRow);
        listBox.append(aboutRow);

        listBox.onRowActivated(e -> {
            if (e == null) return;
            switch (e.getName()) {
                case "status" -> toastOverlay.addToast(new Toast("Status"));
                case "settings" -> toastOverlay.addToast(new Toast("Settings"));
                case "about" -> aDialog.present(window);
            }
        });

        listBox.onRowSelected(e -> {

        });

        var popover = new Popover();
        popover.setChild(listBox);
        mbutton.setPopover(popover);

        headerBar.packStart(sbutton);
        headerBar.packEnd(mbutton);

        box.append(headerBar);
        box.append(toastOverlay);

        window.setContent(box);
        window.present();
    }

    public static Main_Window mw;
    public static ColorManager cm;
    public static EventManager eventManager;
    public static String version;
    public static void main_two() {
        // program initialization
        // create timestamp that is used to calculate starting time
        start = System.currentTimeMillis();
        // create new settings and server_settings classes to hold config settings
        settings = new Local_Settings();
        server_settings = new Server_Settings();
        // create new logger instance
        logger = new Logger();
        // startup information displayed in the console upon opening the program
        logger.info("Welcome back!");
        logger.info("Starting Program...");
        String os_name = System.getProperty("os.name");
        String os_version = System.getProperty("os.version");

        logger.info("System environment: " + os_name + " " + os_version);
        boolean windows = false;

        if (os_name.toLowerCase().contains("windows")) {
            logger.warn("Our application does not have official Windows support. We do not fix any windows only bugs!");
            logger.warn("You will be ignored if you open an issue for a windows only bug! You can fork the repo though and fix the bug yourself!");
            windows = true;
        }

        // getting the current application version using a version.properties file
        // the .properties file contains a maven variable that gets replaced once the application is compiled
        try (InputStream inputStream = Main.class.getResourceAsStream("/version.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);
            version = properties.getProperty("app.version");
        } catch (IOException e) {
            // if the version can't be loaded an error is displayed in the console
            // the program is also halted to prevent any further issues
            // if this fails its likely that this build is faulty
            Main.logger.fatal("Wasn't able to get app version!");
            Main.logger.warn("Application was halted!");
            Main.logger.warn("If this keeps happening please open an issue on GitHub!");
            Main.logger.warn("Please restart the application!");
            Main.logger.fatal_popup("Wasn't able to get app version! Please restart the application!");
            Main.logger.warn_popup("If this keeps happening please open an issue on GitHub!");
            Main.exit(0);
        }


        // defining config file
        File file = new File(Paths.config);
        File file1 = new File(Paths.server_config);
        try {
            // checking if the config file doesn't already exist
            if (!file.exists()) {
                // if the config does not exist the parent directory is created
                // then a new config file is loaded with the default values from internal resources folder
                if (file.getParentFile().mkdirs())
                    logger.debug("Successfully created parent directory for config file: " + file.getParentFile().getAbsolutePath());
                if (file.createNewFile()) {
                    logger.debug("New config file was successfully created: " + file.getAbsolutePath());
                    settings.saveDefaultConfig();
                } else {
                    // if the config can for whatever reason not be created, display a warning message in the console
                    logger.warn("Config couldn't be created!");
                    logger.warn("Please restart the application to prevent wierd behaviour!");
                }
            }
            // checking if the server side config file does not already exist
            if (!file1.exists()) {
                // if the config does not exist it is created and the default values are loaded from the internal resources folder
                if (file1.createNewFile()) {
                    logger.debug("New server config file was successfully created: " + file1.getAbsolutePath());
                    server_settings.saveDefaultConfig();
                } else {
                    // if the config couldn't be loaded for whatever reason
                    logger.warn("Server config couldn't be created!");
                    logger.warn("Please restart the application to prevent wierd behaviour!");
                }
            }
        } catch (IOException | NullPointerException e) {
            // if any serious exceptions occur during config creation an error is displayed in the console
            // additionally the program is halted to prevent any further issues or unexpected behavior
            logger.error("Settings failed to load!");
            logger.warn("Application was halted!");
            logger.warn("If this keeps happening please open an issue on GitHub!");
            logger.warn("Please restart the application!");
            logger.error_popup("Settings failed to load! Please restart the application!");
            logger.warn_popup("If this keeps happening please open an issue on GitHub!");
            exit(0);
            return;
        }

        loadConfigsFromFile();

        // creating event manager
        eventManager = new EventManager();

        eventManager.addEventListener(settings);
        eventManager.addEventListener(server_settings);

        eventManager.newEvent(new StartupEvent());

        // creating color manager
        cm = new ColorManager();

        // creating main window
        //mw = new Main_Window(windows, true);


    }

    public static void loadConfigsFromFile() {
        File file = new File(Paths.config);
        File file1 = new File(Paths.server_config);
        try {
            // parsing config and loading the values from storage (Default: ./LED-Cube-Control-Panel/config.yaml)
            // using Apache-Commons-Config (and dependencies like snakeyaml and commons-beanutils)
            Configurations configs = new Configurations();
            FileBasedConfiguration config = configs.properties(file);
            // settings are loaded into an instance of the settings class, so they can be used during runtime without any IO-Calls
            settings.load(config);
        } catch (ConfigurationException e) {
            // if any errors occur during config parsing an error is displayed in the console
            // the program is halted to prevent any further unwanted behavior
            logger.error("Failed to parse config.yaml!");
            logger.warn("Application was halted!");
            logger.warn("If this keeps happening please open an issue on GitHub!");
            logger.warn("Please restart the application!");
            logger.error_popup("Failed to parse config.yaml! Please restart the application!");
            logger.warn_popup("If this keeps happening please open an issue on GitHub!");
            exit(0);
            return;
        }

        try {
            // parsing config and loading the values from storage (Default: ./LED-Cube-Control-Panel/server_config.yaml)
            // using Apache-Commons-Config (and dependencies like snakeyaml and commons-beanutils)
            Configurations configs = new Configurations();
            FileBasedConfiguration server_config = configs.properties(file1);
            // settings are loaded into an instance of the settings class, so they can be used during runtime without any IO-Calls
            server_settings.load(server_config);
        } catch (ConfigurationException e) {
            // if any errors occur during config parsing an error is displayed in the console
            // the program is halted to prevent any further unwanted behavior
            logger.error("Failed to parse server_config.yaml!");
            logger.warn("Application was halted!");
            logger.warn("If this keeps happening please open an issue on GitHub!");
            logger.warn("Please restart the application!");
            logger.error_popup("Failed to parse server_config.yaml! Please restart the application!");
            logger.warn_popup("If this keeps happening please open an issue on GitHub!");
            exit(0);
        }
    }
    // display start message with starting duration
    public static void started() {
        // calculating time elapsed during startup and displaying it in the console
        long timeElapsed = System.currentTimeMillis() - start;
        logger.info("Successfully started program! (took " + timeElapsed / 1000 + "." + timeElapsed % 1000 + "s)");
    }


    // exiting program with specified status code
    public static void exit(int status) {
        Main.logger.info("Saving...");
        eventManager.newEvent(new SaveEvent());
        Main.logger.info("Successfully saved!");
        Main.logger.info("Shutting down...");
        Main.logger.info("Goodbye!");
        Main.logger.info("Status code: " + status);
        System.exit(status);
    }
    public static void restart() {
        mw.setVisible(false);
        mw.dispose();
        mw = new Main_Window(mw.windows, false);
    }

    public static void restartWithDest(Settings.Type restartDest) {
        restart();
        switch (restartDest) {
            case LOCAL -> mw.settingsMenu();
            case SERVER -> mw.serverSettingsMenu();
        }
    }
    public static void sysBeep() {
        Main.logger.debug("Triggered system beep!");
        getDefaultToolkit().beep();
    }

    public static class main_listener implements EventListener {

        @Override
        public void onEvent(Event event) {
            switch (event.getType()) {
                case RELOAD -> {
                    ReloadEvent e = (ReloadEvent) event;
                    cm.revalidate();
                    if (e.hasRebootDest()) restartWithDest(e.getRebootDest());
                    else restart();
                }
            }
        }
    }
}
