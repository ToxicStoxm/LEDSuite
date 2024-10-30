package com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu;

import com.toxicstoxm.LEDSuite.auto_registration.Registrable;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModule;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.menu_error.Code;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.menu_error.MenuErrorPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.menu_error.Severity;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.LEDSuite.ui.animation_menu.AnimationMenu;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
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
        return null;
    }

}
