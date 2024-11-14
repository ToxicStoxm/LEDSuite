package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.ui.dialogs.UpdateCallback;

/**
 * Wrapper interface for API endpoints of {@link UploadPage}.
 * @since 1.0.0
 */
public interface UploadPageEndpoint {
    UpdateCallback<Boolean> connectivityUpdater();
    UpdateCallback<UploadStatistics> uploadStatisticsUpdater();
    UpdateCallback<Boolean> uploadButtonState();
    UpdateCallback<Boolean> uploadSuccessCallback();
}
