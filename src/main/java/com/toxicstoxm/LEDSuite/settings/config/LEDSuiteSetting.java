package com.toxicstoxm.LEDSuite.settings.config;


public class LEDSuiteSetting<T> implements Setting<T> {
    private T value;
    private boolean shouldSave = false;

    public LEDSuiteSetting(T value) {
        this.value = value;
    }

    public LEDSuiteSetting(Setting<Object> setting, Class<T> clazz) {
        if (!setting.isType(setting.get())) {
            throw new IllegalArgumentException("Type mismatch");
        }
        this.value = clazz.cast(setting.get());
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public void set(T value) {
        this.value = value;
    }

    @Override
    public void set(T value, boolean shouldSave) {
        this.value = value;
        this.shouldSave = shouldSave;
    }

    @Override
    public Class<?> getType() {
        return value.getClass();
    }

    @Override
    public String toString() {
        return "LEDSuiteSetting: [ Name: " + getClass().getName() + " Type: " + getType().getName() + " Value: " + value.toString() + " ]";
    }

    @Override
    public boolean equals(Object obj) {
        return isType(obj) && getClass().isInstance(obj);
    }
}
