package com.toxicstoxm.LEDSuite.ui.dialogs.status_dialog;

import com.toxicstoxm.LEDSuite.ui.dialogs.UpdateCallback;

/**
 * Wrapper interface for API endpoints of {@link StatusDialog}.
 * @since 1.0.0
 */
public interface StatusDialogEndpoint {
    void update(StatusUpdate statusUpdate);
}
