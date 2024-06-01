package com.x_tornado10.lccp.settings;

import com.x_tornado10.lccp.LCCP;
import com.x_tornado10.lccp.util.Paths;
import com.x_tornado10.lccp.util.logging.Messages;
import lombok.Getter;
import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConversionException;
import org.apache.commons.configuration2.io.FileHandler;

import java.io.*;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.Objects;


// settings class to store config settings on runtime
@Getter
public class LocalSettings extends Settings {
    // default settings
    private final Type type = Type.LOCAL;
    private final String name = "Main-Config";
    private String WindowTitle = "LED-Cube-Control-Panel";
    private boolean WindowResizeable = true;
    private int WindowDefWidth = 1280;
    private int WindowDefHeight = 720;
    private int LogLevel = 4;
    private String selectionDir = System.getProperty("user.home");
    private boolean AutoUpdateRemote = false;
    private boolean DisplayStatusBar = false;
    private double AutoUpdateRemoteTick = 1.5;
    private boolean CheckIPv4 = true;

    private LocalSettings backup;

    // get the default configuration values from internal resource folder and save them to config.yaml
    @Override
    public void saveDefaultConfig() throws IOException, NullPointerException {
        LCCP.logger.debug("Loading default config values...");
        LCCP.logger.debug("Note: this only happens if config.yaml does not exist or couldn't be found!");
        LCCP.logger.debug("If your settings don't work and this message is shown");
        LCCP.logger.debug(Messages.WARN.OPEN_GITHUB_ISSUE);
        // get the internal resource folder and default config values
        URL url = getClass().getClassLoader().getResource("config.yaml");
        // if the path is null or not found an exception is thrown
        if (url == null) throw new NullPointerException();
        // try to open a new input stream to read the default values
        try(InputStream inputStream = url.openStream()) {
            // defining config.yaml file to save the values to
            File outputFile = new File(Paths.File_System.config);
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
    public void load(YAMLConfiguration config) {
        LCCP.logger.debug("Loading config values to memory...");
        try {
            this.WindowTitle = config.getString(Paths.Config.WINDOW_TITLE);
            // setting values to parsed config values
            this.WindowResizeable = config.getBoolean(Paths.Config.WINDOW_RESIZABLE);

            // handle potential ConversionExceptions gracefully
            try {
                this.WindowDefHeight = config.getInt(Paths.Config.WINDOW_DEFAULT_HEIGHT);
                this.WindowDefWidth = config.getInt(Paths.Config.WINDOW_DEFAULT_WIDTH);
            } catch (ConversionException e) {
                LCCP.logger.error("Error while parsing Window-Default-Height and Window-Default-Width! Not a valid Number!");
                LCCP.logger.warn("There was an error while reading the config file, some settings may be broken!");
            }

            try {
                this.LogLevel = config.getInt(Paths.Config.LOG_LEVEL);
            } catch (ConversionException e) {
                LCCP.logger.error("Error while parsing Log-Level! Not a valid Number!");
                LCCP.logger.warn("There was an error while reading the config file, some settings may be broken!");
            }

            try {
                this.AutoUpdateRemoteTick = config.getDouble(Paths.Config.AUTO_UPDATE_REMOTE_TICK);
            } catch (ConversionException e) {
                LCCP.logger.error("Error while parsing Auto-Update-Remote-Tick! Not a valid Number!");
                LCCP.logger.warn("There was an error while reading the config file, some settings may be broken!");
            }

            this.selectionDir = config.getString(Paths.Config.SELECTION_DIR);

            AutoUpdateRemote = config.getBoolean(Paths.Config.AUTO_UPDATE_REMOTE);
            DisplayStatusBar = config.getBoolean(Paths.Config.DISPLAY_STATUS_BAR);
            CheckIPv4 = config.getBoolean(Paths.Config.CHECK_IPV4);

            LCCP.logger.debug("Loaded config values to memory!");
        } catch (NoSuchElementException e){
            LCCP.logger.error("Error while parsing config! Settings / values missing! Your probably using an old config file!");
            LCCP.logger.warn("Program halted to prevent any further errors!");
            LCCP.logger.warn("Please delete the old config file from your .config folder and restart the application!");
            LCCP.exit(1);
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
        LocalSettings settings = (LocalSettings) settings1;
        LCCP.logger.debug("Loading settings from " + settings.getName() + "...");
        // copying settings
        this.WindowTitle = settings.getWindowTitleRaw();
        this.WindowResizeable = settings.isWindowResizeable();
        this.WindowDefWidth = settings.getWindowDefWidth();
        this.WindowDefHeight = settings.getWindowDefHeight();
        this.LogLevel = settings.getLogLevel();
        this.selectionDir = settings.getSelectionDir();
        this.AutoUpdateRemote = settings.AutoUpdateRemote;
        this.DisplayStatusBar = settings.DisplayStatusBar;
        this.AutoUpdateRemoteTick = settings.AutoUpdateRemoteTick;
        this.CheckIPv4 = settings.CheckIPv4;
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
        YAMLConfiguration conf;
        FileHandler fH;
        try {
            conf = new YAMLConfiguration();
            fH = new FileHandler(conf);
            fH.load(Paths.File_System.config);
        } catch (ConfigurationException e) {
            LCCP.logger.error("Error occurred while writing config values to config.yaml!");
            LCCP.logger.warn("Please restart the application to prevent further errors!");
            return;
        }

        // writing config settings to file
        try {
            conf.setProperty(Paths.Config.WINDOW_TITLE, WindowTitle);
            conf.setProperty(Paths.Config.WINDOW_RESIZABLE, WindowResizeable);
            conf.setProperty(Paths.Config.WINDOW_DEFAULT_WIDTH, WindowDefWidth);
            conf.setProperty(Paths.Config.WINDOW_DEFAULT_HEIGHT, WindowDefHeight);
            conf.setProperty(Paths.Config.LOG_LEVEL, LogLevel);
            conf.setProperty(Paths.Config.SELECTION_DIR, selectionDir);
            conf.setProperty(Paths.Config.AUTO_UPDATE_REMOTE, AutoUpdateRemote);
            conf.setProperty(Paths.Config.DISPLAY_STATUS_BAR, DisplayStatusBar);
            conf.setProperty(Paths.Config.AUTO_UPDATE_REMOTE_TICK, AutoUpdateRemoteTick);
            conf.setProperty(Paths.Config.CHECK_IPV4, CheckIPv4);
            // saving settings
            fH.save(Paths.File_System.config);
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
        this.backup = new LocalSettings().cloneS();
    }

    // creating a clone of this config class
    @Override
    public LocalSettings cloneS() {
        LocalSettings settings1 = new LocalSettings();
        settings1.copy(LCCP.settings);
        return settings1;
    }

    public void setWindowTitle(String windowTitle) {
        WindowTitle = windowTitle;
        reload("WindowTitle -> " + windowTitle);
    }

    public void setWindowResizeable(boolean windowResizeable) {
        WindowResizeable = windowResizeable;
        reload("WindowResizeable -> " + windowResizeable);
    }

    public void setLogLevel(int logLevel) {
        LogLevel = logLevel;
        reload("LogLevel -> " + logLevel);
    }

    public void setSelectionDir(String selectionDir) {
        this.selectionDir = selectionDir;
        reload("selectionDir -> " + selectionDir);
    }

    public void setAutoUpdateRemote(boolean autoUpdateRemote) {
        AutoUpdateRemote = autoUpdateRemote;
        reload("AutoUpdateRemote -> " + autoUpdateRemote);
    }

    public void setDisplayStatusBar(boolean displayStatusBar) {
        DisplayStatusBar = displayStatusBar;
        reload("DisplayStatusBar -> " + displayStatusBar);
    }

    public void setAutoUpdateRemoteTick(double autoUpdateRemoteTick) {
        AutoUpdateRemoteTick = autoUpdateRemoteTick;
        reload("AutoUpdateRemoteTick -> " + autoUpdateRemoteTick);
    }

    public String getSelectionDir() {
        if (selectionDir.contains("%")) {
            String temp = selectionDir.replaceAll("%",  "");
            try {

                return System.getProperty(temp);
            } catch (Exception e) {
                LCCP.logger.debug("System property placeholder: '" + selectionDir + "'");
                LCCP.logger.debug("System property parsed: '" + temp + "'");
                LCCP.logger.warn("Invalid system property: '" + temp + "'");
                return System.getProperty("user.home");
            }
        }
        return selectionDir;
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
        LocalSettings other = (LocalSettings) obj;
        return WindowResizeable == other.WindowResizeable &&
                WindowDefWidth == other.WindowDefWidth &&
                WindowDefHeight == other.WindowDefHeight &&
                LogLevel == other.LogLevel &&
                AutoUpdateRemote == other.AutoUpdateRemote &&
                DisplayStatusBar == other.DisplayStatusBar &&
                CheckIPv4 == other.CheckIPv4 &&
                Objects.equals(selectionDir, other.selectionDir) &&
                Objects.equals(WindowTitle, other.WindowTitle) &&
                Objects.equals(AutoUpdateRemoteTick, other.AutoUpdateRemoteTick);
    }

    // generate hash code for current settings
    @Override
    public int hashCode() {
        return Objects.hash(WindowTitle, WindowResizeable, WindowDefHeight, WindowDefWidth, LogLevel, selectionDir, AutoUpdateRemote, DisplayStatusBar, AutoUpdateRemoteTick, CheckIPv4);
    }

}
