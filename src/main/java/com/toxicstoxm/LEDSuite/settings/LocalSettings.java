package com.toxicstoxm.LEDSuite.settings;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.LEDSuite;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConversionException;
import org.apache.commons.configuration2.io.FileHandler;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * The `LocalSettings` class represents the local configuration settings for the application.
 * It extends the `Settings` class and provides methods for loading, saving, and managing
 * configuration settings stored in a YAML file.
 *
 * @since 1.0.0
 */
@Getter
@Setter
public class LocalSettings extends Settings {
    // Default settings
    private final Type type = Type.LOCAL;
    private final String name = "Main-Config";
    private boolean WindowResizeable = true;
    private int WindowDefWidth = 1280;
    private int WindowDefHeight = 720;
    private int LogLevel = 4;
    private String selectionDir = System.getProperty("user.home");
    private boolean DisplayStatusBar = false;
    private boolean CheckIPv4 = true;
    private boolean AutoPlayAfterUpload = true;
    private int NetworkingCommunicationClock = 10;
    private int StatusRequestClockPassive = 5000;
    private int StatusRequestClockActive = 1000;
    private boolean LogFileEnabled = false;
    private boolean LogFileLogLevelAll = true;
    private int LogFileMaxFiles = 1;
    private HashMap<String, Integer> LogColors = new HashMap<>(
            Map.of(
                    "FATAL", 0xB40000,
                    "ERROR", 0xFF0000,
                    "WARN", 0xE3A300,
                    "INFO", 0xFFFFFF,
                    "DEBUG", 0x075ED9,
                    "VERBOSE", 0xA016F4,
                    "STACKTRACE", 0x40404,
                    "TRACE", 0x009432
            )
    );
    private boolean LogColorCodingEnabled = true;
    private int StackTraceDepth = 11;

    private LocalSettings backup;

