package com.toxicstoxm.LEDSuite.ui.dialogs;

import com.toxicstoxm.LEDSuite.ui.dialogs.status_dialog.StatusUpdate;

/**
 * A callback interface used for exposing an update method in a class to the rest of the application.
 * <p>This interface allows other parts of the application to trigger an update in a class without needing
 * a direct reference to the entire class. The class implementing this interface will provide an update
 * method to handle new data and refresh its state accordingly.</p>
 *
 * @param <T> the type of the data structure (e.g., {@link StatusUpdate}) that contains the new data
 *
 * @since 1.0.0
 */
public interface UpdateCallback<T> {

    /**
     * Updates the data within the class using the provided new values.
     *
     * @param newValues the new data values to update the class with, typically passed as a record or other
     *                  data structure
     */
    void update(T newValues);
}
