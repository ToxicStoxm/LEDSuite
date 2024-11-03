package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.ui.dialogs.UpdateCallback;

public interface UploadPageEndpoint {

    UpdateCallback<Boolean> connectivityUpdater();
    UpdateCallback<UploadStatistics> uploadStatisticsUpdater();
}
