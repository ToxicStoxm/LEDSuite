package com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog;

import org.jetbrains.annotations.NotNull;

/**
 * Default implementation of the {@link SettingsDialogEndpoint} interface.
 * <p>
 * This class provides default (empty) behavior for all methods in the
 * {@link SettingsDialogEndpoint} interface. It is intended to be extended
 * by classes that need to implement only the relevant methods, without
 * the need to define all methods from the interface.
 * </p>
 *
 * @since 1.0.0
 */
public abstract class DefaultSettingsDialogEndpoint implements SettingsDialogEndpoint {

    /**
     * Default implementation for updating settings. Does nothing by default.
     * Subclasses can override this method to handle specific update logic.
     *
     * @param settingsUpdate the settings update to be applied
     */
    @Override
    public void update(SettingsUpdate settingsUpdate) {
        // Default no-op implementation
    }

    /**
     * Default implementation for handling apply button cooldown. Does nothing by default.
     * Subclasses can override this method to implement button cooldown logic.
     */
    @Override
    public void applyButtonCooldown() {
        // Default no-op implementation
    }

    /**
     * Returns the default settings data. This implementation returns an empty
     * {@link SettingsData} object with all fields set to null.
     *
     * @return a new {@link SettingsData} object
     */
    @Override
    public SettingsData getData() {
        return new SettingsData(null, null, null);
    }

    /**
     * Default implementation for setting the authentication status to "authenticating".
     * Does nothing by default. Subclasses can override to handle the authenticating state.
     */
    @Override
    public void setAuthenticating() {
        // Default no-op implementation
    }

    /**
     * Default implementation for setting the authenticated status.
     * Does nothing by default. Subclasses can override to handle the authenticated state.
     *
     * @param authenticated true if authenticated, false otherwise
     */
    @Override
    public void setAuthenticated(boolean authenticated) {
        // Default no-op implementation
    }

    /**
     * Default implementation for setting the authenticated status, including the username.
     * Does nothing by default. Subclasses can override to handle the authenticated state with username.
     *
     * @param authenticated true if authenticated, false otherwise
     * @param username the authenticated username (can be null)
     */
    @Override
    public void setAuthenticated(boolean authenticated, String username) {
        // Default no-op implementation
    }

    /**
     * Default implementation for setting the server state.
     * Does nothing by default. Subclasses can override this method to handle the server state.
     *
     * @param serverState the state of the server
     */
    @Override
    public void setServerState(@NotNull ServerState serverState) {
        // Default no-op implementation
    }
}
