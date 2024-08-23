package com.toxicstoxm.LEDSuite.settings.config;


import com.toxicstoxm.LEDSuite.settings.yaml.InvalidConfigurationException;
import com.toxicstoxm.LEDSuite.settings.yaml.file.YamlConfiguration;
import lombok.SneakyThrows;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class LEDSuiteSettingsManager {
    private final ConcurrentHashMap<String, Setting<Object>> settings = new ConcurrentHashMap<>();

    public LEDSuiteSettingsManager(String file) {
        load(file);
    }

    @SneakyThrows
    public void load(String file) {
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }

        for (String key : yaml.getKeys(true)) {
            settings.put(key, new LEDSuiteSetting<>(yaml.get(key)));
        }

        // Optionally load settings via SettingsManager
        new SettingsHandler<>().loadSettings(LEDSuiteSettingsBundle.class, settings::get);
    }

    public <T> T getSetting(Class<? extends LEDSuiteSetting<T>> settingClass) {
        try {
            // Find the inner class by name
            String path = settingClass.getAnnotation(YAMLSetting.class).path();
            // Access the setting from the map and cast it
            return settingClass.cast(settingClass.getConstructor(Setting.class).newInstance(get(path))).get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get setting for: " + settingClass.getName(), e);
        }
    }

    public Setting<Object> get(String s) {
        return settings.get(s);
    }

    @SneakyThrows
    public void save(String file) {
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }

        // Save settings via SettingsManager
        new SettingsHandler<>().saveSettings(LEDSuiteSettingsBundle.class, yaml);

        try {
            yaml.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
