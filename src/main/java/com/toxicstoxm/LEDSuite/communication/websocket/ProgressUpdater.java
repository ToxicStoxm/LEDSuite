package com.toxicstoxm.LEDSuite.communication.websocket;

import com.toxicstoxm.LEDSuite.ui.dialogs.UpdateCallback;

/**
 * Wrapper interface for a {@link UpdateCallback} with type {@link ProgressUpdate}
 * @since 1.0.0
 */
public interface ProgressUpdater extends UpdateCallback<ProgressUpdate> {
    void onConnect(String sessionID);
}
