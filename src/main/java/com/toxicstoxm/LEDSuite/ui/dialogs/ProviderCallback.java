package com.toxicstoxm.LEDSuite.ui.dialogs;

import com.toxicstoxm.LEDSuite.ui.dialogs.status_dialog.StatusUpdate;

/**
 * A functional interface used as a callback for providing data to the application.
 * <p>This interface is designed to allow classes to retrieve data without holding a reference
 * to the entire class. It is commonly used to decouple data retrieval logic, enabling better
 * modularity and testability in the application.</p>
 *
 * @param <T> the type of data that is provided by the callback (e.g., {@link StatusUpdate})
 *
 * @since 1.0.0
 */
@FunctionalInterface
public interface ProviderCallback<T> {
    /**
     * Retrieves the data provided by the callback.
     *
     * @return the data of type {@code T}, which could be any data structure, such as
     *         {@link StatusUpdate} or other custom types.
     */
    T getData();
}
