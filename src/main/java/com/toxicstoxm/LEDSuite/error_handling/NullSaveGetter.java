package com.toxicstoxm.LEDSuite.error_handling;

public interface NullSaveGetter<T> {
    T get();
    boolean isAvailable();
    T getDefault();
}
