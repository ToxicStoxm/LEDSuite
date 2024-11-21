package com.toxicstoxm.LEDSuite.ui;

/**
 * Wrapper interface for API endpoints of {@link UploadPage}.
 * @since 1.0.0
 */
public interface UploadPageEndpoint {
    void setServerConnected(boolean connected);
    void setUploadStatistics(UploadStatistics uploadStatistics);
    void setUploadButtonActive(boolean active);
    void uploadCompleted(boolean success);
}
