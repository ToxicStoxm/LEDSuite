package com.toxicstoxm.LEDSuite.settings;

import com.toxicstoxm.YAJSI.ConfigType;
import com.toxicstoxm.YAJSI.SettingsBundle;
import com.toxicstoxm.YAJSI.YAMLSetting;
import com.toxicstoxm.YAJSI.upgrading.ConfigVersion;
import lombok.Getter;
import lombok.Setter;

import java.io.File;

/**
 * Settings bundle for LEDSuite using YAJSI framework.
 * This class provides access to application-specific settings, mapped to YAML configuration paths.
 * @since 1.0.0
 */
public class LEDSuiteSettingsBundle extends SettingsBundle {
    public LEDSuiteSettingsBundle(File f) {
        super(new ConfigVersion(1, 0, 0), f, ConfigType.SETTINGS);
    }

    @YAMLSetting.Ignore
    @Getter
    @Setter
    private boolean demoMode = false;

    @YAMLSetting(name = "LEDSuite")
    public LEDSuiteSettingsBundle.DefaultSettings mainSection = new LEDSuiteSettingsBundle.DefaultSettings();

    public static class DefaultSettings {

        @YAMLSetting(name = "UI")
        public LEDSuiteSettingsBundle.DefaultSettings.DefaultUISettings uiSettings = new LEDSuiteSettingsBundle.DefaultSettings.DefaultUISettings();

        @YAMLSetting(name = "Network")
        public LEDSuiteSettingsBundle.DefaultSettings.DefaultNetworkSettings networkSettings = new LEDSuiteSettingsBundle.DefaultSettings.DefaultNetworkSettings();

        @YAMLSetting(name = "Debugging")
        public LEDSuiteSettingsBundle.DefaultSettings.DefaultDebuggingSettings debuggingSettings = new LEDSuiteSettingsBundle.DefaultSettings.DefaultDebuggingSettings();

        public static class DefaultUISettings {
            @YAMLSetting(name = "FilePicker-Initial-Folder")
            public String filePickerInitialFolder = "";
        }

        public static class DefaultNetworkSettings {
            @YAMLSetting(name = "Websocket-URI")
            public String websocketURI = "wss://www.yourWebsocket.com/yourWebsocketEndpoint/";
            @YAMLSetting(name = "Packet-Size-Bytes")
            public int packetSizeBytes = 131072;
        }

        public static class DefaultDebuggingSettings {
            @YAMLSetting(name = "Enable-Settings-Logging")
            public boolean enableSettingsDebugLogging = false;
        }
    }
}
