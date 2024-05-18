package com.x_tornado10.lccp.Settings;

import com.x_tornado10.lccp.LCCP;
import com.x_tornado10.lccp.util.Paths;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConversionException;

import java.io.*;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.Objects;

// settings class to store config settings on runtime
@Setter
@Getter
public class Local_Settings extends Settings {
    // default settings
    private Type type = Type.LOCAL;
    private String name = "Main-Config";
    private String WindowTitle = "LED-Cube-Control-Panel";
    private boolean WindowResizeable = false;
    private int WindowWidth = 1280;
    private int WindowHeight = 720;
    private int LogLevel = 4;
    private String selectionDir = System.getProperty("user.home");

    private Local_Settings backup;

    // get the default configuration values from internal resource folder and save them to config.yaml
    @Override
    public void saveDefaultConfig() throws IOException, NullPointerException {
        LCCP.logger.debug("Loading default config values...");
        LCCP.logger.debug("Note: this only happens if config.yaml does not exist or couldn't be found!");
        LCCP.logger.debug("If your settings don't work and this message is shown please seek support on the projects GitHub page: " + Paths.Links.Project_GitHub);
        // get the internal resource folder and default config values
        URL url = getClass().getClassLoader().getResource("config.yaml");
        // if the path is null or not found an exception is thrown
        if (url == null) throw new NullPointerException();
        // try to open a new input stream to read the default values
        try(InputStream inputStream = url.openStream()) {
            // defining config.yaml file to save the values to
            File outputFile = new File(Paths.config);
            // try to open a new output stream to save the values to the new config file
            try (OutputStream outputStream = new FileOutputStream(outputFile)) {

                byte[] buffer = new byte[1024];
                // if the buffer isn't empty the write function writes the read bytes using the stored length in bytesRead var below
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        }
        LCCP.logger.debug("Successfully loaded default config values!");
    }
    // load the config settings from config.yaml and store them in an instance of this class
    @Override
    public void load(FileBasedConfiguration config) {
        LCCP.logger.debug("Loading config values to memory...");
        try {
            this.WindowTitle = config.getString(Paths.Config.WINDOW_TITLE);
            // setting values to parsed config values
            this.WindowResizeable = config.getBoolean(Paths.Config.WINDOW_RESIZABLE);

            // handle potential ConversionExceptions gracefully
            try {
                this.WindowWidth = config.getInt(Paths.Config.WINDOW_INITIAL_WIDTH);
                this.WindowHeight = config.getInt(Paths.Config.WINDOW_INITIAL_HEIGHT);
            } catch (ConversionException e) {
                LCCP.logger.error("Error while parsing Window-Initial-Height and Window-Initial-Width! Not a valid Number!");
                LCCP.logger.warn("There was an error while reading the config file, some settings may be broken!");
            }

            try {
                this.LogLevel = config.getInt(Paths.Config.LOG_LEVEL);
            } catch (ConversionException e) {
                LCCP.logger.error("Error while parsing Window-Spawn-X and Window-Spawn-Y! Not a valid Number!");
                LCCP.logger.warn("There was an error while reading the config file, some settings may be broken!");
            }

            String dir = config.getString(Paths.Config.SELECTION_DIR);
            if (dir.contains("%")) {
                String temp = dir.replace("%", "");
                System.getProperty(temp);
            } else this.selectionDir = dir;

            LCCP.logger.debug("Loaded config values to memory!");
        } catch (NoSuchElementException e){
            LCCP.logger.error("Error while parsing config! Settings / values missing! Your probably using an old config file!");
            LCCP.logger.warn("Program halted to prevent any further errors!");
            LCCP.logger.warn("Please delete the old config file from your .config folder and restart the application!");
            //Main.logger.error_popup("Error while parsing config! Settings / values missing! Your probably using an old config file!");
            //Main.logger.warn_popup("Please delete the old config file from your .config folder and restart the application!");
            LCCP.exit(0);
        }
    }
    public String getWindowTitle() {
        return LCCP.version == null ? WindowTitle : WindowTitle.replace(Paths.Placeholders.VERSION, LCCP.version);
    }
    public String getWindowTitleRaw() {
        return WindowTitle;
    }

    // copy the settings of another settings class
    @Override
    public void copy(Settings settings1) {
        // check if other settings class type is compatible
        if (settings1.getType() != type) {
            if (settings1.getType() != Type.UNDEFINED) {
                LCCP.logger.error("Can't copy settings from " + settings1.getName() + " Type: " + settings1.getType() + " to " + getName() + " Type: " + type);
                return;
            }
            LCCP.logger.info("Can't confirm settings type! Type = UNDEFINED");
        }
        // casting to compatible settings type after check
        Local_Settings settings = (Local_Settings) settings1;
        LCCP.logger.debug("Loading settings from " + settings.getName() + "...");
        // copying settings
        this.WindowTitle = settings.getWindowTitleRaw();
        this.WindowResizeable = settings.isWindowResizeable();
        this.WindowWidth = settings.getWindowWidth();
        this.WindowHeight = settings.getWindowHeight();
        this.LogLevel = settings.getLogLevel();
        this.selectionDir = settings.getSelectionDir();
        LCCP.logger.debug("Successfully loaded settings from " + settings.getName() + "!");
        LCCP.logger.debug(getName() + " now inherits all values from " + settings.getName());
    }

    // saving current settings to disk
    @Override
    public void save() {
        // check for changes to avoid unnecessary save
        if (this.equals(backup)) {
            LCCP.logger.debug("Didn't save " + name + " because nothing changed!");
            return;
        }
        LCCP.logger.debug("Saving " + name + " values to config.yaml...");
        // loading config file
        File file = new File(Paths.config);
        Configurations configs = new Configurations();
        FileBasedConfiguration config;
        Parameters parameters = new Parameters();
        // loading config file to memory
        try {
            config = configs.properties(file);
        } catch (ConfigurationException e) {
            LCCP.logger.error("Error occurred while writing config values to config.yaml!");
            LCCP.logger.warn("Please restart the application to prevent further errors!");
            return;
        }

        // initializing new config builder
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                new FileBasedConfigurationBuilder<FileBasedConfiguration>(config.getClass())
                        .configure(parameters.fileBased()
                                .setFile(file));

        // writing config settings to file
        try {
            builder.getConfiguration().setProperty(Paths.Config.WINDOW_TITLE, WindowTitle);
            builder.getConfiguration().setProperty(Paths.Config.WINDOW_RESIZABLE, WindowResizeable);
            builder.getConfiguration().setProperty(Paths.Config.WINDOW_INITIAL_WIDTH, WindowWidth);
            builder.getConfiguration().setProperty(Paths.Config.WINDOW_INITIAL_HEIGHT, WindowHeight);
            builder.getConfiguration().setProperty(Paths.Config.LOG_LEVEL, LogLevel);
            builder.getConfiguration().setProperty(Paths.Config.SELECTION_DIR, selectionDir);
            // saving settings
            builder.save();
        } catch (ConfigurationException e)  {
            LCCP.logger.error("Something went wrong while saving the config values for config.yaml!");
            LCCP.logger.warn("Please restart the application to prevent further errors!");
            LCCP.logger.warn("Previously made changes to the config may be lost!");
            LCCP.logger.warn("If this message appears on every attempt to save config changes please open an issue on GitHub!");
            return;
        }

        LCCP.logger.debug("Successfully saved server-config values to config.yaml!");
    }

    // creating clone for unnecessary saving check
    @Override
    public void startup() {
        this.backup = new Local_Settings().cloneS();
    }

    // creating a clone of this config class
    @Override
    public Local_Settings cloneS() {
        Local_Settings settings1 = new Local_Settings();
        settings1.copy(LCCP.settings);
        return settings1;
    }

    // used to check if current settings equal another settings class
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Local_Settings other = (Local_Settings) obj;
        return WindowResizeable == other.WindowResizeable &&
                WindowWidth == other.WindowWidth &&
                WindowHeight == other.WindowHeight &&
                LogLevel == other.LogLevel &&
                Objects.equals(selectionDir, other.selectionDir) &&
                Objects.equals(WindowTitle, other.WindowTitle);
    }

    // generate hash code for current settings
    @Override
    public int hashCode() {
        return Objects.hash(WindowTitle, WindowResizeable, WindowWidth, WindowHeight, LogLevel, selectionDir);
    }

}
