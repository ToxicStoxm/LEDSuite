package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.widgets.templates;

/**
 * Fallback callback
 * that is used by some animation menu widgets
 * who don't have a good signal for reporting that the user changed their state.
 * @since 1.0.0
 */
@FunctionalInterface
public interface ChangedCallback {
    void onChanged();
}
