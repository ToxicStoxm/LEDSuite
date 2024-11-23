package com.toxicstoxm.LEDSuite.authentication;

import com.toxicstoxm.LEDSuite.time.Action;

/**
 * Wrapper interface for API endpoints of {@link AuthManager}.
 * Provides methods for initiating authentication requests, retrieving authentication state,
 * and handling authentication responses.
 *
 * @since 1.0.0
 */
public interface AuthManagerEndpoint {

    /**
     * Initiates an authentication request using the provided credentials.
     * The specified callback will be invoked once the server processes the request.
     *
     * @param credentials the {@link Credentials} object containing the username and password hash
     *                    for the authentication attempt.
     * @param finishCb    an {@link Action} that is executed when the server responds to the request.
     *                    This callback is used to process the authentication result or handle errors.
     */
    void requestAuth(Credentials credentials, Action finishCb);

    /**
     * Retrieves the username of the currently authenticated user.
     * If no user is authenticated, this method returns {@code null}.
     *
     * @return a {@link String} representing the username of the authenticated user,
     *         or {@code null} if no user is authenticated.
     */
    String getUsername();

    /**
     * Processes the result of an authentication request received from the server.
     * This method is typically invoked internally after the server responds to an
     * authentication request.
     *
     * @param username the username associated with the authentication request.
     *                 This is the username provided in the original {@link Credentials}.
     * @param result   a {@code boolean} indicating the success or failure of the authentication
     *                 attempt.
     *                 if authentication was successful; otherwise {@code false}.
     */
    void authResult(String username, boolean result);
}
