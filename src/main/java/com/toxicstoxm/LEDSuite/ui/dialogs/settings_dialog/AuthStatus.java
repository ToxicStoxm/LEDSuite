package com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog;

/**
 * Interface for managing and updating the authentication status.
 * <p>
 * This interface defines methods for updating the authentication status of a user.
 * It provides callbacks for transitioning between states such as authenticating and authenticated.
 * Implementing classes can use these methods to update the UI or other components that depend on authentication state.
 * </p>
 * @since 1.0.0
 */
public interface AuthStatus {
    /**
     * Indicates that the authentication process is ongoing.
     * <p>
     * This method should be called when the authentication process is in progress.
     * It can be used to update the UI to show a loading spinner or other indicators of activity.
     * </p>
     */
    void setAuthenticating();

    /**
     * Sets the authentication status to authenticated or unauthenticated.
     * <p>
     * This method should be called when the authentication process has completed.
     * If the user is authenticated, the method should be called with a `true` value,
     * and the username can be provided. If unauthenticated, `false` should be passed.
     * </p>
     *
     * @param authenticated a boolean indicating whether the user is authenticated
     * @param username the username of the authenticated user, or null if unauthenticated
     */
    void setAuthenticated(boolean authenticated, String username);

    /**
     * Sets the authentication status to authenticated or unauthenticated without providing a username.
     * <p>
     * This is a default method that allows for a simplified version of the
     * {@link #setAuthenticated(boolean, String)} method where no username is provided.
     * </p>
     *
     * @param authenticated a boolean indicating whether the user is authenticated
     */
    default void setAuthenticated(boolean authenticated) {
        setAuthenticated(authenticated, null);
    }
}
