package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu;

import com.toxicstoxm.LEDSuite.auto_registration.AutoRegistrableItem;
import com.toxicstoxm.LEDSuite.auto_registration.Registrable;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.Serializable;
import com.toxicstoxm.LEDSuite.ui.animation_menu.AnimationMenu;

/**
 * This interface represents a widget that can be used to populate an {@link AnimationMenu}.
 * <p>All widget types in the animation menu should implement this interface. Implementations
 * of this interface define how widgets are serialized and deserialized, and can be automatically registered
 * within the system. For more information on automatic registration, see {@link Registrable}.</p>
 *
 * @since 1.0.0
 */
public interface Widget extends Serializable<DeserializableWidget, org.gnome.gtk.Widget>, AutoRegistrableItem {

    /**
     * Returns the type of the widget, which is a string representation of the specific
     * widget type (e.g., button, slider, etc.). This type is used to categorize and
     * identify the widget within the animation menu.
     *
     * @return the widget type as a {@link String}.
     */
    String getType();

    /**
     * Serializes the widget into a {@link DeserializableWidget} representation.
     * This method is used to convert the widget into a serializable form that can be stored or transmitted.
     *
     * @return the serialized {@link DeserializableWidget} representation of the widget.
     */
    @Override
    DeserializableWidget serialize();

    /**
     * Deserializes a {@link DeserializableWidget} into an instance of a {@link org.gnome.gtk.Widget}.
     * This method is used to reconstruct the widget from its serialized form, typically for use within
     * a graphical user interface (GUI).
     *
     * @param deserializableWidget the serialized widget data to deserialize.
     * @return the deserialized {@link org.gnome.gtk.Widget}.
     * @throws DeserializationException if deserialization fails.
     */
    @Override
    org.gnome.gtk.Widget deserialize(DeserializableWidget deserializableWidget) throws DeserializationException;

    /**
     * Returns the type of the item being registered. This is used for automatic registration
     * of the widget type in the system. The default implementation returns the widget's type.
     *
     * @return the type of the widget as a {@link String}.
     */
    @Override
    default String getItemType() {
        return getType();
    }
}
