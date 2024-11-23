package com.toxicstoxm.LEDSuite.settings;

import com.toxicstoxm.YAJSI.api.settings.Setting;
import com.toxicstoxm.YAJSI.api.settings.SettingsBundle;
import com.toxicstoxm.YAJSI.api.settings.YAJSISetting;
import com.toxicstoxm.YAJSI.api.settings.YAMLSetting;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Settings bundle for LEDSuite using YAJSI framework.
 * This class provides access to application-specific settings, mapped to YAML configuration paths.
 * @since 1.0.0
 * @see YAJSISetting
 * @see SettingsBundle
 */
public class LEDSuiteSettingsBundle implements SettingsBundle {

    /**
     * Setting to enable or disable logging of settings.
     */
    @YAMLSetting(path = "LEDSuite.Debugging.Enable-Settings-Logging")
    public static class EnableSettingsLogging extends YAJSISetting<Boolean> {

        @Getter
        private static EnableSettingsLogging instance;

        public EnableSettingsLogging(Setting<Object> setting) {
            super(setting, Boolean.class);
            instance = this;
        }
    }

    /**
     * Setting for the WebSocket URI used in LEDSuite network communication.
     */
    @YAMLSetting(path = "LEDSuite.Network.Websocket-URI")
    public static class WebsocketURI extends YAJSISetting<String> {

        @Getter
        private static WebsocketURI instance;

        public WebsocketURI(Setting<Object> setting) {
            super(setting, String.class);
            instance = this;
        }
    }

    /**
     * Setting for the initial folder path used by the file picker in the UI.
     */
    @YAMLSetting(path = "LEDSuite.UI.FilePicker-Initial-Folder")
    public static class FilePickerInitialFolder extends YAJSISetting<String> {

        @Getter
        private static FilePickerInitialFolder instance;

        public FilePickerInitialFolder(Setting<Object> setting) {
            super(setting, String.class);
            instance = this;
        }
    }

    /**
     * Setting for the packet size in bytes used for network communication.
     */
    @YAMLSetting(path = "LEDSuite.Network.Packet-Size-Bytes")
    public static class PacketSize extends YAJSISetting<Integer> {

        @Getter
        private static PacketSize instance;

        public PacketSize(@NotNull Setting<Object> setting) {
            super(setting, Integer.class);
            instance = this;
        }
    }
}
