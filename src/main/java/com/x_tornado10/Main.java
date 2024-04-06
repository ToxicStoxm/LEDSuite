package com.x_tornado10;

import com.x_tornado10.Events.EventManager;
import com.x_tornado10.Logger.Logger;
import com.x_tornado10.Main_Window.Main_Window;
import com.x_tornado10.Settings.Settings;
import com.x_tornado10.util.ColorManager;
import com.x_tornado10.util.Paths;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;
import java.io.IOException;

public class Main {
    public static Settings settings;
    public static Logger logger;
    private static long start;
    public static Main_Window mw;
    public static ColorManager cm;
    public static EventManager eventManager;
    public static void main(String[] args) {
        // program initialization
        // create timestamp that is used to calculate starting time
        start = System.currentTimeMillis();
        // create new settings class to hold config settings
        settings = new Settings();
        // create new logger instance
        logger = new Logger();
        // startup information displayed in the console upon opening the program
        logger.info("Welcome back!");
        logger.info("Starting Program...");
        // defining config file
        File file = new File(Paths.config);
        try {
            // checking if the config file doesn't already exist
            if (!file.exists()) {
                // if the config does not exist the parent directory is created
                // then a new config file is loaded with the default values from internal resources folder
                if (file.getParentFile().mkdirs())
                    logger.info("Successfully created parent directory for config file: " + file.getParentFile().getAbsolutePath());
                if (file.createNewFile()) {
                    logger.info("New config file was successfully created: " + file.getAbsolutePath());
                    settings.saveDefaultConfig();
                } else {
                    // if the config can for whatever reason not be created, display a warning message in the console
                    logger.warn("Config couldn't be created!");
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
            return;
        }

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
            return;
        }

        // creating event manager
        eventManager = new EventManager();

        // creating color manager
        cm = new ColorManager();

        // creating main window
        mw = new Main_Window();

    }
    // throw a new runtime error if something really severe happens
    public static void error() {
        throw new RuntimeException();
    }
    // display start message with starting duration
    public static void started() {
        // calculating time elapsed during startup and displaying it in the console
        long timeElapsed = System.currentTimeMillis() - start;
        logger.info("Successfully started program! (took " + timeElapsed / 1000 + "." + timeElapsed % 1000 + "s)");
    }
    // exiting program with specified status code
    public static void exit(int status) {
        Main.logger.info("Shutting down...");
        Main.logger.info("Goodbye!");
        Main.logger.info("Status code: " + status);
        System.exit(status);
    }

}
