package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.widgets.templates;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.DeserializableWidget;
import org.gnome.adw.ActionRow;
import org.gnome.glib.Type;
import org.jetbrains.annotations.NotNull;

public abstract class AnimationMenuActionRowWidget<T extends ActionRow> extends AnimationMenuRowWidget<T>{
    @Override
    public abstract String getType();

    @Override
    public abstract Type getWidgetType();

    @Override
    public T deserialize(@NotNull DeserializableWidget deserializableWidget) throws DeserializationException {
        super.deserialize(deserializableWidget);

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.SUBTITLE)) {
            widget.setSubtitle(widgetSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.SUBTITLE));
        }

        return widget;
    }
}