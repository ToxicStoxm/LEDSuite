package com.toxicstoxm.LEDSuite.ui.dialogs.status_dialog;

/**
 * Default implementation of the {@link StatusDialogEndpoint} interface.
 * <p>
 * This class provides a default (empty) implementation of the {@link StatusDialogEndpoint} interface,
 * which can be extended or overridden as needed. By default, the `update` method is not performing any actions.
 * This class serves as a base for more specific implementations.
 * </p>
 * @since 1.0.0
 */
public class DefaultStatusDialogEndpoint implements StatusDialogEndpoint {

    /**
     * Default implementation of the {@link StatusDialogEndpoint#update(StatusUpdate)} method.
     * <p>
     * This method is meant to be overridden in subclasses to handle updates to the status dialog.
     * In this default implementation, the method does nothing.
     * </p>
     *
     * @param statusUpdate The status update object containing the latest status information to update the dialog.
     * @since 1.0.0
     */
    @Override
    public void update(StatusUpdate statusUpdate) {
        // Default empty implementation
    }
}
