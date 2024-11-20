package com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog;

import com.toxicstoxm.LEDSuite.time.Action;
import com.toxicstoxm.LEDSuite.ui.dialogs.ProviderCallback;
import com.toxicstoxm.LEDSuite.ui.dialogs.UpdateCallback;

/**
 * Wrapper interface for API endpoints of {@link SettingsDialog}.
 * @since 1.0.0
 */
public interface SettingsDialogEndpoint {
    ConnectivityStatus connectivityManager();
    AuthStatus authManager();
    ProviderCallback<SettingsData> settingsManager();
    UpdateCallback<SettingsUpdate> updater();
    Action applyButtonCooldown();
}
