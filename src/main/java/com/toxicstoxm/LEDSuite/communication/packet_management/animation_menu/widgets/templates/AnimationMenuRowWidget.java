package com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.widgets.templates;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.DeserializableWidget;
import org.gnome.adw.PreferencesRow;
import org.gnome.glib.Type;
import org.jetbrains.annotations.NotNull;

public abstract class AnimationMenuRowWidget<T extends PreferencesRow> extends AnimationMenuWidget<T> {
    @Override
    public abstract String getType();

    @Override
    public abstract Type getWidgetType();

    @Override
    public T deserialize(@NotNull DeserializableWidget deserializableWidget) throws DeserializationException {
        super.deserialize(deserializableWidget);

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.TOOLTIP, widgetSection)) {
            widget.setTooltipText(widgetSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.TOOLTIP));
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.LABEL, widgetSection)) {
            widget.setTitle(widgetSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.LABEL));
        }

        return widget;
    }
}
