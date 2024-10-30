package com.toxicstoxm.LEDSuite.auto_registration.modules;

import com.toxicstoxm.LEDSuite.auto_registration.Registrable;
import lombok.Builder;
import lombok.Getter;

/**
 * Wrapper class for auto register module information data.
 * @param <T> module type
 * @since 1.0.0
 */
@Builder
@Getter
public class AutoRegisterModule<T> {

    private Class<T> moduleType;
    private String classPath;
    private AutoRegisterModules module;

    /**
     * Constructs a new module with the specified information data parameters.
     * This is used by {@link Registrable}.
     * @param moduleType module data type
     * @param classPath classpath of this module
     * @param module the module name, see {@link AutoRegisterModules}
     */
    public AutoRegisterModule(Class<T> moduleType, String classPath, AutoRegisterModules module) {
        this.moduleType = moduleType;
        this.classPath = classPath;
        this.module = module;
    }
}
