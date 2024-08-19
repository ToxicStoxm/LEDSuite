package com.toxicstoxm.LEDSuite.settings.config;

public interface SettingsAccessor {
    Setting<Object> get(String path);
}
