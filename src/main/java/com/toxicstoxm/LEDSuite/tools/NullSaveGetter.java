package com.toxicstoxm.LEDSuite.tools;

import org.jetbrains.annotations.NotNull;

/**
 * A generic interface designed to provide a safe way to retrieve values,
 * ensuring that null values are handled gracefully.
 * <p>
 * Implementing classes are expected to define how the actual value is retrieved
 * and provide a default value in case the retrieved value is null.
 * </p>
 *
 * @param <T> the type of value this interface is working with
 * @since 1.0.0
 */
public interface NullSaveGetter<T> {

    /**
     * Retrieves the value from the implementing class. If the value is available
     * (i.e., not null), it returns that value; otherwise, it returns a default value.
     *
     * @return the value of type T, either the actual value if available, or a default value
     */
    default @NotNull T getInstance() {
        return isAvailable() ? value() : defaultValue();
    }

    /**
     * The method responsible for providing the actual value.
     * This method should be implemented to retrieve the value, which may potentially be null.
     *
     * @return the value of type T, which may be null
     */
    T value();

    /**
     * Checks whether the value is available (i.e., not null).
     * <p>
     * This method uses the {@link #value()} method to check if the value is null.
     * </p>
     *
     * @return true if the value is not null, false otherwise
     */
    default boolean isAvailable() {
        return value() != null;
    }

    /**
     * Provides a default value to return when the actual value is not available (i.e., is null).
     *
     * @return a default value of type T, never null
     */
    @NotNull T defaultValue();
}
