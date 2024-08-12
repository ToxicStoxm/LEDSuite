package com.toxicstoxm.LEDSuite.cache;

import com.toxicstoxm.LEDSuite.LEDSuite;
import lombok.NonNull;

import java.util.HashMap;

/**
 * This is a wrapper of the standard HashMap implementation.
 * <p>
 * This class can be used instead of creating a lot of global variables. It allows for easy storage and retrieval of objects,
 * with additional safety features such as automatic null-checks and class cast exception handling.
 * </p>
 *
 * @implNote This class extends {@link HashMap} and overrides the {@code put} method to include a warning if a key
 *           already exists in the cache. It also includes a custom {@code get} method that performs type checking and
 *           handles potential {@link ClassCastException}s.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public class Cache<K, V> extends HashMap<K, V> {

    /**
     * Associates the specified value with the specified key in this cache. If the cache previously contained a
     * mapping for the key, the old value is replaced by the specified value.
     * <p>
     * If the key already exists in the cache, a warning is logged.
     * </p>
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with {@code key}, or
     *         {@code null} if there was no mapping for {@code key}.
     *         (A {@code null} return can also indicate that the map
     *         previously associated {@code null} with {@code key}.)
     */
    @Override
    public V put(K key, V value) {
        return put(key, value, false);
    }

    /**
     * Associates the specified value with the specified key in this cache, with an option to suppress warnings.
     * <p>
     * If the cache previously contained a mapping for the key, the old value is replaced by the specified value.
     * If the key already exists in the cache, a warning is logged unless the {@code suppressOverrideWarning} parameter is set to {@code true}.
     * </p>
     *
     * @param key            key with which the specified value is to be associated
     * @param value          value to be associated with the specified key
     * @param suppressOverrideWarning if {@code true}, suppresses the warning when a key already exists in the cache
     * @return the previous value associated with {@code key}, or
     *         {@code null} if there was no mapping for {@code key}.
     *         (A {@code null} return can also indicate that the map
     *         previously associated {@code null} with {@code key}.)
     */
    public V put(K key, V value, boolean suppressOverrideWarning) {
        if (containsKey(key)) {
            if (!suppressOverrideWarning) {
                LEDSuite.logger.warn("Cache already contains object for key: '" + key + "'!");
            }
        }
        return super.put(key, value);
    }

    /**
     * Retrieves the value associated with the specified key and casts it to the desired type.
     * <p>
     * If the key does not exist or if the value cannot be cast to the desired type, {@code null} is returned,
     * and a warning is logged in case of a {@link ClassCastException}.
     * </p>
     *
     * @param <T>          the type to which the value should be cast
     * @param desiredType  the {@link Class} object representing the desired type
     * @param key          the key whose associated value is to be returned
     * @return the value mapped to the specified key cast to the desired type, or {@code null} if the key does not exist
     *         or if the cast fails
     */
    public <T> T get(@NonNull Class<T> desiredType, @NonNull String key) {
        if (!containsKey(key)) {
            return null;
        }
        try {
            return desiredType.cast(super.get(key));
        } catch (ClassCastException e) {
            LEDSuite.logger.warn("Failed to get cache for Key: '" + key + "' and type: '" + desiredType.getName() + "'!");
            return null;
        }
    }
}
