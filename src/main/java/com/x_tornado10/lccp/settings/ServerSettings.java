package com.x_tornado10.lccp.settings;

import com.x_tornado10.lccp.LCCP;
import com.x_tornado10.lccp.communication.network.Networking;
import com.x_tornado10.lccp.Paths;
import com.x_tornado10.lccp.logging.Messages;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConversionException;
import org.apache.commons.configuration2.io.FileHandler;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Objects;

@Setter
@Getter
public class ServerSettings extends Settings{

    private String name = "Server-Config";
    private Type type = Type.SERVER;
    private int Port = 12345;
    private String IPv4 = "localhost";
    private float LED_Brightness = 0.20F;

    private ServerSettings backup;

    // get the default configuration values from internal resource folder and save them to config.yaml
    @Override
    public void saveDefaultConfig() throws IOException, NullPointerException {
        LCCP.logger.debug("Loading default server config values...");
        LCCP.logger.debug("Note: this only happens if server_config.yaml does not exist or couldn't be found!");
        LCCP.logger.debug("If your settings don't work and this message is shown");
        LCCP.logger.debug(Messages.WARN.OPEN_GITHUB_ISSUE);
        // get the internal resource folder and default config values
        URL url = getClass().getClassLoader().getResource("server_config.yaml");
        // if the path is null or not found an exception is thrown
        if (url == null) throw new NullPointerException();
        // try to open a new input stream to read the default values
        try(InputStream inputStream = url.openStream()) {
            // defining config.yaml file to save the values to
            File outputFile = new File(Paths.File_System.server_config);
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
        LCCP.logger.debug("Successfully loaded default server config values!");
    }

    // copy settings from another settings class
    @Override
    public void copy(Settings settings1) {
        // check if other settings class type is compatible
        if (settings1.getType() != type) {
            // send error message if config type is not compatible
            if (settings1.getType() != Type.UNDEFINED) {
                LCCP.logger.error("Can't copy settings from " + settings1.getName() + " Type: " + settings1.getType() + " to " + getName() + " Type: " + type);
                return;
            }
            // send info message if other config class type is undefined
            LCCP.logger.debug("Can't confirm settings type! Type = UNDEFINED");
        }
        // casting other settings class to compatible type
        ServerSettings settings = (ServerSettings) settings1;
        // copy settings
        LCCP.logger.debug("Loading settings from " + settings.getName() + "...");
        this.Port = settings.getPort();
        this.IPv4 = settings.getIPv4();
        this.LED_Brightness = settings.getLED_Brightness();
        LCCP.logger.debug("Successfully loaded settings from " + settings.getName() + "!");
        LCCP.logger.debug(getName() + " now inherits all values from " + settings.getName());
    }

    // loading settings from config file
    @Override
    public void load(YAMLConfiguration config) {
        // loading settings
        try {
            this.IPv4 = config.getString(Paths.Server_Config.IPV4);
            int tempPort = config.getInt(Paths.Server_Config.PORT);
            // checking if provided port is in the valid port range
            if (!Networking.General.isValidPORT(String.valueOf(tempPort))) {
                LCCP.logger.error("Error while parsing Server-Port! Invalid Port!");
                LCCP.logger.warn("Port is outside the valid range of 0-65535!");
                LCCP.logger.warn("There was an error while reading the config file, some settings may be broken!");
            } else {
                this.Port = tempPort;
            }
        } catch (ConversionException | NullPointerException e) {
            LCCP.logger.error("Error while parsing Server-IP and Server-Port! Not a valid number!");
            LCCP.logger.warn("Invalid port and / or IPv4 address! Please restart the application!");
            LCCP.logger.warn("There was an error while reading the config file, some settings may be broken!");
        }
        this.LED_Brightness = (float) config.getInt(Paths.Server_Config.BRIGHTNESS) / 100;
    }

    // save current settings to config file
    @Override
    public void save() {
        // check for changes to avoid unnecessary save
        if (this.equals(backup)) {
            LCCP.logger.debug("Didn't save " + name + " because nothing changed!");
            return;
        }
        LCCP.logger.debug("Saving " + name + " values to server-config.yaml...");
        // loading config file
        YAMLConfiguration conf;
        FileHandler fH;
        HashMap<Integer, String> comments;
        try {
            conf = new YAMLConfiguration();
            fH = new FileHandler(conf);
            fH.load(Paths.File_System.server_config);
            comments = new HashMap<>(CommentPreservation.extractComments(Paths.File_System.server_config));
        } catch (ConfigurationException e) {
            LCCP.logger.error("Error occurred while writing server-config values to server-config.yaml!");
            LCCP.logger.warn("Please restart the application to prevent further errors!");
            return;
        }

        // writing config settings to file
        try {
            conf.setProperty(Paths.Server_Config.BRIGHTNESS, Math.round(LED_Brightness * 100));
            conf.setProperty(Paths.Server_Config.IPV4, IPv4);
            conf.setProperty(Paths.Server_Config.PORT, Port);
            // saving settings
            fH.save(Paths.File_System.server_config);
            CommentPreservation.insertComments(Paths.File_System.server_config, comments);
        } catch (ConfigurationException e)  {
            LCCP.logger.error("Something went wrong while saving the config values for server-config.yaml!");
            LCCP.logger.warn("Please restart the application to prevent further errors!");
            LCCP.logger.warn("Previously made changes to the server-config may be lost!");
            LCCP.logger.warn("If this message appears on every attempt to save config changes please open an issue on GitHub!");
            return;
        } catch (IOException e) {
            LCCP.logger.error("Something went wrong while saving the config comments for server-config.yaml!");
            LCCP.logger.warn("Please restart the application to prevent further errors!");
            LCCP.logger.warn("Previously made changes to the server-config may be lost!");
            LCCP.logger.warn("If this message appears on every attempt to save config changes please open an issue on GitHub!");
            return;
        }

        LCCP.logger.debug("Successfully saved server-config values to server-config.yaml!");
    }


    // creating clone for unnecessary saving check
    @Override
    public void startup() {
        this.backup = new ServerSettings().cloneS();
    }

    // creating a clone of this config class
    @Override
    public ServerSettings cloneS() {
        ServerSettings settings1 = new ServerSettings();
        settings1.copy(LCCP.server_settings);
        return settings1;
    }

    public void setLED_Brightness(float LED_Brightness) {
        this.LED_Brightness = LED_Brightness / 100;
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
        ServerSettings other = (ServerSettings) obj;
        return LED_Brightness == other.LED_Brightness &&
                Objects.equals(IPv4, other.IPv4) &&
                Objects.equals(Port, other.Port);
    }

    // generate hash code for current settings
    @Override
    public int hashCode() {
        return Objects.hash(LED_Brightness, IPv4, Port);
    }
}
