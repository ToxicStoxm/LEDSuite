package com.toxicstoxm.LEDSuite.settings.config;



import com.toxicstoxm.LEDSuite.settings.yaml.file.YamlConfiguration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unchecked")
public class SettingsHandler<T> {

    public void loadSettings(Class<? extends SettingsBundle> settingsBundle, SettingsAccessor accessor)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (!List.of(settingsBundle.getInterfaces()).contains(SettingsBundle.class)) {
            throw new IllegalArgumentException(settingsBundle.getName() + " must implement SettingsBundle interface!");
        }
        for (Class<?> innerClass : settingsBundle.getDeclaredClasses()) {
            if (innerClass.isAnnotationPresent(YAMLSetting.class)) {
                String path = innerClass.getAnnotation(YAMLSetting.class).path();
                Constructor<?> constructor = innerClass.getConstructor(com.toxicstoxm.LEDSuite.settings.config.Setting.class);
                T setting = (T) constructor.newInstance(accessor.get(path));
                System.out.println(setting);
            }
        }
    }

    public void saveSettings(Class<? extends SettingsBundle> settingsBundle, YamlConfiguration yaml)
            throws InvocationTargetException, IllegalAccessException {
        if (!List.of(settingsBundle.getInterfaces()).contains(SettingsBundle.class)) {
            throw new IllegalArgumentException(settingsBundle.getName() + " must implement SettingsBundle interface!");
        }
        for (Class<?> innerClass : settingsBundle.getDeclaredClasses()) {
            if (innerClass.isAnnotationPresent(YAMLSetting.class)) {
                String path = innerClass.getAnnotation(YAMLSetting.class).path();
                Optional<Method> getterOpt = findGetter(innerClass);
                if (getterOpt.isPresent()) {
                    Method getter = getterOpt.get();
                    try {
                        T value = (T) getter.invoke(null); // Assuming static getter methods
                        yaml.set(path, value);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException("Error invoking method: " + getter.getName(), e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Illegal access to method: " + getter.getName(), e);
                    }
                } else {
                    throw new NullPointerException("Getter method not found in " + innerClass.getName());
                }
            }
        }
    }

    private Optional<Method> findGetter(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Getter.class) && method.getParameterCount() == 0) {
                return Optional.of(method);
            }
        }
        return Optional.empty();
    }
}
