package com.toxicstoxm.LEDSuite.settings;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.LEDSuite;
import com.toxicstoxm.LEDSuite.communication.network.Networking;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConversionException;
import org.apache.commons.configuration2.io.FileHandler;

import java.io.*;
import java.net.URL;
import java.util.Objects;
import java.util.TreeMap;

@Setter
@Getter
public class ServerSettings extends Settings{

    private String name = "Server-Config";
    private Type type = Type.SERVER;
    private int Port = 12345;
    private String IPv4 = "localhost";
    private float LED_Brightness = 0.20F;

    private ServerSettings backup;

    // get the default configuration values from the internal resource folder and save them to config.yaml
    @Override
    public void saveDefaultConfig() throws IOException, NullPointerException {
        LEDSuite.logger.debug("Loading default server config values...");
        LEDSuite.logger.debug("Note: this only happens if server_config.yaml does not exist or couldn't be found!");
        LEDSuite.logger.debug("If your settings don't work and this message is shown");
        LEDSuite.logger.debug(Constants.Messages.WARN.OPEN_GITHUB_ISSUE);
        // get the internal resource folder and default config values
        URL url = getClass().getClassLoader().getResource("server_config.yaml");
        // if the path is null or not found, an exception is thrown
        if (url == null) throw new NullPointerException();
        // try to open a new input stream to read the default values
        try(InputStream inputStream = url.openStream()) {
            // defining config.yaml file to save the values to
            File outputFile = new File(Constants.File_System.server_config);
            // try to open a new output stream to save the values to the new config file
            try (OutputStream outputStream = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[1024];
                // if the buffer isn't empty, the write function writes the read bytes using the stored length in bytesRead var below
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        }
        LEDSuite.logger.debug("Successfully loaded default server config values!");
    }

    // copy settings from another settings class
    @Override
    public void copy(Settings settings1) {
        // check if another settings class type is compatible
        if (settings1.getType() != type) {
            // send an error message if a config type is not compatible
            if (settings1.getType() != Type.UNDEFINED) {
                LEDSuite.logger.error("Can't copy settings from " + settings1.getName() + " Type: " + settings1.getType() + " to " + getName() + " Type: " + type);
                return;
            }
            // send an info message if another config class type is undefined
            LEDSuite.logger.debug("Can't confirm settings type! Type = UNDEFINED");
        }
        // casting other settings class to a compatible type
        ServerSettings settings = (ServerSettings) settings1;
        // copy settings
        LEDSuite.logger.debug("Loading settings from " + settings.getName() + "...");
        this.Port = settings.getPort();
        this.IPv4 = settings.getIPv4();
        this.LED_Brightness = settings.getLED_Brightness();
        LEDSuite.logger.debug("Successfully loaded settings from " + settings.getName() + "!");
        LEDSuite.logger.debug(getName() + " now inherits all values from " + settings.getName());
    }

    // loading settings from config file
    @Override
    public void load(YAMLConfiguration config) {
        // loading settings
        try {
            this.IPv4 = config.getString(Constants.Server_Config.IPV4);
            int tempPort = config.getInt(Constants.Server_Config.PORT);
            // checking if the provided port is in the valid port range
            if (!Networking.Validation.isValidPORT(String.valueOf(tempPort))) {
                LEDSuite.logger.error("Error while parsing Server-Port! Invalid Port!");
                LEDSuite.logger.warn("Port is outside the valid range of 0-65535!");
                LEDSuite.logger.warn("There was an error while reading the config file, some settings may be broken!");
            } else {
                this.Port = tempPort;
            }
        } catch (ConversionException | NullPointerException e) {
            LEDSuite.logger.error("Error while parsing Server-IP and Server-Port! Not a valid number!");
            LEDSuite.logger.warn("Invalid port and / or IPv4 address! Please restart the application!");
            LEDSuite.logger.warn("There was an error while reading the config file, some settings may be broken!");
        }
        this.LED_Brightness = (float) config.getInt(Constants.Server_Config.BRIGHTNESS) / 100;
    }

    // save current settings to config file
    @Override
    public void save() {
        // check for changes to avoid unnecessary save
        if (this.equals(backup)) {
            LEDSuite.logger.debug("Didn't save " + name + " because nothing changed!");
            return;
        }
        LEDSuite.logger.debug("Saving " + name + " values to server-config.yaml...");
        // loading config file
        YAMLConfiguration conf;
        FileHandler fH;
        TreeMap<Integer, String> comments;
        try {
            conf = new YAMLConfiguration();
            fH = new FileHandler(conf);
            fH.load(Constants.File_System.server_config);
            comments = new TreeMap<>(CommentPreservation.extractComments(Constants.File_System.server_config));
        } catch (ConfigurationException e) {
            LEDSuite.logger.error("Error occurred while writing server-config values to server-config.yaml!");
            LEDSuite.logger.warn("Please restart the application to prevent further errors!");
            return;
        }

        // writing config settings to file
        try {
            conf.setProperty(Constants.Server_Config.BRIGHTNESS, Math.round(LED_Brightness * 100));
            conf.setProperty(Constants.Server_Config.IPV4, IPv4);
            conf.setProperty(Constants.Server_Config.PORT, Port);
            // saving settings
            fH.save(Constants.File_System.server_config);
            CommentPreservation.insertComments(Constants.File_System.server_config, comments);
        } catch (ConfigurationException e)  {
            LEDSuite.logger.error("Something went wrong while saving the config values for server-config.yaml!");
            LEDSuite.logger.warn("Please restart the application to prevent further errors!");
            LEDSuite.logger.warn("Previously made changes to the server-config may be lost!");
            LEDSuite.logger.warn("If this message appears on every attempt to save config changes please open an issue on GitHub!");
            return;
        } catch (IOException e) {
            LEDSuite.logger.error("Something went wrong while saving the config comments for server-config.yaml!");
            LEDSuite.logger.warn("Please restart the application to prevent further errors!");
            LEDSuite.logger.warn("Previously made changes to the server-config may be lost!");
            LEDSuite.logger.warn("If this message appears on every attempt to save config changes please open an issue on GitHub!");
            return;
        }

        LEDSuite.logger.debug("Successfully saved server-config values to server-config.yaml!");
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
        settings1.copy(LEDSuite.server_settings);
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
