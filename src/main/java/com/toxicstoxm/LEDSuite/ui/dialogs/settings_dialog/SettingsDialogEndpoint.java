package com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog;

import org.jetbrains.annotations.NotNull;

/**
 * Wrapper interface for API endpoints of {@link SettingsDialog}.
 * <br>This interface defines methods for interacting with a SettingsDialog's API,
 * such as updating settings, managing button cooldowns, retrieving data, and setting the server state.
 * It extends {@link AuthStatus} to handle authentication-related operations as well.
 *
 * @since 1.0.0
 */
public interface SettingsDialogEndpoint extends AuthStatus {

    /**
     * Updates the settings dialog with the latest settings data.
     * This method is called to refresh the dialog UI with new settings from the backend or user input.
     *
     * @param settingsUpdate An object containing the new settings to apply to the dialog.
     */
    void update(SettingsUpdate settingsUpdate);

    /**
     * Applies the cooldown to the apply button, ensuring that it cannot be clicked repeatedly in a short time.
     * This is useful for preventing accidental multiple submissions or requests.
     */
    void applyButtonCooldown();

    /**
     * Retrieves the current settings data from the dialog.
     * This can be used to get a snapshot of the settings the user has configured, possibly for saving or sending to a server.
     *
     * @return The settings data currently held by the dialog.
     */
    SettingsData getData();

    /**
     * Sets the current state of the server in the settings dialog.
     * This method adjusts the UI and functionality based on the server's connection state (e.g., connected, disconnected).
     *
     * @param serverState The server state to apply (e.g., CONNECTED, DISCONNECTED).
     */
    void setServerState(@NotNull ServerState serverState);
}
