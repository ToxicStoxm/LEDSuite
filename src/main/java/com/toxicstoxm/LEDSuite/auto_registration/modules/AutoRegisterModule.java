package com.toxicstoxm.LEDSuite.auto_registration.modules;

import lombok.Builder;
import org.jetbrains.annotations.NotNull;

/**
 * Represents metadata for an auto-registrable module.
 * This wrapper record provides type, class path, and module-specific details,
 * and is used for automatic registration mechanisms.
 *
 * @param <T> the type of the module
 * @param moduleType the type of the module being registered
 * @param classPath  the classpath where the module is located
 * @param module     the specific module identifier, see {@link AutoRegisterModules}
 *
 * @since 1.0.0
 */
@Builder
public record AutoRegisterModule<T>(
        @NotNull Class<T> moduleType,
        @NotNull String classPath,
        @NotNull AutoRegisterModules module
) {}
