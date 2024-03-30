package com.x_tornado10.Settings;

import com.x_tornado10.Main;
import com.x_tornado10.util.Paths;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.ex.ConversionException;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.Properties;

// settings class to store config settings on runtime
@Setter
@Getter
public class Settings {
    private boolean DarkM;
    private String DarkMColorPrim;
    private String DarkMColorSec;
    private String LightMColorPrim;
    private String LightMColorSec;
    private String WindowTitle = "LED-Cube-Control-Panel";
    private boolean WindowResizeable;
    private int WindowWidth;
    private int WindowHeight;
    private boolean WindowCenter;
    private int WindowX;
    private int WindowY;
    private boolean FakeLoadingBar;
    private boolean WindowFullScreen;
    private boolean WindowedFullScreen;
    private int WindowInitialScreen;

    // get the default configuration values from internal resource folder and save them to config.yaml
    public void saveDefaultConfig() throws IOException, NullPointerException {
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
    }
    // load the config settings from config.yaml and store them in an instance of this class
    public void load(FileBasedConfiguration config) {
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

        // strings
        this.DarkMColorPrim = config.getString(Paths.Config.DARK_MODE_COLOR_PRIMARY);
        this.DarkMColorSec = config.getString(Paths.Config.DARK_MODE_COLOR_SECONDARY);
        this.LightMColorPrim = config.getString(Paths.Config.LIGHT_MODE_COLOR_PRIMARY);
        this.LightMColorSec = config.getString(Paths.Config.LIGHT_MODE_COLOR_SECONDARY);

        // booleans
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
            this.WindowHeight = 0;
            this.WindowWidth = 0;
        }

        try {
            this.WindowX = config.getInt(Paths.Config.WINDOW_SPAWN_X);
            this.WindowY = config.getInt(Paths.Config.WINDOW_SPAWN_Y);
        } catch (ConversionException e) {
            Main.logger.error("Error while parsing Window-Spawn-X and Window-Spawn-Y! Not a valid Number!");
            Main.logger.warn("There was an error while reading the config file, some settings may be broken!");
            this.WindowX = 0;
            this.WindowY = 0;
        }


        try {
            this.WindowInitialScreen = config.getInt(Paths.Config.WINDOW_INITIAL_SCREEN);
        } catch (ConversionException e) {
            Main.logger.error("Error while parsing Window-Initial-Screen! Not a valid Number!");
            Main.logger.warn("There was an error while reading the config file, some settings may be broken!");
            this.WindowInitialScreen = 0;
        }
    }
    public Color getDarkModePrim() {
        return Color.decode(DarkMColorPrim);
    }
    public Color getDarkModeSec() {
        return Color.decode(DarkMColorSec);
    }
    public Color getLightModePrim() {
        return Color.decode(LightMColorPrim);
    }
    public Color getLightModeSec() {
        return Color.decode(LightMColorSec);
    }
}
