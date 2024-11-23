package com.toxicstoxm.LEDSuite.ui;

/**
 * An abstract base class providing default (empty) implementations of the methods from the {@link UploadPageEndpoint} interface.
 * <p>
 * This class is intended to be extended by concrete implementations of an upload page, allowing the user to override only the necessary methods
 * for customizing behavior. By default, each method does nothing. Subclasses can override these methods to provide the desired functionality.
 * </p>
 *
 * <p>Common methods in this class include handling server connection status, updating upload statistics, controlling the upload button state,
 * and notifying the page when an upload has been completed.</p>
 *
 * @since 1.0.0
 */
public abstract class DefaultUploadPageEndpoint implements UploadPageEndpoint {

    /**
     * Default implementation for setting the server connection status.
     * <p>
     * This method is called to update the connection status of the server (e.g., whether it is connected or disconnected).
     * The default implementation does nothing. Subclasses should override this method to handle connection state updates.
     * </p>
     *
     * @param connected {@code true} if the server is connected, {@code false} otherwise.
     */
    @Override
    public void setServerConnected(boolean connected) {
        // Default implementation does nothing.
    }

    /**
     * Default implementation for setting upload statistics.
     * <p>
     * This method is called to update the upload statistics (e.g., upload progress, number of files uploaded, etc.).
     * The default implementation does nothing. Subclasses should override this method to handle statistics updates.
     * </p>
     *
     * @param uploadStatistics The {@link UploadStatistics} object containing statistics about the upload process.
     */
    @Override
    public void setUploadStatistics(UploadStatistics uploadStatistics) {
        // Default implementation does nothing.
    }

    /**
     * Default implementation for setting the upload button's active state.
     * <p>
     * This method is called to enable or disable the upload button based on the state of the upload process.
     * The default implementation does nothing. Subclasses should override this method to modify the button's state.
     * </p>
     *
     * @param active {@code true} to activate the upload button, {@code false} to deactivate it.
     */
    @Override
    public void setUploadButtonActive(boolean active) {
        // Default implementation does nothing.
    }

    /**
     * Default implementation for handling upload completion.
     * <p>
     * This method is called when an upload process is completed, either successfully or unsuccessfully.
     * The default implementation does nothing. Subclasses should override this method to provide custom behavior when the upload completes.
     * </p>
     *
     * @param success {@code true} if the upload was completed successfully, {@code false} otherwise.
     */
    @Override
    public void uploadCompleted(boolean success) {
        // Default implementation does nothing.
    }
}
