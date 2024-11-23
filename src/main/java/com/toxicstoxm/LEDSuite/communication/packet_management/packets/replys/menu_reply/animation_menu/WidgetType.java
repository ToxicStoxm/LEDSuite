package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu;

import org.jetbrains.annotations.NotNull;

/**
 * WidgetType for {@link Widget} derivatives.
 * @since 1.0.0
 */
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
