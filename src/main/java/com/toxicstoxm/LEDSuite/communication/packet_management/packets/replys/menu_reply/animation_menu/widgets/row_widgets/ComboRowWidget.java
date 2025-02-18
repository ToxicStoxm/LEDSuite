package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.widgets.row_widgets;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.ErrorCode;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.DeserializableWidget;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.WidgetType;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.widgets.templates.AnimationMenuActionRowWidget;
import org.gnome.adw.ComboRow;
import org.gnome.glib.Type;
import org.gnome.gtk.StringList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @since 1.0.0
 */
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

        widget.setEnableSearch(
                getBooleanIfAvailable(Constants.Communication.YAML.Keys.Reply.MenuReply.ComboRow.ENABLE_SEARCH, true)
        );

        ensureKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT);
        List<String> content = widgetSection.getStringList(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT);

        if (content.isEmpty()) throw new DeserializationException("Combo row without content is disallowed!", ErrorCode.ComboRowWithoutContent);

        widget.setModel(
                StringList.builder().setStrings(content.toArray(new String[]{})).build()
        );

        widget.setSelected(
                content.indexOf(getStringIfAvailable(Constants.Communication.YAML.Keys.Reply.MenuReply.VALUE, ""))
        );

        onChanged(() -> sendMenuChangeRequest(String.valueOf(content.get(widget.getSelected()))));

        return widget;
    }
}
