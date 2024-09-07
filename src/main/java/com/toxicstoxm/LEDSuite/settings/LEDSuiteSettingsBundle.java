package com.toxicstoxm.LEDSuite.settings;


import com.toxicstoxm.YAJSI.api.settings.Setting;
import com.toxicstoxm.YAJSI.api.settings.SettingsBundle;
import com.toxicstoxm.YAJSI.api.settings.YAJSISetting;
import com.toxicstoxm.YAJSI.api.settings.YAMLSetting;
import lombok.Getter;

public class LEDSuiteSettingsBundle implements SettingsBundle {
    @YAMLSetting(path = "LEDSuite.Test")
    public static class EnableTest extends YAJSISetting<Boolean> {

        @Getter
        public static EnableTest instance;

        public EnableTest(Setting<Object> setting) {
            super(setting, Boolean.class);
            instance = this;
        }
    }
}
