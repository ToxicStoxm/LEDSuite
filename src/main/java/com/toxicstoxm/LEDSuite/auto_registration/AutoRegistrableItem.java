package com.toxicstoxm.LEDSuite.auto_registration;

/**
 * Registrable items (interfaces) must extend this interface.
 * @since 1.0.0
 */
public interface AutoRegistrableItem {
    default String getItemType() {
        return "Unknown item type!";
    }
}
