package com.toxicstoxm.LEDSuite.ui.dialogs;

import com.toxicstoxm.LEDSuite.ui.dialogs.status_dialog.StatusUpdate;

/**
 * Callback used by classes for exposing their update method to the rest of the application.
 * Using this callback makes it
 * possible to update data within a class without having to keep a reference to that whole class.
 * @param <T> Data structure (record) which holds the new data. Example {@link StatusUpdate}
 * @since 1.0.0
 */
public interface UpdateCallback<T> {
    void update(T newValues);
}
