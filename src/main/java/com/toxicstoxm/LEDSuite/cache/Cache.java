package com.toxicstoxm.LEDSuite.cache;

import com.toxicstoxm.LEDSuite.LEDSuite;
import lombok.NonNull;


import java.util.HashMap;

public class Cache<T> extends HashMap<String, T> {
    public <U> U get(@NonNull Class<U> desiredType, @NonNull String key) {
        if (!containsKey(key)) return null;
        try {
            return desiredType.cast(super.get(key));
        } catch (ClassCastException e) {
            LEDSuite.logger.warn("Failed to get cache for Key: '" + key + "' and type: '" + desiredType.getName() + "'!");
            return null;
        }
    }
}
