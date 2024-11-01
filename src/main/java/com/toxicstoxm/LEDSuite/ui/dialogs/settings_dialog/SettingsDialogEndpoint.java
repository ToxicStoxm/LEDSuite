package com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog;

import com.toxicstoxm.LEDSuite.time.Action;
import com.toxicstoxm.LEDSuite.ui.dialogs.ProviderCallback;
import com.toxicstoxm.LEDSuite.ui.dialogs.UpdateCallback;

public interface SettingsDialogEndpoint {

    ConnectivityStatus connectivityManager();
    ProviderCallback<SettingsData> settingsManager();
    UpdateCallback<SettingsUpdate> updater();
    Action applyButtonCooldown();

}
