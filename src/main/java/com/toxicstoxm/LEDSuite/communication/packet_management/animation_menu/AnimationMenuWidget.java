package com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.tools.YamlTools;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.ConfigurationSection;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class AnimationMenuWidget implements Widget {

    protected String key;

    protected String toolTip;

    @Override
    public abstract String getType();

    @Override
    public DeserializableWidget serialize() {
        return save();
    }

    protected DeserializableWidget save() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE, getKey());
        return new DeserializableWidget(
                yaml,
                getKey()
        );
    }

    @Override
    public org.gnome.gtk.Widget deserialize(DeserializableWidget deserializableWidget) throws DeserializationException {
        this.key = deserializableWidget.widgetKey();
        ConfigurationSection widgetSection = deserializableWidget.widgetSection();
        if (widgetSection == null) throw new DeserializationException("Widget section for widget '" + getKey() + "' is null or invalid!");

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.TOOLTIP, widgetSection)) {
            toolTip = widgetSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.TOOLTIP);
        }
        return null;
    }

    protected boolean checkIfKeyExists(String key, @NotNull ConfigurationSection yaml) {
        return YamlTools.checkIfKeyExists(key, yaml);
    }

    protected void ensureKeyExists(String key, @NotNull ConfigurationSection yaml) {
        YamlTools.ensureKeyExists(key, yaml);
    }
}
