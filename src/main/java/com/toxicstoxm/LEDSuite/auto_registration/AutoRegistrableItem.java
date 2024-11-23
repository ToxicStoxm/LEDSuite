package com.toxicstoxm.LEDSuite.auto_registration;

/**
 * Represents an item that can be automatically registered.
 * Interfaces or classes intended for auto-registration should implement this interface.
 *
 * @implNote The default implementation of {@link #getItemType()} returns a placeholder string.
 * Implementers should override this method to provide a meaningful item type.
 *
 * @since 1.0.0
 */
public interface AutoRegistrableItem {

    /**
     * Returns the type of this registrable item.
     * The type is typically used to categorize or identify the item during the registration process.
     *
     * @return a {@link String} representing the item type. Defaults to {@code "Unknown item type!"}.
     */
    default String getItemType() {
        return "Unknown item type!";
    }
}
