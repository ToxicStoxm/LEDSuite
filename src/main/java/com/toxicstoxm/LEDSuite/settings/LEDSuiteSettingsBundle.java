package com.toxicstoxm.LEDSuite.settings;

import com.toxicstoxm.YAJSI.Setting;
import com.toxicstoxm.YAJSI.SettingsBundle;
import com.toxicstoxm.YAJSI.YAJSISetting;
import com.toxicstoxm.YAJSI.YAMLSetting;
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
