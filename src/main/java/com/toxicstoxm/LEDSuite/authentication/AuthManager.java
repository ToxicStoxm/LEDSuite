package com.toxicstoxm.LEDSuite.authentication;

import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.AuthenticationRequestPacket;
import com.toxicstoxm.LEDSuite.time.Action;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.LEDSuite.ui.dialogs.UpdateCallback;
import com.toxicstoxm.YAJL.Logger;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * Handles user authentication by managing authentication requests and responses.
 * This class tracks pending authentication requests and updates the authentication
 * state based on server responses.
 *
 * @since 1.0.0
 */
@Getter
public class AuthManager implements AuthManagerEndpoint {
    private static final Logger logger = Logger.autoConfigureLogger();

    /**
     * A map storing pending authentication requests.
     * The key is the username, and the value is the callback invoked when the server responds.
     */
    private final HashMap<String, UpdateCallback<Boolean>> awaitingResponse = new HashMap<>();

    /**
     * The username of the currently authenticated user.
     * If no user is authenticated, this value is {@code null}.
     */
    private String username;

    /**
     * Initiates an authentication request for the provided credentials.
     * The method clears any existing authentication state and sends a request to the server.
     * Once the server responds, the specified callback is invoked.
     *
     * @param credentials the {@link Credentials} object containing the username and password hash.
     * @param finishCb    an {@link Action} to execute once the authentication attempt completes.
     *                    This action typically updates the UI or other application state.
     */
    @Override
    public void requestAuth(@NotNull Credentials credentials, Action finishCb) {
        if (username != null) {
            logger.debug(" > Clearing existing authentication state for '{}'", username);
            username = null;
        }

        String newUsername = credentials.username();
        logger.debug(" > Requesting authentication for '{}'", newUsername);

        awaitingResponse.put(newUsername, result -> {
            if (result) {
                logger.info(" > Authentication successful for '{}'", newUsername);
                username = newUsername;
            } else {
                logger.warn(" > Authentication failed for '{}'", newUsername);
            }

            LEDSuiteApplication.getWindow().setAuthenticated(result, username);
            finishCb.run();
        });

        LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                AuthenticationRequestPacket.builder()
                        .username(newUsername)
                        .passwordHash(credentials.passwordHash())
                        .build().serialize()
        );
        logger.verbose(" > Sent authentication request for '{}'", newUsername);
    }

    /**
     * Processes the result of an authentication request from the server.
     * Updates the authentication state and invokes the associated callback.
     *
     * @param username the username associated with the authentication attempt. Can be {@code null}.
     * @param result   {@code true} if the authentication was successful, otherwise {@code false}.
     */
    @Override
    public void authResult(String username, boolean result) {
        if ((username == null || username.isBlank()) && !result) {
            logger.warn(" > Received failed authentication result with no username");
            LEDSuiteApplication.getWindow().setAuthenticated(false);
            return;
        }

        if (awaitingResponse.containsKey(username)) {
            logger.debug(" > Received authentication result for '{}': {}", username, result ? "success" : "failure");
            awaitingResponse.remove(username).update(result);
        } else {
            logger.warn(" > No pending authentication request found for '{}'", username);
        }
    }
}
