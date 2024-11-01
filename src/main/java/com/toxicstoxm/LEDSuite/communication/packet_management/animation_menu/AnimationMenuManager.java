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

import java.util.HashMap;

public class AnimationMenuManager extends Registrable<Widget> {

    private final String classPath;

    private final HashMap<String, Widget> registeredWidgets = new HashMap<>();

    public AnimationMenuManager(String classPath) {
        this.classPath = classPath;
    }

    public boolean registerWidget(Widget widget) {
        return registeredWidgets.putIfAbsent(widget.getType(), widget) == null;
    }

    @Override
    protected AutoRegisterModule<Widget> autoRegisterModule() {
        return AutoRegisterModule.<Widget>builder()
                .moduleType(Widget.class)
                .module(AutoRegisterModules.WIDGETS)
                .classPath(classPath)
                .build();
    }


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
                animationMenu.append(deserializeAnimationMenuGroup(menuGroupKey, menuGroupSection));
            } catch (DeserializationException e) {
                throw new DeserializationException("Failed to deserialize animation menu group '" + menuGroupKey + "'!", e);
            }
        }
        return animationMenu;
    }


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

        //if (YamlTools.checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.))

        // TODO deserialize group content


        return menuGroup;
    }

}
