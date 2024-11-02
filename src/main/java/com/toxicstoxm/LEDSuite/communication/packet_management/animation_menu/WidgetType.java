package com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu;

import org.jetbrains.annotations.NotNull;

public enum WidgetType {
    GROUP,
    BUTTON_ROW,
    BUTTON,
    ENTRY_ROW,
    PROPERTY_ROW,
    COMBO_ROW,
    SWITCH_ROW,
    SPIN_ROW,
    EXPANDER_ROW;

    public @NotNull String getName() {
        return this.name().toLowerCase();
    }

}
