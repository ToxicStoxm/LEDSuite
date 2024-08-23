package com.toxicstoxm.LEDSuite.settings.config;

public interface Setting<T> {

    T get();
    void set(T value);

    default void set(T value, boolean shouldSave) {
        throw new UnsupportedOperationException("Conditional saving is not supported by default! Override this method if you want to implement it!");
    }

    Class<?> getType();

    default boolean isType(Object type) {
        return getType().isInstance(type);
    }

    String toString();

    default String getIdentifier(boolean withVarType) {
        return toString();
    }

    boolean equals(Object obj);
}
