package com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.widgets;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.AnimationMenuWidget;
import com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.DeserializableWidget;
import com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.WidgetType;
import org.gnome.adw.ButtonRow;
import org.gnome.glib.Type;
import org.jetbrains.annotations.NotNull;

@AutoRegister(module = AutoRegisterModules.WIDGETS)
public class ButtonRowWidget extends AnimationMenuWidget<ButtonRow> {
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
        ButtonRow widget = super.deserialize(deserializableWidget);

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.LABEL, widgetSection)) {
            widget.setTitle(widgetSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.LABEL));
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.ButtonRow.START_ICON_NAME, widgetSection)) {
            widget.setStartIconName(widgetSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.ButtonRow.START_ICON_NAME));
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.ButtonRow.END_ICON_NAME, widgetSection)) {
            widget.setEndIconName(widgetSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.ButtonRow.END_ICON_NAME));
        }

        widget.onActivated(this::sendMenuChangeRequestWithoutValue);

        return widget;
    }
}
