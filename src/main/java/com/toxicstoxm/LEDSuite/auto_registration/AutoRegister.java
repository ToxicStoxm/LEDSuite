package com.toxicstoxm.LEDSuite.auto_registration;

import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for marking classes to be automatically registered to a specific module.
 * Classes annotated with {@code @AutoRegister} will be discovered and registered
 * to the specified module during runtime using the {@link Registrable#autoRegister()} method.
 *
 * @implNote The {@link Registrable#autoRegister()} method scans for all classes annotated with
 * {@code @AutoRegister} and registers them to their corresponding {@link AutoRegisterModules}.
 *
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoRegister {
    /**
     * Specifies the module to which the annotated class belongs.
     *
     * @return the target module for auto-registration, defined in {@link AutoRegisterModules}.
     */
    AutoRegisterModules module();
}
