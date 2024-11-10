package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.widgets.templates;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.DeserializableWidget;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.widgets.row_widgets.PropertyRowWidget;
import org.gnome.adw.PreferencesRow;
import org.gnome.glib.Type;
import org.jetbrains.annotations.NotNull;

/**
 * Wrapper class for animation menu row widgets. E.g.: {@link PropertyRowWidget}
 * @since 1.0.0
 */
public abstract class AnimationMenuRowWidget<T extends PreferencesRow> extends AnimationMenuWidget<T> {
    @Override
    public abstract String getType();

    @Override
    public abstract Type getWidgetType();

    @Override
    public T deserialize(@NotNull DeserializableWidget deserializableWidget) throws DeserializationException {
        super.deserialize(deserializableWidget);

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.TOOLTIP)) {
            widget.setTooltipText(widgetSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.TOOLTIP));
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.LABEL)) {
            widget.setTitle(widgetSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.LABEL));
        }

        return widget;
    }
}
