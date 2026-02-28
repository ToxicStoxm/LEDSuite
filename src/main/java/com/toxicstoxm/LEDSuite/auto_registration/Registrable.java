package com.toxicstoxm.LEDSuite.auto_registration;

import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModule;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import com.toxicstoxm.LEDSuite.formatting.StringFormatter;
import com.toxicstoxm.YAJL.core.Logger;
import com.toxicstoxm.YAJL.core.LoggerManager;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides a mechanism to dynamically register items to specific modules at runtime.
 * <br>
 * Subclasses must define the module information by overriding {@link #autoRegisterModule()}.
 * {@link #autoRegister()} will scan the specified classpath for classes annotated with
 * {@link AutoRegister} that match the module and type defined in the subclass.
 * If matched, those classes will be registered and available for access through {@link #get(String)}.
 * <br>
 * @param <T> The type of the module, extending {@link AutoRegistrableItem}. For example, {@link Packet}.
 * @since 1.0.0
 */
public abstract class Registrable<T extends AutoRegistrableItem> {
    private static final Logger logger = LoggerManager.getLogger(Registrable.class);

    private final HashMap<String, T> registeredItems = new HashMap<>();

    /**
     * Retrieves the registered item corresponding to the given key (item type).
     *
     * @param type the key to look up in the registered items map
     * @return the registered item associated with the specified type, or {@code null} if not found
     */
    public T get(String type) {
        return registeredItems.get(type);
    }

    /**
     * Checks if an item of the specified type has been registered.
     *
     * @param type the key to check in the registered items map
     * @return {@code true} if the item is registered, otherwise {@code false}
     */
    public boolean isRegistered(String type) {
        return registeredItems.containsKey(type);
    }

    /**
     * Abstract method to be implemented by subclasses to specify module registration data.
     *
     * @return an {@link AutoRegisterModule} instance describing the module for registration
     */
    protected abstract AutoRegisterModule<T> autoRegisterModule();

    /**
     * Registers a given item to the module.
     *
     * @param item the item to register
     * @return {@code true} if the item was successfully registered, otherwise {@code false} (item already registered)
     */
    public boolean registerItem(T item) {
        return registeredItems.putIfAbsent(item.getItemType(), item) == null;
    }

    /**
     * Automatically registers all classes annotated with {@link AutoRegister} that match the
     * module and type specified by the subclass.
     * The registration process uses reflection to load and instantiate the classes.
     * <br>
     * Classes must implement the module type defined by {@link #autoRegisterModule()}.
     */
    public void autoRegister() {
        AutoRegisterModule<T> module = autoRegisterModule();

        if (module == null) {
            logger.error(" > Tried to auto-register items for an unsupported module!");
            return;
        }

        Class<T> moduleType = module.moduleType();
        AutoRegisterModules moduleName = module.module();
        String moduleClassPath = module.classPath();

        String moduleTypeName = StringFormatter.getClassName(moduleType);
        logger.debug(" > Auto-registering module '{}', type '{}', scanning path '{}'.", moduleName, moduleTypeName, moduleClassPath);

        Set<Class<T>> annotatedClasses = new HashSet<>();

        // Scan the specified package for classes annotated with @AutoRegister
        try (ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()
                .acceptPackages(moduleClassPath)
                .scan()) {

            logger.debug(" > Scanning for classes annotated with @AutoRegister...");

            for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(AutoRegister.class.getName())) {
                try {
                    Class<?> loadedClass = classInfo.loadClass();

                    AutoRegister annotation = loadedClass.getAnnotation(AutoRegister.class);
                    if (annotation != null && annotation.module().equals(moduleName)) {
                        if (moduleType.isAssignableFrom(loadedClass)) {
                            @SuppressWarnings("unchecked")
                            Class<T> typeClass = (Class<T>) loadedClass;
                            annotatedClasses.add(typeClass);
                            logger.debug(" > Found valid auto-registerable class: {}", classInfo.getName());
                        } else {
                            logger.error(" > Class {} does not implement {} interface!", classInfo.getName(), moduleTypeName);
                        }
                    } else {
                        logger.debug(" > Skipping class {} — annotation missing or module mismatch.", classInfo.getName());
                    }
                } catch (Exception e) {
                    logger.error(" > Failed to load class '{}' for module '{}'", classInfo.getName(), moduleName);
                    logger.error(e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        }

        logger.debug(" > Found {} valid class(es) to register for module '{}'.", annotatedClasses.size(), moduleName);

        // Register the valid items found in the previous step
        for (Class<T> itemClass : annotatedClasses) {
            try {
                Constructor<T> constructor = itemClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                T item = constructor.newInstance();

                String id = item.getItemType();
                if (registerItem(item)) {
                    logger.verbose(" > Successfully registered {}: {}", moduleTypeName, id);
                } else {
                    logger.debug(" > Item {} is already registered. Skipping it.", id);
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                logger.error(" > Failed to auto-register {}: {}", moduleTypeName, itemClass.getName());
                logger.error("   Reason: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        }

        logger.debug(" > Finished auto-registering module '{}'.", moduleName);
    }

    /**
     * Unregisters an item by its type (key).
     *
     * @param type the type of the item to unregister
     * @return {@code true} if the item was successfully unregistered, otherwise {@code false}
     */
    public boolean unregisterPacket(String type) {
        boolean removed = registeredItems.remove(type) != null;
        if (removed) {
            logger.debug(" > Unregistered packet: {}", type);
        } else {
            logger.debug(" > Tried to unregister unknown packet: {}", type);
        }
        return removed;
    }

    /**
     * Unregisters all items.
     */
    public void clearPackets() {
        int count = registeredItems.size();
        registeredItems.clear();
        logger.debug(" > Cleared all packets ({} removed).", count);
    }
}
