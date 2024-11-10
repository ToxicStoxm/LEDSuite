package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu;

import com.toxicstoxm.LEDSuite.auto_registration.AutoRegistrableItem;
import com.toxicstoxm.LEDSuite.auto_registration.Registrable;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.Serializable;
import com.toxicstoxm.LEDSuite.ui.animation_menu.AnimationMenu;

/**
 * Derivatives of this class are used to populate {@link AnimationMenu}s.
 * This class can be automatically registered.
 * For more information see {@link Registrable}.
 * @since 1.0.0
 */
public interface Widget extends Serializable<DeserializableWidget, org.gnome.gtk.Widget>, AutoRegistrableItem {

    @Override
    default String getItemType() {
        return getType();
    }

    /**
     * The widget-type of the child class
     * @return {@link WidgetType}
     */
    String getType();

    @Override
    DeserializableWidget serialize();

    @Override
    org.gnome.gtk.Widget deserialize(DeserializableWidget deserializableWidget) throws DeserializationException;
}
