package com.x_tornado10.Settings;

import com.x_tornado10.Main;
import com.x_tornado10.util.Networking;
import com.x_tornado10.util.Paths;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.ex.ConversionException;

import java.io.*;
import java.net.URL;
import java.util.Objects;

@Setter
@Getter
public class Server_Settings extends Settings {

    private String name = "Server-Config";
    private Type type = Type.SERVER;
    private int Port = 12345;
    private String IPv4 = "127.0.0.1";
    private float LED_Brightness = 0.20F;

    // get the default configuration values from internal resource folder and save them to config.yaml
    public void saveDefaultConfig() throws IOException, NullPointerException {
        Main.logger.info("Loading default server config values...");
        Main.logger.info("Note: this only happens if server_config.yaml does not exist or couldn't be found!");
        Main.logger.info("If your settings don't work and this message is shown please seek support on the projects GitHub page: " + Paths.Links.Project_GitHub);
        // get the internal resource folder and default config values
        URL url = getClass().getClassLoader().getResource("server_config.yaml");
        // if the path is null or not found an exception is thrown
        if (url == null) throw new NullPointerException();
        // try to open a new input stream to read the default values
        try(InputStream inputStream = url.openStream()) {
            // defining config.yaml file to save the values to
            File outputFile = new File(Paths.server_config);
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
        Main.logger.info("Successfully loaded default server config values!");
    }

    @Override
    public void copy(Settings settings1) {
        if (settings1.getType() != type) {
            Main.logger.error("Can't copy settings from " + settings1.getName() + " Type: " + settings1.getType() + " to " + getName() + " Type: " + type);
            return;
        }
        Server_Settings settings = (Server_Settings) settings1;
        Main.logger.info("Loading settings from " + settings.getName() + "...");
        this.Port = settings.Port;
        this.IPv4 = settings.IPv4;
        this.LED_Brightness = settings.LED_Brightness;
        Main.logger.info("Successfully loaded settings from " + settings.getName() + "!");
        Main.logger.info(getName() + " now inherits all values from " + settings.getName());
    }

    @Override
    public void load(FileBasedConfiguration config) {
        try {
            String tempIPv4 = config.getString(Paths.Server_Config.IPV4);
            if (!Networking.isValidIP(tempIPv4)) {
                Main.logger.error("Error while parsing Server-IP! Invalid IPv4 address!");
                Main.logger.warn("Invalid IPv4! Please restart the application!");
                Main.logger.warn("IPv4 address does not match the following format: 0.0.0.0 - 255.255.255.255");
                Main.logger.warn("There was an error while reading the config file, some settings may be broken!");
            } else {
                this.IPv4 = tempIPv4;
            }
            int tempPort = config.getInt(Paths.Server_Config.PORT);
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
        this.LED_Brightness = (float) config.getInt(Paths.Server_Config.BRIGHTNESS) / 100;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Server_Settings other = (Server_Settings) obj;
        return LED_Brightness == other.LED_Brightness &&
                Objects.equals(IPv4, other.IPv4) &&
                Objects.equals(Port, other.Port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(LED_Brightness, IPv4, Port);
    }
}
