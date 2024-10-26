package com.toxicstoxm.LEDSuite.ui.animation_menu;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.PacketManager;
import com.toxicstoxm.LEDSuite.formatting.StringFormatter;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.LEDSuite.yaml.YamlTools;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.ConfigurationSection;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;

public abstract class AnimationMenuWidget implements Widget {

    @Override
    public abstract String getType();

    @Override
    public YamlConfiguration serialize() {
        return saveYAML();
    }

    @Override
    public Widget deserialize(@NotNull ConfigurationSection widgetSection) throws PacketManager.DeserializationException {
        LEDSuiteApplication.getLogger().warn("Deserialization implementation missing for: " + StringFormatter.getClassName(getClass()) + "!", new LEDSuiteLogAreas.YAML());
        return null;
    }

    protected YamlConfiguration loadYAML(String yamlString) throws InvalidConfigurationException {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.loadFromString(yamlString);
        return yaml;
    }

    protected YamlConfiguration saveYAML() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set(Constants.Communication.YAML.Keys.MenuReply.TYPE, getType());
        return yaml;
    }

    protected boolean checkIfKeyExists(String key, @NotNull ConfigurationSection yaml) {
        return YamlTools.checkIfKeyExists(key, yaml);
    }

    protected void ensureKeyExists(String key, @NotNull ConfigurationSection yaml) {
        YamlTools.ensureKeyExists(key, yaml);
    }
}
