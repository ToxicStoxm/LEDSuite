package com.toxicstoxm.LEDSuite.auto_registration;

import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotated classes will be automatically registered to the specified module using {@link Registrable#autoRegister()}.
 * @implNote The {@link Registrable#autoRegister()} method will automatically find all annotated classes
 * and register them to their specific module class.
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface AutoRegister {
    AutoRegisterModules module();
}
