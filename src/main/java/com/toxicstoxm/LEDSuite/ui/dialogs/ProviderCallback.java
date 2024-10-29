package com.toxicstoxm.LEDSuite.ui.dialogs;

import com.toxicstoxm.LEDSuite.ui.dialogs.status_dialog.StatusUpdate;

/**
 * Callback used by classes for providing data to the rest of the application.
 * Using this callback makes it
 * possible to retrieve data from a class without having to keep a reference to the whole class.
 * @param <T> Provided data structure (record). Example {@link StatusUpdate}
 * @since 1.0.0
 */
@FunctionalInterface
public interface ProviderCallback<T> {
    T getData();
}
