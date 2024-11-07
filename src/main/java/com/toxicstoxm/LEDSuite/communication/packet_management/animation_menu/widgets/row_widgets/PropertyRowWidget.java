package com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.widgets.row_widgets;

import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.DeserializableWidget;
import com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.WidgetType;
import com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.widgets.templates.AnimationMenuActionRowWidget;
import org.gnome.adw.ActionRow;
import org.gnome.glib.Type;
import org.jetbrains.annotations.NotNull;

@AutoRegister(module = AutoRegisterModules.WIDGETS)
public class PropertyRowWidget extends AnimationMenuActionRowWidget<ActionRow> {

    @Override
    public String getType() {
        return WidgetType.PROPERTY_ROW.getName();
    }

    @Override
    public Type getWidgetType() {
        return ActionRow.getType();
    }

    @Override
    public ActionRow deserialize(@NotNull DeserializableWidget deserializableWidget) throws DeserializationException {
        super.deserialize(deserializableWidget);

        widget.setCssClasses(new String[]{"property"});
        widget.setSubtitleSelectable(true);

        return widget;
    }
}
