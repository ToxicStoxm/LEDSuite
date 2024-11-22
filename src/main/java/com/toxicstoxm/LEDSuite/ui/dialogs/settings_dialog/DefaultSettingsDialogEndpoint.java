package com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog;

import org.jetbrains.annotations.NotNull;

public abstract class DefaultSettingsDialogEndpoint implements SettingsDialogEndpoint {
    @Override
    public void update(SettingsUpdate settingsUpdate) {

    }

    @Override
    public void applyButtonCooldown() {

    }

    @Override
    public SettingsData getData() {
        return new SettingsData(null, null, null);
    }

    @Override
    public void setAuthenticating() {

    }

    @Override
    public void setAuthenticated(boolean authenticated) {

    }

    @Override
    public void setServerState(@NotNull ServerState serverState) {

    }

    @Override
    public void setAuthenticated(boolean authenticated, String username) {

    }
}
