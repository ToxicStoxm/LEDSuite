package com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.widgets.row_widgets;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.DeserializableWidget;
import com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.WidgetType;
import com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.widgets.templates.AnimationMenuRowWidget;
import org.gnome.adw.ButtonRow;
import org.gnome.glib.Type;
import org.jetbrains.annotations.NotNull;

@AutoRegister(module = AutoRegisterModules.WIDGETS)
public class ButtonRowWidget extends AnimationMenuRowWidget<ButtonRow> {
    @Override
    public String getType() {
        return WidgetType.BUTTON_ROW.getName();
    }

    @Override
    public Type getWidgetType() {
        return ButtonRow.getType();
    }

    @Override
    public ButtonRow deserialize(@NotNull DeserializableWidget deserializableWidget) throws DeserializationException {
        super.deserialize(deserializableWidget);

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.ButtonRow.START_ICON_NAME)) {
            widget.setStartIconName(widgetSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.ButtonRow.START_ICON_NAME));
        }

        widget.setStartIconName(getStringIfAvailable(Constants.Communication.YAML.Keys.Reply.MenuReply.ButtonRow.START_ICON_NAME));
        widget.setEndIconName(getStringIfAvailable(Constants.Communication.YAML.Keys.Reply.MenuReply.ButtonRow.END_ICON_NAME));

        widget.onActivated(this::sendMenuChangeRequestWithoutValue);

        return widget;
    }
}
