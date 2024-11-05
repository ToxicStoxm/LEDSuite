package com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.widgets;

import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.AnimationMenuWidget;
import com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.DeserializableWidget;
import com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.WidgetType;
import org.gnome.adw.SpinRow;
import org.gnome.glib.Type;
import org.jetbrains.annotations.NotNull;

@AutoRegister(module = AutoRegisterModules.WIDGETS)
public class SpinRowWidget extends AnimationMenuWidget<SpinRow> {
    @Override
    public String getType() {
        return WidgetType.SPIN_ROW.getName();
    }

    @Override
    public Type getWidgetType() {
        return SpinRow.getType();
    }

    @Override
    public SpinRow deserialize(@NotNull DeserializableWidget deserializableWidget) throws DeserializationException {
        SpinRow widget = super.deserialize(deserializableWidget);

        widget.onOutput(() -> false);

        return widget;
    }
}
