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

/**
 * The `ServerSettings` class extends `Settings` to provide specific configurations for server settings.
 * It handles loading, saving, and managing server-specific settings.
 *
 * @since 1.0.0
 */
@Setter
@Getter
public class ServerSettings extends Settings {

    // Default name for server configuration
    private String name = "Server-Config";
    // Type of settings (specifically SERVER for this class)
    private Type type = Type.SERVER;
    // Server port
    private int Port = 12345;
    // Server IPv4 address
    private String IPv4 = "localhost";
    // LED brightness level
    private float LED_Brightness = 0.20F;
    // Backup of current settings for change detection
    private ServerSettings backup;

    /**
     * Loads the default configuration values from the internal resource folder
     * and saves them to the `config.yaml` file.
     *
     * @throws IOException if an I/O error occurs.
     * @throws NullPointerException if the default configuration resource cannot be found.
     * @since 1.0.0
     */
    @Override
    public void saveDefaultConfig() throws IOException, NullPointerException {
        LEDSuite.logger.debug("Loading default server config values...");
        LEDSuite.logger.debug("Note: this only happens if server_config.yaml does not exist or couldn't be found!");
        LEDSuite.logger.debug("If your settings don't work and this message is shown");
        LEDSuite.logger.debug(Constants.Messages.WARN.OPEN_GITHUB_ISSUE);

        // Get the URL for the default configuration file
        URL url = getClass().getClassLoader().getResource("server_config.yaml");
        // If URL is null, throw an exception
        if (url == null) throw new NullPointerException();

        // Open input stream to read the default configuration file
        try (InputStream inputStream = url.openStream()) {
            // Define the output file to save the configuration
            File outputFile = new File(Constants.File_System.server_config);
            // Open output stream to write the configuration to the file
            try (OutputStream outputStream = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                // Read from input stream and write to output stream
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        }
        LEDSuite.logger.debug("Successfully loaded default server config values!");
    }

    /**
     * Copies settings from another `Settings` instance.
     * Checks compatibility and copies relevant values.
     *
     * @param settings1 The `Settings` instance to copy from.
     * @since 1.0.0
     */
    @Override
    public void copy(Settings settings1) {
        // Check if the type of the settings is compatible
        if (settings1.getType() != type) {
            if (settings1.getType() != Type.UNDEFINED) {
                LEDSuite.logger.error("Can't copy settings from " + settings1.getName() + " Type: " + settings1.getType() + " to " + getName() + " Type: " + type);
                return;
            }
            LEDSuite.logger.debug("Can't confirm settings type! Type = UNDEFINED");
        }
        // Cast the other settings to `ServerSettings` for copying
        ServerSettings settings = (ServerSettings) settings1;
        LEDSuite.logger.debug("Loading settings from " + settings.getName() + "...");
        // Copy settings values
        this.Port = settings.getPort();
        this.IPv4 = settings.getIPv4();
        this.LED_Brightness = settings.getLED_Brightness();
        LEDSuite.logger.debug("Successfully loaded settings from " + settings.getName() + "!");
        LEDSuite.logger.debug(getName() + " now inherits all values from " + settings.getName());
    }

    /**
     * Loads settings from a `YAMLConfiguration` object.
     * Parses values for IPv4 address, port, and LED brightness.
     *
     * @param config The `YAMLConfiguration` object containing the configuration settings.
     * @since 1.0.0
     */
    @Override
    public void load(YAMLConfiguration config) {
        super.load(config);
        try {
            // Load IPv4 address
            this.IPv4 = config.getString(Constants.Server_Config.IPV4);
            int tempPort = config.getInt(Constants.Server_Config.PORT);
            // Validate the port number
            if (!Networking.Validation.isValidPORT(String.valueOf(tempPort))) {
                LEDSuite.logger.warn("Error while parsing Server-Port! Invalid Port!");
                LEDSuite.logger.warn("Port is outside the valid range of 0-65535!");
            } else {
                this.Port = tempPort;
            }
        } catch (ConversionException | NullPointerException e) {
            LEDSuite.logger.warn("Error while parsing Server-IP and Server-Port! Not a valid number!");
            LEDSuite.logger.warn("Invalid port and / or IPv4 address! Please restart the application!");
        }
        // Load and convert LED brightness
        this.LED_Brightness = (float) config.getInt(Constants.Server_Config.BRIGHTNESS) / 100;
    }

    /**
     * Saves the current settings to the configuration file.
     * Checks for changes to avoid unnecessary saves.
     *
     * @since 1.0.0
     */
    @Override
    public void save() {
        // Check if the current settings are the same as the backup
        if (this.equals(backup)) {
            LEDSuite.logger.debug("Didn't save " + name + " because nothing changed!");
            return;
        }
        LEDSuite.logger.debug("Saving " + name + " values to server-config.yaml...");

        // Initialize configuration and file handler
        YAMLConfiguration conf;
        FileHandler fH;
        TreeMap<Integer, String> comments;
        try {
            conf = new YAMLConfiguration();
            fH = new FileHandler(conf);
            fH.load(Constants.File_System.server_config);
            comments = new TreeMap<>(CommentPreservation.extractComments(Constants.File_System.server_config));
        } catch (ConfigurationException e) {
            LEDSuite.logger.warn("Error occurred while writing server-config values to server-config.yaml!" + LEDSuite.logger.getErrorMessage(e));
            return;
        }

        // Write settings to configuration file
        try {
            conf.setProperty(Constants.Server_Config.BRIGHTNESS, Math.round(LED_Brightness * 100));
            conf.setProperty(Constants.Server_Config.IPV4, IPv4);
            conf.setProperty(Constants.Server_Config.PORT, Port);
            fH.save(Constants.File_System.server_config);
            CommentPreservation.insertComments(Constants.File_System.server_config, comments);
        } catch (ConfigurationException e) {
            LEDSuite.logger.warn("Something went wrong while saving the config values for server-config.yaml!" + LEDSuite.logger.getErrorMessage(e));
            LEDSuite.logger.warn("Previously made changes to the server-config may be lost!");
            return;
        } catch (IOException e) {
            LEDSuite.logger.warn("Something went wrong while saving the config comments for server-config.yaml!" + LEDSuite.logger.getErrorMessage(e));
            LEDSuite.logger.warn("Previously made changes to the server-config may be lost!");
            return;
        }

        LEDSuite.logger.debug("Successfully saved server-config values to server-config.yaml!");
    }

    /**
     * Initializes a backup of the current settings.
     * Used to detect changes before saving.
     *
     * @since 1.0.0
     */
    @Override
    public void startup() {
        this.backup = new ServerSettings().cloneS();
    }

    /**
     * Creates a clone of the current `ServerSettings` instance.
     *
     * @return A new `ServerSettings` instance with copied values.
     * @since 1.0.0
     */
    @Override
    public ServerSettings cloneS() {
        ServerSettings settings1 = new ServerSettings();
        settings1.copy(LEDSuite.server_settings);
        return settings1;
    }

    /**
     * Sets the LED brightness value.
     * Converts the brightness from a percentage (0-100) to a fractional value (0.0 - 1.0).
     *
     * @param LED_Brightness The brightness value as a percentage.
     * @since 1.0.0
     */
    public void setLED_Brightness(float LED_Brightness) {
        this.LED_Brightness = LED_Brightness / 100;
    }

    /**
     * Checks if the current settings are equal to another `ServerSettings` instance.
     *
     * @param obj The object to compare with.
     * @return `true` if the settings are equal, otherwise `false`.
     * @since 1.0.0
     */
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

    /**
     * Generates a hash code for the current settings.
     *
     * @return The hash code value.
     * @since 1.0.0
     */
    @Override
    public int hashCode() {
        return Objects.hash(LED_Brightness, IPv4, Port);
    }
}