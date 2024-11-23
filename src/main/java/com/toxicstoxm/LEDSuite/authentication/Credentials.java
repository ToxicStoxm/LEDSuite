package com.toxicstoxm.LEDSuite.authentication;

import lombok.Builder;
import org.jetbrains.annotations.NotNull;

/**
 * Represents user credentials used for authentication.
 * This record encapsulates a username and a hashed password, ensuring immutability and simplicity.
 *
 * @param username     the username of the user attempting to authenticate. Cannot be {@code null}.
 * @param passwordHash the hashed password for authentication. Cannot be {@code null}.
 *
 * @since 1.0.0
 */
@Builder
public record Credentials(
        @NotNull String username,
        @NotNull String passwordHash
) {}
