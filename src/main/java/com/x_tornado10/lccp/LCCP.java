package com.x_tornado10.lccp;

import com.x_tornado10.lccp.event_handling.*;
import com.x_tornado10.lccp.event_handling.listener.EventListener;
import com.x_tornado10.lccp.logger.Logger;
import com.x_tornado10.lccp.settings.LocalSettings;
import com.x_tornado10.lccp.settings.ServerSettings;
import com.x_tornado10.lccp.ui.Window;
import com.x_tornado10.lccp.util.Paths;
import lombok.Getter;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.gnome.adw.Application;
import org.gnome.gio.ApplicationFlags;

import java.io.*;
import java.util.Properties;

import static java.awt.Toolkit.getDefaultToolkit;

@Getter
public class LCCP implements EventListener {
    public static LCCP instance;
    public static LocalSettings settings;
    public static ServerSettings server_settings;
    public static Logger logger;
    private static long start;
    private final Application app;
    public static EventManager eventManager;
    public static String version;
    public static Window mainWindow;

    public LCCP(String[] args) {
        instance = this;
        app = new Application("com.x_tornado10.lccp", ApplicationFlags.DEFAULT_FLAGS);
        app.onActivate(this::activate);
        app.onShutdown(() -> exit(0));
        app.run(args);
    }

    public static void main(String[] args) {
        // create timestamp that is used to calculate starting time
        start = System.currentTimeMillis();

        logicInit();

        new LCCP(args);
    }

    public static void logicInit() {
        // program initialization
        // create new settings and server_settings classes to hold config settings
        settings = new LocalSettings();
        server_settings = new ServerSettings();
        // create new logger instance
        logger = new Logger();
        // startup information displayed in the console upon opening the program
        logger.info("Welcome back!");
        logger.info("Starting Program...");
        String os_name = System.getProperty("os.name");
        String os_version = System.getProperty("os.version");

        logger.info("System environment: " + os_name + " " + os_version);

        if (os_name.toLowerCase().contains("windows")) {
            logger.warn("Our application does not have official Windows support. We do not fix any windows only bugs!");
            logger.warn("You will be ignored if you open an issue for a windows only bug! You can fork the repo though and fix the bug yourself!");
        }

        // getting the current application version using a version.properties file
        // the .properties file contains a maven variable that gets replaced once the application is compiled
        try (InputStream inputStream = LCCP.class.getResourceAsStream("/version.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);
            version = properties.getProperty("app.version");
        } catch (IOException e) {
            // if the version can't be loaded an error is displayed in the console
            // the program is also halted to prevent any further issues
            // if this fails its likely that this build is faulty
            LCCP.logger.fatal("Wasn't able to get app version!");
            LCCP.logger.warn("Application was halted!");
            LCCP.logger.warn("If this keeps happening please open an issue on GitHub!");
            LCCP.logger.warn("Please restart the application!");
            LCCP.exit(0);
        }


        // defining config file
        File file = new File(Paths.config);
        File file1 = new File(Paths.server_config);
        File file2 = new File(Paths.logFile);
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
            if (!file2.exists()) {
                if (file2.createNewFile()) {
                    logger.debug("New log config file was successfully created: " + file2.getAbsolutePath());
                } else {
                    logger.warn("Log file couldn't be created!");
                    logger.warn("Please restart the application to prevent wierd behaviour!");
                }
            } else {
                try (FileWriter writer = new FileWriter(file2, false)) {
                    // FileWriter with 'false' flag to truncate the file
                    writer.write("");
                    logger.debug("Existing log file contents erased: " + file2.getAbsolutePath());
                }
            }
        } catch (IOException | NullPointerException e) {
            // if any serious exceptions occur during config creation an error is displayed in the console
            // additionally the program is halted to prevent any further issues or unexpected behavior
            logger.error("Settings failed to load!");
            logger.warn("Application was halted!");
            logger.warn("If this keeps happening please open an issue on GitHub!");
            logger.warn("Please restart the application!");
            exit(0);
            return;
        }

        loadConfigsFromFile();

        // creating event manager
        eventManager = new EventManager();
        eventManager.registerEvents(settings);
        eventManager.registerEvents(server_settings);
    }

    public void activate() {
        eventManager.registerEvents(this);
        mainWindow = new Window(app);
        eventManager.registerEvents(mainWindow);
        mainWindow.present();
        started();
    }

    // display start message with starting duration
    public static void started() {
        // calculating time elapsed during startup and displaying it in the console
        long timeElapsed = System.currentTimeMillis() - start;
        logger.info("Successfully started program! (took " + timeElapsed / 1000 + "." + timeElapsed % 1000 + "s)");
    }


    // exiting program with specified status code
    public static void exit(int status) {
        eventManager.fireEvent(new Events.Shutdown("Shutdown"));
        LCCP.logger.info("Saving...");
        eventManager.fireEvent(new Events.Save("Shutdown - Save"));
        LCCP.logger.info("Successfully saved!");
        LCCP.logger.info("Shutting down...");
        LCCP.logger.info("Goodbye!");
        LCCP.logger.info("Status code: " + status);
        System.exit(status);
    }
    public static void sysBeep() {
        LCCP.logger.debug("Triggered system beep!");
        getDefaultToolkit().beep();
    }

    public static void updateRemoteConfig() {
        LCCP.logger.debug("Updating RemoteConfig...");
        LCCP.logger.debug("Successfully updatedRemoteConfig!");
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
            LCCP.logger.error("Failed to parse config.yaml!");
            LCCP.logger.warn("Application was halted!");
            LCCP.logger.warn("If this keeps happening please open an issue on GitHub!");
            LCCP.logger.warn("Please restart the application!");
            LCCP.exit(0);
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
            LCCP.logger.error("Failed to parse server_config.yaml!");
            LCCP.logger.warn("Application was halted!");
            LCCP.logger.warn("If this keeps happening please open an issue on GitHub!");
            LCCP.logger.warn("Please restart the application!");
            LCCP.exit(0);
        }
    }

    @EventHandler
    public void onReload(Events.Reload e) {
        logger.debug("Fulfilling reload request: " + e.message());
        mainWindow.setTitle(settings.getWindowTitle());
        mainWindow.setResizable(settings.isWindowResizeable());
        mainWindow.setAutoUpdate(settings.isAutoUpdateRemote());
    }
    @EventHandler
    public void onStartup(Events.Startup e) {
        logger.debug("Fulfilling startup request: " + e.message());
    }
    @EventHandler
    public void onSave(Events.Save e) {
        logger.debug("Fulfilling save request: " + e.message());
    }
    @EventHandler
    public void onShutdown(Events.Shutdown e) {
        logger.debug("Fulfilling shutdown request: " + e.message());
        logger.info("New log file was saved to: '" + Paths.logFile + "'");
    }
}
