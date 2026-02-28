package com.toxicstoxm.LEDSuite.ui;

/**
 * Wrapper interface for API endpoints of {@link UploadPage}.
 * <p>This interface defines methods for updating the state of the UploadPage component. These methods are called
 * by the application logic to modify the UI and provide feedback to the user during the upload process.</p>
 *
 * @since 1.0.0
 */
public interface UploadPageEndpoint {
    /**
     * Sets the server connection status and updates UI accordingly.
     *
     * @param connected {@code true} if the server is connected, {@code false} if disconnected.
     */
    void setServerConnected(boolean connected);

    /**
     * Updates the upload statistics on the page, including speed and ETA.
     *
     * @param uploadStatistics The updated upload statistics.
     */
    void setUploadStatistics(UploadStatistics uploadStatistics);

    /**
     * Activates or deactivates the upload button based on the provided status.
     *
     * @param active {@code true} to activate the button, {@code false} to deactivate it.
     */
    void setUploadButtonUploading(boolean active);

    /**
     * Updates the UI when the upload process is completed.
     *
     * @param success {@code true} if the upload was successful, {@code false} if it failed.
     */
    void uploadCompleted(boolean success);
}
