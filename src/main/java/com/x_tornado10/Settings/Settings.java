package com.x_tornado10.Settings;

import com.x_tornado10.Main;
import com.x_tornado10.util.Paths;
import com.x_tornado10.util.Networking;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.ex.ConversionException;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.Objects;
import java.util.Properties;

// settings class to store config settings on runtime
@Setter
@Getter
public class Settings {
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
    private int Port = 12345;
    private String IPv4 = "127.0.0.1";

    // get the default configuration values from internal resource folder and save them to config.yaml
    public void saveDefaultConfig() throws IOException, NullPointerException {
        Main.logger.info("Loading default config values...");
        Main.logger.info("Note: this only happens if config.yaml does not exist or couldn't be found!");
        Main.logger.info("If this message is shown on each startup please seek support on the projects GitHub page: " + Paths.Links.Project_GitHub);
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
        Main.logger.info("Successfully loaded default config values!");
    }
    // load the config settings from config.yaml and store them in an instance of this class
    public void load(FileBasedConfiguration config) {
        Main.logger.info("Loading config values to memory...");
        String version;
        // getting the current application version using a version.properties file
        // the .properties file contains a maven variable that gets replaced once the application is compiled
        try (InputStream inputStream = Main.class.getResourceAsStream("/version.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);
            version = properties.getProperty("app.version");
        } catch (IOException ignored) {
            // if the version can't be loaded an error is displayed in the console
            // the program is also halted to prevent any further issues
            // if this fails its likely that this build is faulty
            Main.logger.error("Wasn't able to get app version!");
            Main.logger.warn("Application was halted!");
            Main.logger.warn("If this keeps happening please open an issue on GitHub!");
            Main.logger.warn("Please restart the application!");
            Main.error();
            return;
        }
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
            this.WindowInitialScreen = config.getInt(Paths.Config.WINDOW_INITIAL_SCREEN);
        } catch (ConversionException e) {
            Main.logger.error("Error while parsing Window-Initial-Screen! Not a valid number!");
            Main.logger.warn("There was an error while reading the config file, some settings may be broken!");
        }

        try {
            String tempIPv4 = config.getString(Paths.Config.IPv4);
            if (!Networking.isValidIP(tempIPv4)) {
                Main.logger.error("Error while parsing Server-IP! Invalid IPv4 address!");
                Main.logger.warn("Invalid IPv4! Please restart the application!");
                Main.logger.warn("IPv4 address does not match the following format: 0.0.0.0 - 255.255.255.255");
                Main.logger.warn("There was an error while reading the config file, some settings may be broken!");
            } else {
                this.IPv4 = tempIPv4;
            }
            int tempPort = config.getInt(Paths.Config.Port);
            if (!Networking.isValidPORT(String.valueOf(tempPort))) {
                Main.logger.error("Error while parsing Server-Port! Invalid Port!");
                Main.logger.warn("Port is outside the valid range of 0-65535!");
                Main.logger.warn("There was an error while reading the config file, some settings may be broken!");
            } else {
                this.Port = tempPort;
            }
        } catch (ConversionException | NullPointerException e) {
            Main.logger.error("Error while parsing Server-IP and Server-Port! Not a valid number!");
            Main.logger.warn("Invalid port and / or IPv4 address! Please restart the application!");
            Main.logger.warn("There was an error while reading the config file, some settings may be broken!");
        }
        Main.logger.info("Loaded config values to memory!");
    }
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

    // copy the settings of another settings class
    public void copy(Settings settings) {
        Main.logger.info("Loading settings from " + settings.getName() + "...");
        this.DarkM = settings.DarkM;
        this.DarkMColorPrim = settings.DarkMColorPrim;
        this.DarkMColorSec = settings.DarkMColorSec;
        this.LightMColorPrim = settings.LightMColorPrim;
        this.LightMColorSec = settings.LightMColorSec;
        this.WindowTitle = settings.WindowTitle;
        this.WindowResizeable = settings.WindowResizeable;
        this.WindowWidth = settings.WindowWidth;
        this.WindowHeight = settings.WindowHeight;
        this.WindowCenter = settings.WindowCenter;
        this.WindowX = settings.WindowX;
        this.WindowY = settings.WindowY;
        this.FakeLoadingBar = settings.FakeLoadingBar;
        this.WindowFullScreen = settings.WindowFullScreen;
        this.WindowedFullScreen = settings.WindowedFullScreen;
        this.WindowInitialScreen = settings.WindowInitialScreen;
        this.IPv4 = settings.IPv4;
        this.Port = settings.Port;
        Main.logger.info("Successfully loaded settings from " + settings.getName() + "!");
        Main.logger.info(getName() + " now inherits all values from " + settings.getName());
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
        Settings other = (Settings) obj;
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
                Objects.equals(DarkMColorPrim, other.DarkMColorPrim) &&
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
                FakeLoadingBar, WindowFullScreen, WindowedFullScreen, WindowInitialScreen);
    }

}
