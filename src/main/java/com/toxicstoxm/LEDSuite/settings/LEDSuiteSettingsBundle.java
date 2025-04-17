package com.toxicstoxm.LEDSuite.settings;

import com.toxicstoxm.YAJSI.api.settings.YAMLConfiguration;
import com.toxicstoxm.YAJSI.api.settings.YAMLSetting;
import lombok.Getter;
import lombok.Setter;

/**
 * Settings bundle for LEDSuite using YAJSI framework.
 * This class provides access to application-specific settings, mapped to YAML configuration paths.
 * @since 1.0.0
 */
@YAMLConfiguration(name = "config.yaml")
public class LEDSuiteSettingsBundle {

    public LEDSuiteSettingsBundle() {

    }

    @YAMLSetting.Ignore
    @Getter
    @Setter
    private boolean demoMode = false;

    @YAMLSetting(name = "LEDSuite")
    public DefaultSettings mainSection = new DefaultSettings();

    public static class DefaultSettings {

        @YAMLSetting(name = "UI")
        public DefaultUISettings uiSettings = new DefaultUISettings();

        @YAMLSetting(name = "Network")
        public DefaultNetworkSettings networkSettings = new DefaultNetworkSettings();

        @YAMLSetting(name = "Debugging")
        public DefaultDebuggingSettings debuggingSettings = new DefaultDebuggingSettings();

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
