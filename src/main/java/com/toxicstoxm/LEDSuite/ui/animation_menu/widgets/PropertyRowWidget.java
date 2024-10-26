package com.toxicstoxm.LEDSuite.ui.animation_menu.widgets;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.PacketManager;
import com.toxicstoxm.LEDSuite.ui.animation_menu.AnimationMenuWidget;
import com.toxicstoxm.LEDSuite.ui.animation_menu.CallbackRelay;
import com.toxicstoxm.LEDSuite.ui.animation_menu.Widget;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.ConfigurationSection;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.gnome.adw.ActionRow;
import org.jetbrains.annotations.NotNull;

@Builder
@Getter
@Setter
public class PropertyRowWidget extends AnimationMenuWidget {

    private String label;
    private String text;

    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.MenuReply.Types.PROPERTY_ROW;
    }

    @Override
    public YamlConfiguration serialize() {
        YamlConfiguration yaml = saveYAML();

        yaml.set(Constants.Communication.YAML.Keys.MenuReply.LABEL, label);
        yaml.set(Constants.Communication.YAML.Keys.MenuReply.Property.TEXT, text);

        return yaml;
    }

    @Override
    public Widget deserialize(@NotNull ConfigurationSection widgetSection, String yamlPath) throws PacketManager.DeserializationException {

        callbackPath = yamlPath;

        ensureKeyExists(Constants.Communication.YAML.Keys.MenuReply.LABEL, widgetSection);
        label = widgetSection.getString(Constants.Communication.YAML.Keys.MenuReply.LABEL);

        ensureKeyExists(Constants.Communication.YAML.Keys.MenuReply.Property.TEXT, widgetSection);
        text = widgetSection.getString(Constants.Communication.YAML.Keys.MenuReply.Property.TEXT);

        return this;
    }

    @Override
    public org.gnome.gtk.Widget asAdwaitaWidget(CallbackRelay callbackRelay) {
        return ActionRow.builder()
                .setTitle(label)
                .setSubtitle(text)
                .setCssClasses(new String[]{"property"})
                .build();
    }
}
