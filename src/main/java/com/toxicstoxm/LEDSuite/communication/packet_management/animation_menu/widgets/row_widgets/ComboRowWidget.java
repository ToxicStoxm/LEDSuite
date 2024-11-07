package com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.widgets.row_widgets;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.DeserializableWidget;
import com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.WidgetType;
import com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.widgets.templates.AnimationMenuActionRowWidget;
import org.gnome.adw.ComboRow;
import org.gnome.glib.Type;
import org.gnome.gtk.Gtk;
import org.gnome.gtk.StringList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@AutoRegister(module = AutoRegisterModules.WIDGETS)
public class ComboRowWidget extends AnimationMenuActionRowWidget<ComboRow> {
    @Override
    public String getType() {
        return WidgetType.COMBO_ROW.getName();
    }

    @Override
    public Type getWidgetType() {
        return ComboRow.getType();
    }

    @Override
    public ComboRow deserialize(@NotNull DeserializableWidget deserializableWidget) throws DeserializationException {
        super.deserialize(deserializableWidget);

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.ComboRow.ENABLE_SEARCH)) {
            widget.setEnableSearch(widgetSection.getBoolean(Constants.Communication.YAML.Keys.Reply.MenuReply.ComboRow.ENABLE_SEARCH
            ));
        } else {
            widget.setEnableSearch(true);
        }

        ensureKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT);
        List<String> content = widgetSection.getStringList(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT);

        if (content.isEmpty()) throw new DeserializationException("Combo row without content is disallowed!");

        widget.setModel(
                StringList.builder().setStrings(content.toArray(new String[]{})).build()
        );

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.VALUE)) {
            widget.setSelected(content.indexOf(widgetSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.VALUE)));
        } else {
            widget.setSelected(Gtk.INVALID_LIST_POSITION);
        }

        onChanged(() -> sendMenuChangeRequest(String.valueOf(content.get(widget.getSelected()))));

        return widget;
    }
}
