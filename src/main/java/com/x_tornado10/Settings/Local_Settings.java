package com.x_tornado10.Settings;

import com.x_tornado10.Main;
import com.x_tornado10.util.Paths;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConversionException;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Properties;

// settings class to store config settings on runtime
@Setter
@Getter
public class Local_Settings extends Settings {
    // default settings
    private Type type = Type.LOCAL;
    private String name = "Main-Config";
    private boolean DarkM = false;
    private String DarkMColorPrim = "#000000";
    private String DarkMColorSec = "#FFFFFF";
    private String LightMColorPrim = "#FFFFFF";
    private String LightMColorSec = "#000000";
    private String WindowTitle = "LED-Cube-Control-Panel";
    private boolean WindowResizeable = false;
    private int WindowWidth = 1024;
    private int WindowHeight = 600;
    private boolean WindowCenter = true;
    private int WindowX = 0;
    private int WindowY = 0;
    private boolean FakeLoadingBar = false;
    private boolean WindowFullScreen = false;
    private boolean WindowedFullScreen = false;
    private int WindowInitialScreen = 0;
    private boolean MobileFriendly = true;
    private double MobileFriendlyModifier = 200;
    private int LogLevel = 4;
    private String selectionDir = System.getProperty("user.home");

    private Local_Settings backup;

