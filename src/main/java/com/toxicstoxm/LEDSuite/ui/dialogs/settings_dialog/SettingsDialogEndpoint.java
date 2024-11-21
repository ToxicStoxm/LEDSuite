package com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog;

import com.toxicstoxm.LEDSuite.ui.dialogs.ProviderCallback;
import org.jetbrains.annotations.NotNull;

/**
 * Wrapper interface for API endpoints of {@link SettingsDialog}.
 * @since 1.0.0
 */
public interface SettingsDialogEndpoint extends AuthStatus {
    void update(SettingsUpdate settingsUpdate);
    void applyButtonCooldown();
    SettingsData getData();
    void setServerState(@NotNull ServerState serverState);
}
