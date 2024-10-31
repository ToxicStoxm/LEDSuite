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
        // Try to load
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

        YamlTools.ensureKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.FILENAME, yaml);
        AnimationMenu animationMenu = AnimationMenu.create(yaml.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.FILENAME));

        if (!YamlTools.checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT, yaml)) {
            return animationMenu;
        }

        ConfigurationSection menuContentSection = yaml.getConfigurationSection(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT);
        if (menuContentSection == null) throw new DeserializationException("Menu content section is empty!");

        for (String menuGroupKey : menuContentSection.getKeys(false)) {
            ConfigurationSection menuGroupSection = menuContentSection.getConfigurationSection(menuGroupKey);
            if (menuGroupSection == null) throw new DeserializationException("Menu group '" + menuGroupKey + "' content section is empty!");

            if (YamlTools.checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE, menuGroupSection)) {
                String widgetType = menuGroupSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE);
                if (widgetType == null) throw new DeserializationException("Invalid widget type 'null' for top level group " + menuGroupKey + "'!");
                if (widgetType.equals(WidgetType.GROUP.getName())) throw new DeserializationException("Invalid top level widget type '" + widgetType +"' for '" + menuGroupKey + "' isn't a group! Top level widgets must be groups!");
            }
        }
        return null;
    }

}
