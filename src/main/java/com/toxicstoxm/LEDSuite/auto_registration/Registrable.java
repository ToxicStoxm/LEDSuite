package com.toxicstoxm.LEDSuite.auto_registration;

import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModule;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import com.toxicstoxm.LEDSuite.formatting.StringFormatter;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * This class provides a way to dynamically allow registering specific items to specific module classes.<br>
 * @implNote Children of this class must provide {@link #autoRegisterModule()}. <br>
 * {@link #autoRegister()} will now search the provided classpath using reflection {@link ClassGraph}. <br>
 * It will automatically detect all classes annotated with the {@link AutoRegister} annotation
 * and check if their specified {@link AutoRegister#module()} matches the one specified by the child class.
 * If true, that class will be added to the {@link #registeredItems} map, and is accessible from the child class by calling {@link #get(String)}.
 * @param <T> the module-type, derivative of {@link AutoRegistrableItem}. E.g. {@link Packet}
 * @since 1.0.0
 */
public abstract class Registrable<T extends AutoRegistrableItem> {

    private final HashMap<String, T> registeredItems = new HashMap<>();

    /**
     * Relay function for {@link HashMap#get(Object)}
     * @param type key
     * @return value associated with the specified key, or {@code null} if the key is not present or associated with null
     */
    public T get(String type) {
        return registeredItems.get(type);
    }

    /**
     * Relay function for {@link HashMap#containsKey(Object)}
     * @param type key
     * @return {@code true} if {@link #registeredItems} contains this key, otherwise {@code false}
     */
    public boolean isRegistered(String type) {
        return registeredItems.containsKey(type);
    }

    /**
     * Module data specified by the child class.
     * @return the module data
     * @see AutoRegisterModule
     */
    protected abstract AutoRegisterModule<T> autoRegisterModule();

    public boolean registerItem(T item) {
        return registeredItems.putIfAbsent(item.getItemType(), item) == null;
    }

    /**
     * Searches through the classpath provided by the child class.
     * Registers all classes annotated with {@link AutoRegister} and the same {@link AutoRegister#module()}
     * as the one specified by the child class in {@link #autoRegisterModule()} to {@link #registeredItems}.
     */
    public void autoRegister() {
        // Module data specified by the child class
        AutoRegisterModule<T> module = autoRegisterModule();

        Class<T> moduleType = module.getModuleType();
        AutoRegisterModules moduleName = module.getModule();
        String moduleClassPath = module.getClassPath();

        String moduleTypeName = StringFormatter.getClassName(moduleType);

        Set<Class<T>> annotatedClasses = new HashSet<>();

        // Scan the specified package for classes with @AutoRegister annotation
        try (ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()
                .acceptPackages(moduleClassPath)
                .scan()) {

            for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(AutoRegister.class.getName())) {
                try {
                    // Load class annotated with @AutoRegister
                    Class<?> loadedClass = classInfo.loadClass();

                    // Only register if the class has the correct module name and type
                    AutoRegister annotation = loadedClass.getAnnotation(AutoRegister.class);
                    if (annotation != null && annotation.module().equals(moduleName)) {
                        if (moduleType.isAssignableFrom(loadedClass)) {
                            // Casting the loaded class to type class
                            // Suppressing the unchecked warning because compatability is checked with is assignable from above
                            @SuppressWarnings("unchecked")
                            Class<T> typeClass = (Class<T>) loadedClass;
                            annotatedClasses.add(typeClass);
                        } else {
                            LEDSuiteApplication.getLogger().error("Failed to load class: " + classInfo.getName()
                                    + ". Class doesn't implement " + moduleTypeName + " interface!", new LEDSuiteLogAreas.COMMUNICATION());
                        }
                    }
                } catch (Exception e) {
                    LEDSuiteApplication.getLogger().error("Failed to load class: '" + classInfo.getName() + "' to module '" + moduleName + "'",
                            new LEDSuiteLogAreas.COMMUNICATION());
                    LEDSuiteApplication.getLogger().error(e.getMessage(), new LEDSuiteLogAreas.COMMUNICATION());
                    throw new RuntimeException(e);
                }
            }
        }

        // Loop through all previously loaded classes
        for (Class<T> itemClass : annotatedClasses) {
            try {
                // Use reflection to bypass access check
                Constructor<T> constructor = itemClass.getDeclaredConstructor();
                constructor.setAccessible(true); // Enable access to non-public constructors
                T item = constructor.newInstance(); // Create a new instance of the loaded class
                String id = item.getItemType(); // Retrieve the item's id for logging

                // Register the new item instance and log the results
                if (registerItem(item)) {
                    LEDSuiteApplication.getLogger().info("Successfully Registered " + moduleTypeName + ": " + id,
                            new LEDSuiteLogAreas.COMMUNICATION());
                } else {
                    LEDSuiteApplication.getLogger().debug("Item " + id + " is already registered. Skipping it.",
                            new LEDSuiteLogAreas.COMMUNICATION());
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                LEDSuiteApplication.getLogger().error("Failed to auto-register " + moduleTypeName + ": " + itemClass.getName(),
                        new LEDSuiteLogAreas.COMMUNICATION());
                LEDSuiteApplication.getLogger().error(e.getMessage(), new LEDSuiteLogAreas.COMMUNICATION());
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Unregisters a singular item, associated with the specified type.
     * @param type type associated with an item
     * @return {@code true}, if the item was registered and was successfully unregistered, otherwise {@code false}
     */
    public boolean unregisterPacket(String type) {
        return registeredItems.remove(type) != null;
    }

    /**
     * Unregisters all registered items.
     */
    public void clearPackets() {
        registeredItems.clear();
    }
}