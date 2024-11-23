package com.toxicstoxm.LEDSuite.ui.dialogs.status_dialog;

/**
 * Interface that defines the API for updating the status dialog.
 * <p>This interface is intended to be implemented by classes, such as {@link StatusDialog}, that need
 * to handle status updates and modify the dialog content accordingly. The {@code update} method
 * allows for updating the dialog with new status information, which can affect various elements like
 * voltage, current, file state, etc.</p>
 *
 * @since 1.0.0
 */
public interface StatusDialogEndpoint {

    /**
     * Updates the status dialog with the provided status information.
     *
     * @param statusUpdate the new status information to be displayed in the dialog
     */
    void update(StatusUpdate statusUpdate);
}
