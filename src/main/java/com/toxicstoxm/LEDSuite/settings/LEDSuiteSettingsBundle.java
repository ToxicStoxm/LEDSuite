package com.toxicstoxm.LEDSuite.settings;


import com.toxicstoxm.YAJSI.api.settings.Setting;
import com.toxicstoxm.YAJSI.api.settings.SettingsBundle;
import com.toxicstoxm.YAJSI.api.settings.YAJSISetting;
import com.toxicstoxm.YAJSI.api.settings.YAMLSetting;
import lombok.Getter;

/**
 * YAJSI settings bundle for LEDSuite.
 * @since 1.0.0
 * @see YAJSISetting
 * @see SettingsBundle
 */
public class LEDSuiteSettingsBundle implements SettingsBundle {

    @YAMLSetting(path = "LEDSuite.Debugging.Enable-Settings-Logging")
    public static class EnableSettingsLogging extends YAJSISetting<Boolean> {

        @Getter
        private static EnableSettingsLogging instance;

        public EnableSettingsLogging(Setting<Object> setting) {
            super(setting, Boolean.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "LEDSuite.Network.Websocket-URI")
    public static class WebsocketURI extends YAJSISetting<String> {

        @Getter
        private static WebsocketURI instance;

        public WebsocketURI(Setting<Object> setting) {
            super(setting, String.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "LEDSuite.UI.FilePicker-Initial-Folder")
    public static class FilePickerInitialFolder extends YAJSISetting<String> {

        @Getter
        private static FilePickerInitialFolder instance;

        public FilePickerInitialFolder(Setting<Object> setting) {
            super(setting, String.class);
            instance = this;
        }
    }
}
