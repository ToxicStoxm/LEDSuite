package com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu;

import com.toxicstoxm.LEDSuite.auto_registration.AutoRegistrableItem;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.Serializable;

public interface Widget extends Serializable, AutoRegistrableItem {

    @Override
    default String getItemType() {
        return getType();
    }

    String getType();

    @Override
    String serialize();

    @Override
    Serializable deserialize(String string) throws DeserializationException;
}