    /**
     * Saves the default configuration values from the internal resource folder to `config.yaml`.
     *
     * @throws IOException if an I/O error occurs.
     * @throws NullPointerException if the default configuration file cannot be found.
     * @since 1.0.0
     */
    @Override
    public void saveDefaultConfig() throws IOException, NullPointerException {
        LEDSuite.logger.debug("Loading default config values...");
        LEDSuite.logger.debug("Note: this only happens if config.yaml does not exist or couldn't be found!");
        LEDSuite.logger.debug("If your settings don't work and this message is shown");
        LEDSuite.logger.debug(Constants.Messages.WARN.OPEN_GITHUB_ISSUE);

        // Get the internal resource folder and default config values
        URL url = getClass().getClassLoader().getResource("config.yaml");

        // If the path is null or not found, an exception is thrown
        if (url == null) throw new NullPointerException();

        // Try to open a new input stream to read the default values
        try (InputStream inputStream = url.openStream()) {
            // Defining config.yaml file to save the values to
            File outputFile = new File(Constants.File_System.config);

            // Try to open a new output stream to save the values to the new config file
            try (OutputStream outputStream = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;

                // Write the read bytes using the stored length in bytesRead
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        }
        LEDSuite.logger.debug("Successfully loaded default config values!");
    }

    /**
     * Loads the configuration settings from the provided config and stores them in an instance of this class.
     *
     * @param config The `YAMLConfiguration` object containing the configuration settings.
     * @since 1.0.0
     */
    @Override
    public void load(YAMLConfiguration config) {
        super.load(config);
        LEDSuite.logger.debug("Loading config values to memory...");
        try {
            // Setting values to parsed config values
            this.WindowResizeable = config.getBoolean(Constants.Config.WINDOW_RESIZABLE);

            // Handle potential ConversionExceptions gracefully
            try {
                this.WindowDefWidth = config.getInt(Constants.Config.WINDOW_DEFAULT_WIDTH);
                this.WindowDefHeight = config.getInt(Constants.Config.WINDOW_DEFAULT_HEIGHT);
            } catch (ConversionException e) {
                LEDSuite.logger.warn("Error while parsing Window-Default-Height and Window-Default-Width! Not a valid Number!" + LEDSuite.logger.getErrorMessage(e));
            }

            try {
                this.LogLevel = config.getInt(Constants.Config.LOG_LEVEL);
            } catch (ConversionException e) {
                LEDSuite.logger.warn("Error while parsing Log-Level! Not a valid Number!" + LEDSuite.logger.getErrorMessage(e));
            }

            try {
                double temp = config.getDouble(Constants.Config.NETWORK_COMMUNICATION_CLOCK);
                this.NetworkingCommunicationClock = (int) (Math.round((temp * 1000)));
            } catch (ClassCastException | ConversionException e) {
                LEDSuite.logger.warn("Error while parsing NetworkingCommunicationClock! Not a valid time argument (seconds)!" + LEDSuite.logger.getErrorMessage(e));
            }

            try {
                LogFileMaxFiles = config.getInt(Constants.Config.LOG_FILE_MAX_FILES);
            } catch (ConversionException e) {
                LEDSuite.logger.warn("Error while parsing LogFileMaxFiles! Not a valid time argument (seconds)!" + LEDSuite.logger.getErrorMessage(e));
            }

            try {
                double temp = config.getDouble(Constants.Config.STATUS_REQUEST_CLOCK_ACTIVE);
                this.StatusRequestClockActive = (int) (Math.round((temp * 1000)));
            } catch (ClassCastException | ConversionException e) {
                LEDSuite.logger.warn("Error while parsing StatusRequestClockActive! Not a valid time argument (seconds)!" + LEDSuite.logger.getErrorMessage(e));
            }

            try {
                double temp = config.getDouble(Constants.Config.STATUS_REQUEST_CLOCK_PASSIVE);
                this.StatusRequestClockPassive = (int) (Math.round((temp * 1000)));
            } catch (ClassCastException | ConversionException e) {
                LEDSuite.logger.warn("Error while parsing StatusRequestClockPassive! Not a valid time argument (seconds)!" + LEDSuite.logger.getErrorMessage(e));
            }

            try {
                this.StackTraceDepth = config.getInt(Constants.Config.STACK_TRACE_DEPTH);
            } catch (ClassCastException | ConversionException e) {
                LEDSuite.logger.warn("Error while parsing StackTraceDepth! This needs to be a numeric value!" + LEDSuite.logger.getErrorMessage(e));
            }

            // Setting the remaining values
            this.selectionDir = config.getString(Constants.Config.SELECTION_DIR);
            this.DisplayStatusBar = config.getBoolean(Constants.Config.DISPLAY_STATUS_BAR);
            this.CheckIPv4 = config.getBoolean(Constants.Config.CHECK_IPV4);
            this.AutoPlayAfterUpload = config.getBoolean(Constants.Config.AUTO_PLAY_AFTER_UPLOAD);
            this.LogFileEnabled = config.getBoolean(Constants.Config.LOG_FILE_ENABLED);
            this.LogFileLogLevelAll = config.getBoolean(Constants.Config.LOG_FILE_LOG_LEVEL_ALL);
            this.LogColorCodingEnabled = config.getBoolean(Constants.Config.COLOR_CODING_ENABLED);

            // Ensure the LogFileMaxFiles value is at least 1 if LogFileEnabled is true
            if (LogFileMaxFiles < 1) LogFileMaxFiles = LogFileEnabled ? 1 : 0;

            HashMap<String, Integer> tmp = new HashMap<>(LogColors);

            for (Map.Entry<String, Integer> entry : LogColors.entrySet()) {
                String key = entry.getKey();
                Integer hex = config.getInteger(Constants.Config.COLORS_SECTION + Constants.Config.SEPARATOR + key, -1);
                if (hex != null) {
                    if (hex != -1) tmp.put(key, hex);
                    else  LEDSuite.logger.warn("Invalid color for log level '" + key + "': '" + hex + "'!");
                } else LEDSuite.logger.warn("Color for log level '" + key + "' mustn't be empty!");
            }
            this.LogColors.putAll(tmp);

            LEDSuite.logger.debug("Loaded config values to memory!");
        } catch (NoSuchElementException e) {
            LEDSuite.logger.fatal("Error while parsing config! Settings / values missing! You're probably using an old config file!");
            LEDSuite.logger.warn("Your can reset the config file by starting the application with the -R CLI argument!");
            LEDSuite.getInstance().exit(3);
        }
    }

    /**
     * Copies the settings of another `Settings` instance to this instance.
     *
     * @param settings1 The `Settings` instance to copy from.
     * @since 1.0.0
     */
    @Override
    public void copy(Settings settings1) {
        copyImpl(settings1, true);
    }

    /**
     * Copies the settings of another `Settings` instance to this instance with an option to log the process.
     *
     * @param settings1 The `Settings` instance to copy from.
     * @param log       Whether to log the copy process.
     * @since 1.0.0
     */
    public void copyImpl(Settings settings1, boolean log) {
        // Check if the other settings class type is compatible
        if (settings1.getType() != type) {
            if (settings1.getType() != Type.UNDEFINED) {
                LEDSuite.logger.error("Can't copy settings from " + settings1.getName() + " Type: " + settings1.getType() + " to " + getName() + " Type: " + type);
                return;
            }
            LEDSuite.logger.info("Can't confirm settings type! Type = UNDEFINED");
        }

        // Casting to a compatible settings type after check
        LocalSettings settings = (LocalSettings) settings1;
        if (log) LEDSuite.logger.debug("Loading settings from " + settings.getName() + "...");

        // Copying settings
        this.WindowResizeable = settings.isWindowResizeable();
        this.WindowDefWidth = settings.getWindowDefWidth();
        this.WindowDefHeight = settings.getWindowDefHeight();
        this.LogLevel = settings.getLogLevel();
        this.selectionDir = settings.getSelectionDir();
        this.DisplayStatusBar = settings.DisplayStatusBar;
        this.NetworkingCommunicationClock = settings.NetworkingCommunicationClock;
        this.CheckIPv4 = settings.CheckIPv4;
        this.AutoPlayAfterUpload = settings.AutoPlayAfterUpload;
        this.StatusRequestClockPassive = settings.StatusRequestClockPassive;
        this.StatusRequestClockActive = settings.StatusRequestClockActive;
        this.LogFileMaxFiles = settings.LogFileMaxFiles;
        this.LogFileEnabled = settings.LogFileEnabled;
        this.LogFileLogLevelAll = settings.LogFileLogLevelAll;
        this.LogColors = settings.LogColors;
        this.LogColorCodingEnabled = settings.LogColorCodingEnabled;
        this.StackTraceDepth = settings.getStackTraceDepth();

        if (log) LEDSuite.logger.debug("Successfully loaded settings from " + settings.getName() + "!");
        if (log) LEDSuite.logger.debug(getName() + " now inherits all values from " + settings.getName());
    }

    /**
     * Saves the current settings to disk.
     *
     * @since 1.0.0
     */
    @Override
    public void save() {
        // Check if this instance is the main configuration settings
        if (!this.equals(LEDSuite.settings)) return;

        // Check for changes to avoid unnecessary save
        if (this.equals(backup)) {
            LEDSuite.logger.debug("Didn't save " + name + " because nothing changed!");
            return;
        }
        LEDSuite.logger.debug("Saving " + name + " values to config.yaml...");

        // Loading config file
        YAMLConfiguration conf;
        FileHandler fH;
        TreeMap<Integer, String> comments;
        try {
            conf = new YAMLConfiguration();
            fH = new FileHandler(conf);
            fH.load(Constants.File_System.config);
            comments = new TreeMap<>(CommentPreservation.extractComments(Constants.File_System.config));
        } catch (ConfigurationException e) {
            LEDSuite.logger.warn("Error occurred while writing config values to config.yaml!");
            return;
        }

        // Writing config settings to file
        try {
            // Setting properties in the configuration object
            conf.setProperty(Constants.Config.WINDOW_RESIZABLE, WindowResizeable);
            conf.setProperty(Constants.Config.WINDOW_DEFAULT_WIDTH, WindowDefWidth);
            conf.setProperty(Constants.Config.WINDOW_DEFAULT_HEIGHT, WindowDefHeight);
            conf.setProperty(Constants.Config.LOG_LEVEL, LogLevel);
            conf.setProperty(Constants.Config.STACK_TRACE_DEPTH, StackTraceDepth);
            for (Map.Entry<String, Integer> entry : LogColors.entrySet()) {
                conf.setProperty(
                        Constants.Config.COLORS_SECTION + Constants.Config.SEPARATOR + entry.getKey(),
                        "0x" + Integer.toHexString(entry.getValue()).toUpperCase()
                );
            }
            conf.setProperty(Constants.Config.LOG_FILE_ENABLED, LogFileEnabled);
            conf.setProperty(Constants.Config.LOG_FILE_LOG_LEVEL_ALL, LogFileLogLevelAll);
            conf.setProperty(Constants.Config.LOG_FILE_MAX_FILES, LogFileMaxFiles);
            conf.setProperty(Constants.Config.CHECK_IPV4, CheckIPv4);
            conf.setProperty(Constants.Config.NETWORK_COMMUNICATION_CLOCK, ((double) NetworkingCommunicationClock / 1000));
            conf.setProperty(Constants.Config.STATUS_REQUEST_CLOCK_PASSIVE, ((double) StatusRequestClockPassive / 1000));
            conf.setProperty(Constants.Config.STATUS_REQUEST_CLOCK_ACTIVE, ((double) StatusRequestClockActive / 1000));
            conf.setProperty(Constants.Config.SELECTION_DIR, selectionDir);
            conf.setProperty(Constants.Config.DISPLAY_STATUS_BAR, DisplayStatusBar);
            conf.setProperty(Constants.Config.AUTO_PLAY_AFTER_UPLOAD, AutoPlayAfterUpload);
            conf.setProperty(Constants.Config.COLOR_CODING_ENABLED, LogColorCodingEnabled);




            // Saving settings to disk
            fH.save(Constants.File_System.config);
            CommentPreservation.insertComments(Constants.File_System.config, comments);
        } catch (ConfigurationException e) {
            LEDSuite.logger.warn("Something went wrong while saving the config values for config.yaml!" + LEDSuite.logger.getErrorMessage(e));
            return;
        } catch (IOException e) {
            LEDSuite.logger.warn("Something went wrong while saving the config comments for config.yaml!" + LEDSuite.logger.getErrorMessage(e));
            return;
        }

        LEDSuite.logger.debug("Successfully saved server-config values to config.yaml!");
    }

    /**
     * Creates a clone for unnecessary saving check.
     *
     * @since 1.0.0
     */
    @Override
    public void startup() {
        // Cloning the current settings for comparison later
        this.backup = new LocalSettings().cloneS();
    }

    /**
     * Creates a clone of this config class.
     *
     * @return A new instance of `LocalSettings` with the same settings.
     * @since 1.0.0
     */
    @Override
    public LocalSettings cloneS() {
        // Creating a new instance of LocalSettings and copying the current settings to it
        LocalSettings settings1 = new LocalSettings();
        settings1.copy(LEDSuite.settings);
        return settings1;
    }

    /**
     * Sets the selection directory and reloads the configuration.
     *
     * @param selectionDir The new selection directory.
     * @since 1.0.0
     */
    public void setSelectionDir(String selectionDir) {
        // Updating the selection directory and reloading configuration
        this.selectionDir = selectionDir;
        reload("selectionDir -> " + selectionDir);
    }

    /**
     * Sets the display status bar flag and reloads the configuration.
     *
     * @param displayStatusBar The new display status bar flag.
     * @since 1.0.0
     */
    public void setDisplayStatusBar(boolean displayStatusBar) {
        // Updating the display status bar flag and reloading configuration
        DisplayStatusBar = displayStatusBar;
        reload("DisplayStatusBar -> " + displayStatusBar);
    }

    /**
     * Sets the autoplay after upload flag and reloads the configuration.
     *
     * @param autoPlayAfterUpload The new autoplay after uploads flag.
     * @since 1.0.0
     */
    public void setAutoPlayAfterUpload(boolean autoPlayAfterUpload) {
        // Updating the autoplay after upload flag and reloading configuration
        AutoPlayAfterUpload = autoPlayAfterUpload;
        reload("AutoPlayAfterUpload -> " + autoPlayAfterUpload);
    }

    /**
     * Gets the selection directory, resolving any system property placeholders.
     *
     * @return The resolved selection directory.
     * @since 1.0.0
     */
    public String getSelectionDir() {
        // Check if the selection directory contains any system property placeholders
        if (selectionDir.contains("%")) {
            String temp = selectionDir.replaceAll("%", "");
            try {
                // Resolve and return the system property
                return System.getProperty(temp);
            } catch (Exception e) {
                LEDSuite.logger.debug("System property placeholder: '" + selectionDir + "'");
                LEDSuite.logger.debug("System property parsed: '" + temp + "'");
                LEDSuite.logger.warn("Invalid system property: '" + temp + "'");
                return System.getProperty("user.home");
            }
        }
        return selectionDir;
    }

    /**
     * Checks if the current settings are equal to another settings class.
     *
     * @param object The object to compare with.
     * @return True if the settings are equal, false otherwise.
     * @since 1.0.0
     */
    @Override
    public boolean equals(Object object) {
        // Return true if the objects are the same instance
        if (this == object) {
            return true;
        }
        // Return false if the other object is null or of a different class
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        // Cast the other object to LocalSettings and compare field values
        LocalSettings other = (LocalSettings) object;
        return WindowResizeable == other.WindowResizeable &&
                WindowDefWidth == other.WindowDefWidth &&
                WindowDefHeight == other.WindowDefHeight &&
                LogLevel == other.LogLevel &&
                DisplayStatusBar == other.DisplayStatusBar &&
                CheckIPv4 == other.CheckIPv4 &&
                AutoPlayAfterUpload == other.AutoPlayAfterUpload &&
                LogFileEnabled == other.LogFileEnabled &&
                LogFileLogLevelAll == other.LogFileLogLevelAll &&
                LogFileMaxFiles == other.LogFileMaxFiles &&
                StatusRequestClockPassive == other.StatusRequestClockPassive &&
                StatusRequestClockActive == other.StatusRequestClockActive &&
                LogColorCodingEnabled == other.LogColorCodingEnabled &&
                StackTraceDepth == other.StackTraceDepth &&
                Objects.equals(selectionDir, other.selectionDir) &&
                Objects.equals(NetworkingCommunicationClock, other.NetworkingCommunicationClock) &&
                Objects.equals(LogColors, other.LogColors);
    }

    /**
     * Generates the hash code for the current settings.
     *
     * @return The hash code for the current settings.
     * @since 1.0.0
     */
    @Override
    public int hashCode() {
        // Generate a hash code based on the field values
        return Objects.hash(WindowResizeable, WindowDefHeight,
                WindowDefWidth, LogLevel, selectionDir,
                DisplayStatusBar, CheckIPv4, AutoPlayAfterUpload,
                NetworkingCommunicationClock, StatusRequestClockPassive,
                StatusRequestClockActive, LogFileEnabled,
                LogFileLogLevelAll, LogFileMaxFiles, LogColors,
                LogColorCodingEnabled, StackTraceDepth
        );
    }
}
