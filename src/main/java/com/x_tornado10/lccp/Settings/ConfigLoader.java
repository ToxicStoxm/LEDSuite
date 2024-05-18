package com.x_tornado10.lccp.Settings;

import com.x_tornado10.lccp.LCCP;
import com.x_tornado10.lccp.util.Paths;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;

public class ConfigLoader {
    public static void loadConfigsFromFile(Local_Settings settings, Server_Settings server_settings) {
        File file = new File(Paths.config);
        File file1 = new File(Paths.server_config);
        try {
            // parsing config and loading the values from storage (Default: ./LED-Cube-Control-Panel/config.yaml)
            // using Apache-Commons-Config (and dependencies like snakeyaml and commons-beanutils)
            Configurations configs = new Configurations();
            FileBasedConfiguration config = configs.properties(file);
            // settings are loaded into an instance of the settings class, so they can be used during runtime without any IO-Calls
            settings.load(config);
        } catch (ConfigurationException e) {
            // if any errors occur during config parsing an error is displayed in the console
            // the program is halted to prevent any further unwanted behavior
            LCCP.logger.error("Failed to parse config.yaml!");
            LCCP.logger.warn("Application was halted!");
            LCCP.logger.warn("If this keeps happening please open an issue on GitHub!");
            LCCP.logger.warn("Please restart the application!");
            LCCP.exit(0);
            return;
        }

        try {
            // parsing config and loading the values from storage (Default: ./LED-Cube-Control-Panel/server_config.yaml)
            // using Apache-Commons-Config (and dependencies like snakeyaml and commons-beanutils)
            Configurations configs = new Configurations();
            FileBasedConfiguration server_config = configs.properties(file1);
            // settings are loaded into an instance of the settings class, so they can be used during runtime without any IO-Calls
            server_settings.load(server_config);
        } catch (ConfigurationException e) {
            // if any errors occur during config parsing an error is displayed in the console
            // the program is halted to prevent any further unwanted behavior
            LCCP.logger.error("Failed to parse server_config.yaml!");
            LCCP.logger.warn("Application was halted!");
            LCCP.logger.warn("If this keeps happening please open an issue on GitHub!");
            LCCP.logger.warn("Please restart the application!");
            LCCP.exit(0);
        }
    }
}
