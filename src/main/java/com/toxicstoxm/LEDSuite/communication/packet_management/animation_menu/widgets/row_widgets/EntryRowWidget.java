package com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.widgets.row_widgets;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.DeserializableWidget;
import com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.WidgetType;
import com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.widgets.templates.AnimationMenuRowWidget;
import org.gnome.adw.EntryRow;
import org.gnome.glib.Type;
import org.gnome.pango.AttrList;
import org.jetbrains.annotations.NotNull;

@AutoRegister(module = AutoRegisterModules.WIDGETS)
public class EntryRowWidget extends AnimationMenuRowWidget<EntryRow> {
    @Override
    public String getType() {
        return WidgetType.ENTRY_ROW.getName();
    }

    @Override
    public Type getWidgetType() {
        return EntryRow.getType();
    }

    @Override
    public EntryRow deserialize(@NotNull DeserializableWidget deserializableWidget) throws DeserializationException {
        super.deserialize(deserializableWidget);

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.SUBTITLE, widgetSection)) {
            widget.setText(widgetSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.SUBTITLE));
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.EntryRow.APPLY_BUTTON, widgetSection)) {
            widget.setShowApplyButton(widgetSection.getBoolean(Constants.Communication.YAML.Keys.Reply.MenuReply.EntryRow.APPLY_BUTTON));
        } else widget.setShowApplyButton(true);

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.EntryRow.EDITABLE, widgetSection)) {
            widget.setEditable(widgetSection.getBoolean(Constants.Communication.YAML.Keys.Reply.MenuReply.EntryRow.EDITABLE));
        } else widget.setEditable(true);

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.EntryRow.ATTRIBUTE_STRING, widgetSection)) {
            widget.setAttributes(AttrList.fromString(widgetSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.EntryRow.ATTRIBUTE_STRING)));
        }

        if (widget.getShowApplyButton()) {
            widget.onApply(() -> sendMenuChangeRequest(widget.getText()));
        } else {
            widget.onChanged(() -> sendMenuChangeRequest(widget.getText()));
        }

        return widget;
    }
}