    // get the default configuration values from internal resource folder and save them to config.yaml
    @Override
    public void saveDefaultConfig() throws IOException, NullPointerException {
        Main.logger.debug("Loading default config values...");
        Main.logger.debug("Note: this only happens if config.yaml does not exist or couldn't be found!");
        Main.logger.debug("If your settings don't work and this message is shown please seek support on the projects GitHub page: " + Paths.Links.Project_GitHub);
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
        Main.logger.debug("Successfully loaded default config values!");
    }
    // load the config settings from config.yaml and store them in an instance of this class
    @Override
    public void load(FileBasedConfiguration config) {
        Main.logger.debug("Loading config values to memory...");
        String version;
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
            return;
        }
        try {
            this.WindowTitle = config.getString(Paths.Config.WINDOW_TITLE).replace("%VERSION%", version);
            // setting values to parsed config values

            this.DarkMColorPrim = config.getString(Paths.Config.DARK_MODE_COLOR_PRIMARY);
            this.DarkMColorSec = config.getString(Paths.Config.DARK_MODE_COLOR_SECONDARY);
            this.LightMColorPrim = config.getString(Paths.Config.LIGHT_MODE_COLOR_PRIMARY);
            this.LightMColorSec = config.getString(Paths.Config.LIGHT_MODE_COLOR_SECONDARY);

            this.DarkM = config.getBoolean(Paths.Config.DARK_MODE_ENABLED);
            this.WindowCenter = config.getBoolean(Paths.Config.WINDOW_SPAWN_CENTER);
            this.WindowFullScreen = config.getBoolean(Paths.Config.WINDOW_FULL_SCREEN);
            this.WindowResizeable = config.getBoolean(Paths.Config.WINDOW_RESIZABLE);
            this.FakeLoadingBar = config.getBoolean(Paths.Config.STARTUP_FAKE_LOADING_BAR);
            this.WindowedFullScreen = config.getBoolean(Paths.Config.WINDOWED_FULL_SCREEN);
            this.MobileFriendly = config.getBoolean(Paths.Config.MOBILE_FRIENDLY);

            // handle potential ConversionExceptions gracefully
            try {
                this.WindowWidth = config.getInt(Paths.Config.WINDOW_INITIAL_WIDTH);
                this.WindowHeight = config.getInt(Paths.Config.WINDOW_INITIAL_HEIGHT);
            } catch (ConversionException e) {
                Main.logger.error("Error while parsing Window-Initial-Height and Window-Initial-Width! Not a valid Number!");
                Main.logger.warn("There was an error while reading the config file, some settings may be broken!");
            }

            try {
                this.WindowX = config.getInt(Paths.Config.WINDOW_SPAWN_X);
                this.WindowY = config.getInt(Paths.Config.WINDOW_SPAWN_Y);
            } catch (ConversionException e) {
                Main.logger.error("Error while parsing Window-Spawn-X and Window-Spawn-Y! Not a valid Number!");
                Main.logger.warn("There was an error while reading the config file, some settings may be broken!");
            }

            try {
                this.LogLevel = config.getInt(Paths.Config.LOG_LEVEL);
            } catch (ConversionException e) {
                Main.logger.error("Error while parsing Window-Spawn-X and Window-Spawn-Y! Not a valid Number!");
                Main.logger.warn("There was an error while reading the config file, some settings may be broken!");
            }

            try {
                this.WindowInitialScreen = config.getInt(Paths.Config.WINDOW_INITIAL_SCREEN);
            } catch (ConversionException e) {
                Main.logger.error("Error while parsing Window-Initial-Screen! Not a valid number!");
                Main.logger.warn("There was an error while reading the config file, some settings may be broken!");
            }

            try {
                this.MobileFriendlyModifier = config.getDouble(Paths.Config.MOBILE_FRIENDLY_MODIFIER);
            } catch (ConversionException e) {
                Main.logger.error("Error while parsing Mobile-Friendly-Modifier! Not a valid number!");
                Main.logger.warn("There was an error while reading the config file, some settings may be broken!");
            }

            String dir = config.getString(Paths.Config.SELECTION_DIR);
            if (dir.contains("%")) {
                String temp = dir.replace("%", "");
                System.getProperty(temp);
            } else this.selectionDir = dir;

            Main.logger.debug("Loaded config values to memory!");
        } catch (NoSuchElementException e){
            Main.logger.error("Error while parsing config! Settings / values missing! Your probably using an old config file!");
            Main.logger.warn("Program halted to prevent any further errors!");
            Main.logger.warn("Please delete the old config file from your .config folder and restart the application!");
            Main.logger.error_popup("Error while parsing config! Settings / values missing! Your probably using an old config file!");
            Main.logger.warn_popup("Please delete the old config file from your .config folder and restart the application!");
            Main.exit(0);
        }
    }
    // getting color settings (raw = true returns raw hex codes)
    public Object getDarkModePrim(boolean raw) {
        return raw ? DarkMColorPrim : Color.decode(DarkMColorPrim);
    }
    public Object getDarkModeSec(boolean raw) {
        return raw ? DarkMColorSec : Color.decode(DarkMColorSec);
    }
    public Object getLightModePrim(boolean raw) {
        return raw ? LightMColorPrim : Color.decode(LightMColorPrim);
    }
    public Object getLightModeSec(boolean raw) {
        return raw ? LightMColorSec : Color.decode(LightMColorSec);
    }
    public Double getScaleModifier() {
        return MobileFriendly ? MobileFriendlyModifier/100 : 1;
    }
    // returning calculated scale based on the given scale modifier
    public int scale(int value) {
        return (int) Math.round(value * getScaleModifier());
    }

    // copy the settings of another settings class
    @Override
    public void copy(Settings settings1) {
        // check if other settings class type is compatible
        if (settings1.getType() != type) {
            if (settings1.getType() != Type.UNDEFINED) {
                Main.logger.error("Can't copy settings from " + settings1.getName() + " Type: " + settings1.getType() + " to " + getName() + " Type: " + type);
                return;
            }
            Main.logger.info("Can't confirm settings type! Type = UNDEFINED");
        }
        // casting to compatible settings type after check
        Local_Settings settings = (Local_Settings) settings1;
        Main.logger.debug("Loading settings from " + settings.getName() + "...");
        // copying settings
        this.DarkM = settings.isDarkM();
        this.DarkMColorPrim = settings.getDarkMColorPrim();
        this.DarkMColorSec = settings.getDarkMColorSec();
        this.LightMColorPrim = settings.getLightMColorPrim();
        this.LightMColorSec = settings.getLightMColorSec();
        this.WindowTitle = settings.getWindowTitle();
        this.WindowResizeable = settings.isWindowResizeable();
        this.WindowWidth = settings.getWindowWidth();
        this.WindowHeight = settings.getWindowHeight();
        this.WindowCenter = settings.isWindowCenter();
        this.WindowX = settings.getWindowX();
        this.WindowY = settings.getWindowY();
        this.FakeLoadingBar = settings.isFakeLoadingBar();
        this.WindowFullScreen = settings.isWindowFullScreen();
        this.WindowedFullScreen = settings.isWindowedFullScreen();
        this.WindowInitialScreen = settings.getWindowInitialScreen();
        this.MobileFriendly = settings.isMobileFriendly();
        this.MobileFriendlyModifier = settings.getMobileFriendlyModifier();
        this.LogLevel = settings.getLogLevel();
        this.selectionDir = settings.getSelectionDir();
        Main.logger.debug("Successfully loaded settings from " + settings.getName() + "!");
        Main.logger.debug(getName() + " now inherits all values from " + settings.getName());
    }

    // saving current settings to disk
    @Override
    public void save() {
        // check for changes to avoid unnecessary save
        if (this.equals(backup)) {
            Main.logger.debug("Didn't save " + name + " because nothing changed!");
            return;
        }
        Main.logger.debug("Saving " + name + " values to config.yaml...");
        // loading config file
        File file = new File(Paths.config);
        Configurations configs = new Configurations();
        FileBasedConfiguration config;
        Parameters parameters = new Parameters();
        // loading config file to memory
        try {
            config = configs.properties(file);
        } catch (ConfigurationException e) {
            Main.logger.error("Error occurred while writing config values to config.yaml!");
            Main.logger.warn("Please restart the application to prevent further errors!");
            return;
        }

        // initializing new config builder
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                new FileBasedConfigurationBuilder<FileBasedConfiguration>(config.getClass())
                        .configure(parameters.fileBased()
                                .setFile(file));

        // writing config settings to file
        try {
            builder.getConfiguration().setProperty(Paths.Config.DARK_MODE_ENABLED, DarkM);
            builder.getConfiguration().setProperty(Paths.Config.DARK_MODE_COLOR_PRIMARY, DarkMColorPrim);
            builder.getConfiguration().setProperty(Paths.Config.DARK_MODE_COLOR_SECONDARY, DarkMColorSec);
            builder.getConfiguration().setProperty(Paths.Config.LIGHT_MODE_COLOR_PRIMARY, LightMColorPrim);
            builder.getConfiguration().setProperty(Paths.Config.LIGHT_MODE_COLOR_SECONDARY, LightMColorSec);
            builder.getConfiguration().setProperty(Paths.Config.WINDOW_TITLE, WindowTitle);
            builder.getConfiguration().setProperty(Paths.Config.WINDOW_RESIZABLE, WindowResizeable);
            builder.getConfiguration().setProperty(Paths.Config.WINDOW_INITIAL_WIDTH, WindowWidth);
            builder.getConfiguration().setProperty(Paths.Config.WINDOW_INITIAL_HEIGHT, WindowHeight);
            builder.getConfiguration().setProperty(Paths.Config.WINDOW_SPAWN_CENTER, WindowCenter);
            builder.getConfiguration().setProperty(Paths.Config.WINDOW_SPAWN_X, WindowX);
            builder.getConfiguration().setProperty(Paths.Config.WINDOW_SPAWN_Y, WindowY);
            builder.getConfiguration().setProperty(Paths.Config.STARTUP_FAKE_LOADING_BAR, FakeLoadingBar);
            builder.getConfiguration().setProperty(Paths.Config.WINDOW_FULL_SCREEN, WindowFullScreen);
            builder.getConfiguration().setProperty(Paths.Config.WINDOWED_FULL_SCREEN, WindowedFullScreen);
            builder.getConfiguration().setProperty(Paths.Config.WINDOW_INITIAL_SCREEN, WindowInitialScreen);
            builder.getConfiguration().setProperty(Paths.Config.MOBILE_FRIENDLY, MobileFriendly);
            builder.getConfiguration().setProperty(Paths.Config.MOBILE_FRIENDLY_MODIFIER, MobileFriendlyModifier);
            builder.getConfiguration().setProperty(Paths.Config.LOG_LEVEL, LogLevel);
            builder.getConfiguration().setProperty(Paths.Config.SELECTION_DIR, selectionDir);
            // saving settings
            builder.save();
        } catch (ConfigurationException e)  {
            Main.logger.error("Something went wrong while saving the config values for config.yaml!");
            Main.logger.warn("Please restart the application to prevent further errors!");
            Main.logger.warn("Previously made changes to the config may be lost!");
            Main.logger.warn("If this message appears on every attempt to save config changes please open an issue on GitHub!");
            return;
        }

        Main.logger.debug("Successfully saved server-config values to config.yaml!");
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
        settings1.copy(Main.settings);
        return settings1;
    }

    public boolean requiresRestart(Local_Settings settings) {
        boolean result = settings.isDarkM() != isDarkM();
        if (!settings.getDarkMColorPrim().equals(getDarkMColorPrim())) result = true;
        if (!settings.getDarkMColorSec().equals(getDarkMColorSec())) result = true;
        if (!settings.getLightMColorPrim().equals(getLightMColorPrim())) result = true;
        if (!settings.getLightMColorSec().equals(getLightMColorSec())) result = true;
        return result;
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
        return  DarkM == other.DarkM &&
                WindowResizeable == other.WindowResizeable &&
                WindowCenter == other.WindowCenter &&
                FakeLoadingBar == other.FakeLoadingBar &&
                WindowFullScreen == other.WindowFullScreen &&
                WindowedFullScreen == other.WindowedFullScreen &&
                WindowInitialScreen == other.WindowInitialScreen &&
                WindowWidth == other.WindowWidth &&
                WindowHeight == other.WindowHeight &&
                WindowX == other.WindowX &&
                WindowY == other.WindowY &&
                MobileFriendly == other.MobileFriendly &&
                LogLevel == other.LogLevel &&
                MobileFriendlyModifier == other.MobileFriendlyModifier &&
                Objects.equals(DarkMColorPrim, other.DarkMColorPrim) &&
                Objects.equals(selectionDir, other.selectionDir) &&
                Objects.equals(DarkMColorSec, other.DarkMColorSec) &&
                Objects.equals(LightMColorPrim, other.LightMColorPrim) &&
                Objects.equals(LightMColorSec, other.LightMColorSec) &&
                Objects.equals(WindowTitle, other.WindowTitle);
    }

    // generate hash code for current settings
    @Override
    public int hashCode() {
        return Objects.hash(DarkM, DarkMColorPrim, DarkMColorSec, LightMColorPrim, LightMColorSec, WindowTitle,
                WindowResizeable, WindowWidth, WindowHeight, WindowCenter, WindowX, WindowY,
                FakeLoadingBar, WindowFullScreen, WindowedFullScreen, WindowInitialScreen, MobileFriendly,
                MobileFriendlyModifier, LogLevel, selectionDir);
    }

}
