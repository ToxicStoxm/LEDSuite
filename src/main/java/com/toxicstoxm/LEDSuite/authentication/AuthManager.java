package com.toxicstoxm.LEDSuite.authentication;

import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.AuthenticationRequestPacket;
import com.toxicstoxm.LEDSuite.time.Action;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.LEDSuite.ui.dialogs.UpdateCallback;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

/**
 * Handles user authentication by managing authentication requests and responses.
 * This class tracks pending authentication requests and updates the authentication
 * state based on server responses.
 *
 * @since 1.0.0
 */
@Getter
public class AuthManager implements AuthManagerEndpoint {

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

    private List<Permissions> userPermissions;

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
        // Clear existing authentication state
        if (username != null) {
            username = null;
        }

        String newUsername = credentials.username();

        // Register a callback to handle the server response
        awaitingResponse.put(newUsername, result -> {
            if (result) {
                username = newUsername;
            }
            LEDSuiteApplication.getWindow().setAuthenticated(false, username);
        });

        // Send the authentication request packet to the server
        LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                AuthenticationRequestPacket.builder()
                        .username(newUsername)
                        .passwordHash(credentials.passwordHash())
                        .build().serialize()
        );
    }

    /**
     * Processes the result of an authentication request from the server.
     * Updates the authentication state and invokes the associated callback.
     *
     * @param username the username associated with the authentication attempt. Can be {@code null}.
     */
    @Override
    public void authResult(@NotNull String username, List<Permissions> userPermissions) {
        if (userPermissions == null) {
            awaitingResponse.clear();
            if (!username.isEmpty()) {
                LEDSuiteApplication.getWindow().setAuthenticated(true);
            } else {
                this.username = username;
                LEDSuiteApplication.getWindow().setAuthenticated(true, username);
            }
        } else {
            if (userPermissions.isEmpty() && username.isBlank()) {
                if (awaitingResponse.containsKey(username)) {
                    // Remove the pending request and invoke the callback with the result
                    awaitingResponse.remove(username).update(false);
                }
            } else {
                if (awaitingResponse.containsKey(username)) {
                    // Remove the pending request and invoke the callback with the result
                    awaitingResponse.remove(username).update(true);
                }
                this.userPermissions = userPermissions;
            }
        }
    }

    public void fullPermissionAuth() {
        awaitingResponse.clear();
        LEDSuiteApplication.getWindow().setAuthenticated(true);
    }
}
