package com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.Registrable;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModule;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.menu_error.Code;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.menu_error.MenuErrorPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.menu_error.Severity;
import com.toxicstoxm.LEDSuite.tools.YamlTools;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.LEDSuite.ui.animation_menu.AnimationMenu;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.ConfigurationSection;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import org.gnome.adw.PreferencesGroup;
import org.jetbrains.annotations.NotNull;

public class AnimationMenuManager extends Registrable<Widget> {

    private final String widgetClassPath;

    public AnimationMenuManager(String widgetClassPath) {
        this.widgetClassPath = widgetClassPath;
    }

    @Override
    protected AutoRegisterModule<Widget> autoRegisterModule() {
        return AutoRegisterModule.<Widget>builder()
                .moduleType(Widget.class)
                .module(AutoRegisterModules.WIDGETS)
                .classPath(widgetClassPath)
                .build();
    }


    /**
     * Attempts to deserialize the given menu YAML string and construct a new animation menu object from it.
     * @param menuYAML the YAML string to deserialize
     * @return the new animation menu
     * @throws DeserializationException if something goes wrong while deserializing. E.g.: missing keys, invalid YAML or invalid values
     * @see #deserializeAnimationMenuGroup(String, ConfigurationSection)
     */
    public AnimationMenu deserializeAnimationMenu(String menuYAML) throws DeserializationException {
        // Try to load YAML string into a YAML object
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.loadFromString(menuYAML);
        } catch (InvalidConfigurationException e) {
            LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                    MenuErrorPacket.builder()
                            .message(e.getMessage())
                            .severity(Severity.SEVERE)
                            .code(Code.PARSE_ERROR)
                            .build().serialize()
            );
        }

        // Retrieve the menu id corresponding to an animation row and creating a new animation menu object with this id
        YamlTools.ensureKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.FILENAME, yaml);
        AnimationMenu animationMenu = AnimationMenu.create(yaml.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.FILENAME));

        // If no menu content section is found, return the empty menu
        if (!YamlTools.checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT, yaml)) {
            return animationMenu;
        }

        // Load the menu content section and ensure it's not null
        ConfigurationSection menuContentSection = yaml.getConfigurationSection(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT);
        if (menuContentSection == null) throw new DeserializationException("Menu content section is empty!");

        // Loop through the content widgets and ensure they are groups, because all top level widgets need to be groups
        // If a type key exists check if it is a group
        for (String menuGroupKey : menuContentSection.getKeys(false)) {
            ConfigurationSection menuGroupSection = menuContentSection.getConfigurationSection(menuGroupKey);
            if (menuGroupSection == null) throw new DeserializationException("Menu group '" + menuGroupKey + "' section is empty!");

            try {
                animationMenu.animationMenuContent.append(deserializeAnimationMenuGroup(menuGroupKey, menuGroupSection));
            } catch (DeserializationException e) {
                throw new DeserializationException("Failed to deserialize animation menu group '" + menuGroupKey + "'!", e);
            }
        }
        return animationMenu;
    }


    /**
     * Attempts to deserialize the specified menu group YAML section and constructs a new {@link PreferencesGroup}.
     * @param menuGroupKey the animation menu group id (used by the server for identifying individual menu widgets)
     * @param menuGroupSection the YAML config section to deserialize
     * @return the new {@link PreferencesGroup} object
     * @throws DeserializationException if something goes wrong while deserializing. E.g.: missing keys, invalid YAML or invalid values
     * @see #deserializeAnimationMenu(String)
     */
    private PreferencesGroup deserializeAnimationMenuGroup(@NotNull String menuGroupKey, @NotNull ConfigurationSection menuGroupSection) throws DeserializationException {

        if (YamlTools.checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE, menuGroupSection)) {
            String widgetType = menuGroupSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE);
            if (widgetType == null) throw new DeserializationException("Invalid widget type 'null' for top level group " + menuGroupKey + "'!");
            if (widgetType.equals(WidgetType.GROUP.getName())) throw new DeserializationException("Invalid top level widget type '" + widgetType +"' for '" + menuGroupKey + "' isn't a group! Top level widgets must be groups!");
        }

        PreferencesGroup menuGroup = PreferencesGroup.builder().build();

        if (YamlTools.checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.LABEL, menuGroupSection)) {
            menuGroup.setTitle(menuGroupSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.LABEL));
        }

        if (YamlTools.checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.TOOLTIP, menuGroupSection)) {
            menuGroup.setTooltipText(menuGroupSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.TOOLTIP));
        }

        // TODO properties

        if (!YamlTools.checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT, menuGroupSection)) throw new DeserializationException("Group content section is missing!");

        ConfigurationSection menuGroupContentSection = menuGroupSection.getConfigurationSection(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT);
        if (menuGroupContentSection == null) throw new DeserializationException("Group content section is empty!");

        for (String widgetKey : menuGroupContentSection.getKeys(false)) {
            ConfigurationSection widgetSection = menuGroupContentSection.getConfigurationSection(widgetKey);
            if (widgetSection == null) throw new DeserializationException("Widget section for widget '" + widgetKey + "' was null!");

            if (!YamlTools.checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE, widgetSection)) throw new DeserializationException("Widget type for widget '" + widgetKey + "' missing!");

            String widgetType = widgetSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE);

            if (!isRegistered(widgetType)) throw new DeserializationException("Invalid / Unknown widget type '" + widgetType + "' for widget '" + widgetKey +"'!");

            try {
                menuGroup.add(
                        get(widgetType).deserialize(
                                DeserializableWidget.builder()
                                        .widgetSection(widgetSection)
                                        .widgetKey(widgetKey)
                                        .build()
                        )
                );
            } catch (DeserializationException e) {
                throw new DeserializationException("Failed to deserialize widget '" + widgetKey + "' with type '" + widgetType + "'!", e);
            }
        }
        return menuGroup;
    }

}
