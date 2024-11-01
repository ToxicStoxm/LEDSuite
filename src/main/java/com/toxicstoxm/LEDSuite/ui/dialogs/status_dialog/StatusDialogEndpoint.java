package com.toxicstoxm.LEDSuite.ui.dialogs.status_dialog;

import com.toxicstoxm.LEDSuite.ui.dialogs.UpdateCallback;

public interface StatusDialogEndpoint {
    UpdateCallback<StatusUpdate> updater();
}
