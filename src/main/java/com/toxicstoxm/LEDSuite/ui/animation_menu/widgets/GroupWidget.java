package com.toxicstoxm.LEDSuite.ui.animation_menu.widgets;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.PacketManager;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.MenuChangeRequestPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.MenuRequestPacket;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.LEDSuite.ui.animation_menu.AnimationMenuWidget;
import com.toxicstoxm.LEDSuite.ui.animation_menu.CallbackRelay;
import com.toxicstoxm.LEDSuite.ui.animation_menu.Widget;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.ConfigurationSection;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.gnome.adw.PreferencesGroup;
import org.gnome.gtk.Button;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
public class GroupWidget extends AnimationMenuWidget {

    private GroupSuffixWidget suffixWidget;
    private String label;
    private List<Widget> content;

    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.MenuReply.Types.GROUP;
    }

    @Override
    public YamlConfiguration serialize() {
        YamlConfiguration yaml = saveYAML();

        if (content != null && !content.isEmpty()) {
            for (int i = 0; i < content.size(); i++) {
                yaml.set(Constants.Communication.YAML.Keys.MenuReply.CONTENT + "." + i, content.get(i).serialize());
            }
        }

        return yaml;
    }

    @Override
    public Widget deserialize(@NotNull ConfigurationSection widgetSection, String yamlPath) throws PacketManager.DeserializationException {

        callbackPath = yamlPath;

        ensureKeyExists(Constants.Communication.YAML.Keys.MenuReply.LABEL, widgetSection);
        label = widgetSection.getString(Constants.Communication.YAML.Keys.MenuReply.LABEL);

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.MenuReply.Groups.SUFFIX, widgetSection)) {
            ConfigurationSection suffixSection = widgetSection.getConfigurationSection(Constants.Communication.YAML.Keys.MenuReply.Groups.SUFFIX);
            if (suffixSection == null) throw new PacketManager.DeserializationException("Wasn't able to correctly retrieve group suffix section for group!", new NullPointerException("Group suffix section is null!"));
            ensureKeyExists(Constants.Communication.YAML.Keys.MenuReply.Groups.Suffix.ICON_NAME, suffixSection);
            ensureKeyExists(Constants.Communication.YAML.Keys.MenuReply.LABEL, suffixSection);
            suffixWidget = new GroupSuffixWidget(
                    suffixSection.getString(Constants.Communication.YAML.Keys.MenuReply.LABEL),
                    suffixSection.getString(Constants.Communication.YAML.Keys.MenuReply.Groups.Suffix.ICON_NAME)
            );
        }

        ensureKeyExists(Constants.Communication.YAML.Keys.MenuReply.CONTENT, widgetSection);
        ConfigurationSection contentSection = widgetSection.getConfigurationSection(Constants.Communication.YAML.Keys.MenuReply.CONTENT);
        if (contentSection == null) throw new PacketManager.DeserializationException("Wasn't able to correctly retrieve group content section for group!", new NullPointerException("Group content section is null!"));
        for (String contentWidget : contentSection.getKeys(false)) {
            ConfigurationSection contentWidgetSection = contentSection.getConfigurationSection(contentWidget);
            if (contentWidgetSection == null) throw new PacketManager.DeserializationException("Wasn't able to correctly retrieve content widget section for group!", new NullPointerException("Content widget section is null!"));
            content = new ArrayList<>();
            content.add(LEDSuiteApplication.getAnimationMenuConstructor().deserialize(contentWidgetSection, callbackPath + "." + contentWidget));
        }
        return this;
    }

    @Override
    public PreferencesGroup asAdwaitaWidget(CallbackRelay callbackRelay) {

        PreferencesGroup group = PreferencesGroup.builder().setTitle(label).build();

        if (suffixWidget != null) {
            var suffixButton = Button.withLabel(suffixWidget.label());
            String iconName = suffixWidget.icon_name();
            if (iconName != null && !iconName.isBlank()) suffixButton.setIconName(iconName);

            suffixButton.onClicked(() ->
                    callbackRelay.enqueueMessage(
                            MenuChangeRequestPacket.builder()
                                    .objectPath(callbackPath + "suffix")
                                    .objectValue("")
                                    .build().serialize()
                    )
            );

            group.setHeaderSuffix(
                suffixButton
            );
        }

        for (Widget widget : content) {
            group.add(widget.asAdwaitaWidget(callbackRelay));
        }

        return group;
    }
}
