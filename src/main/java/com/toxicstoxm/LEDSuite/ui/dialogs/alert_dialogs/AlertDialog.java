package com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs;

import com.toxicstoxm.LEDSuite.auto_registration.AutoRegistrableItem;
import org.gnome.gtk.Widget;

/**
 * A generic interface for creating and presenting alert dialogs in the LEDSuite application.
 * <p>
 * This interface defines the contract for alert dialogs that can be created, customized, and presented
 * in the application's user interface. It allows for dynamic creation and customization of alert dialogs
 * with specific data, and it provides a method for presenting the dialog within a given parent widget.
 * </p>
 *
 * @param <T> the type of data that the alert dialog will use for initialization
 * @since 1.0.0
 */
public interface AlertDialog<T> extends AutoRegistrableItem {

    /**
     * Creates a new instance of the alert dialog using the provided data.
     * This method is used to initialize the dialog with the data required for its display.
     *
     * @param data the data used to customize the alert dialog
     * @return a new instance of the alert dialog, customized with the provided data
     */
    AlertDialog<T> create(T data);

    /**
     * Presents the alert dialog within the specified parent widget.
     * This method displays the dialog on the screen and ensures it is attached to the parent widget
     * where it should be shown in the user interface.
     *
     * @param parent the parent widget to which the dialog will be attached
     */
    void present(Widget parent);
}
