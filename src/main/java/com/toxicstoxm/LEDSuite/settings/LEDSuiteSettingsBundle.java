package com.toxicstoxm.LEDSuite.settings;


import com.toxicstoxm.YAJSI.api.settings.Setting;
import com.toxicstoxm.YAJSI.api.settings.SettingsBundle;
import com.toxicstoxm.YAJSI.api.settings.YAJSISetting;
import com.toxicstoxm.YAJSI.api.settings.YAMLSetting;
import lombok.Getter;

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

    @YAMLSetting(path = "LEDSuite.Debugging.Print-Logger-Test-Messages")
    public static class PrintLoggerTestMessages extends YAJSISetting<Boolean> {

        @Getter
        private static PrintLoggerTestMessages instance;

        public PrintLoggerTestMessages(Setting<Object> setting) {
            super(setting, Boolean.class);
            instance = this;
        }
    }
}