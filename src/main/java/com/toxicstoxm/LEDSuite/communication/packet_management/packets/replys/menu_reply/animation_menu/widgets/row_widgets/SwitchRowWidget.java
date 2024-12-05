package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.widgets.row_widgets;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.DeserializableWidget;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.WidgetType;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.widgets.templates.AnimationMenuActionRowWidget;
import org.gnome.adw.SwitchRow;
import org.gnome.glib.Type;
import org.jetbrains.annotations.NotNull;

/**
 * @since 1.0.0
 */
@AutoRegister(module = AutoRegisterModules.WIDGETS)
public class SwitchRowWidget extends AnimationMenuActionRowWidget<SwitchRow> {

    @Override
    public String getType() {
        return WidgetType.SWITCH_ROW.getName();
    }

    @Override
    public Type getWidgetType() {
        return SwitchRow.getType();
    }

    @Override
    public SwitchRow deserialize(@NotNull DeserializableWidget deserializableWidget) throws DeserializationException {
        super.deserialize(deserializableWidget);

        widget.setActive(
                getBooleanIfAvailable(Constants.Communication.YAML.Keys.Reply.MenuReply.VALUE, false)
        );

        onChanged(() -> sendMenuChangeRequest(widget.getActive()));

        return widget;
    }
}
