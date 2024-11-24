package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.status_reply.FileState;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.status_reply.StatusReplyPacket;
import com.toxicstoxm.LEDSuite.ui.animation_menu.AnimationMenu;
import com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog.SettingsDialogEndpoint;
import com.toxicstoxm.LEDSuite.ui.dialogs.status_dialog.StatusDialogEndpoint;
import org.gnome.adw.AlertDialog;
import org.gnome.adw.ApplicationWindow;
import org.gnome.gtk.Widget;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Interface representing the main window of the application.
 * <p>This interface extends various dialog endpoints to provide functionalities
 * for managing the user interface, including dialogs, animations, and upload progress.</p>
 *
 * It offers methods for:
 * <ul>
 *     <li>Displaying and toggling various dialogs and windows (e.g., settings, status, about).</li>
 *     <li>Managing animation lists, including updating, showing spinners, and displaying context menus.</li>
 *     <li>Handling server connectivity and upload progress.</li>
 *     <li>Interacting with the main content area and sidebar.</li>
 * </ul>
 *
 * <p>Implementing classes are responsible for providing concrete behavior for these actions.</p>
 *
 * @since 1.0.0
 */
public interface MainWindow extends StatusDialogEndpoint, SettingsDialogEndpoint, UploadPageEndpoint {

    /**
     * Converts this instance to an {@link ApplicationWindow}.
     *
     * @return The main application window instance.
     */
    ApplicationWindow asApplicationWindow();

    /**
     * Toggles the visibility of the sidebar.
     * <p>This method is used to show or hide the sidebar based on its current state.</p>
     */
    void toggleSidebar();

    /**
     * Displays the About dialog.
     * <p>Shows the application’s information, version, and other related details to the user.</p>
     */
    void displayAboutDialog();

    /**
     * Displays the status dialog.
     * <p>Shows the status window which can contain information about the current operation or connection status.</p>
     */
    void displayStatusDialog();

    /**
     * Displays the preferences/settings dialog.
     * <p>Allows the user to configure application settings and preferences.</p>
     */
    void displayPreferencesDialog();

    /**
     * Displays the keyboard shortcuts window.
     * <p>Shows the list of available keyboard shortcuts for the application.</p>
     */
    void displayShortcutsWindow();

    /**
     * Selects the upload page for interaction.
     * <p>This method activates and shows the upload page where the user can manage file uploads.</p>
     */
    void uploadPageSelect();

    /**
     * Applies changes from the settings dialog.
     * <p>Applies the user settings configured in the preferences dialog.</p>
     */
    void settingsDialogApply();

    /**
     * Sets the server connection status.
     *
     * @param connected {@code true} if the application is connected to the server, {@code false} otherwise.
     */
    void setServerConnected(boolean connected);

    /**
     * Toggles the visibility of a spinner next to the animation list.
     *
     * @param showSpinner {@code true} to show the spinner, {@code false} to hide it.
     */
    void showAnimationListSpinner(boolean showSpinner);

    /**
     * Displays a dialog when there is a file collision during an upload.
     *
     * @param callback The callback to handle the user’s response (e.g., overwrite, cancel).
     * @param message The message to display to the user regarding the file collision.
     */
    void displayFileCollisionDialog(AlertDialog.ResponseCallback callback, String message);

    /**
     * Updates the upload progress shown on the UI.
     *
     * @param fraction The current progress as a value between 0.0 and 1.0.
     */
    void setUploadProgress(double fraction);

    /**
     * Indicates that the upload has finished.
     * <p>This method is called once the file upload process is completed, either successfully or with an error.</p>
     */
    void uploadFinished();

    /**
     * Displays the animation menu for a selected animation.
     *
     * @param menu The animation menu to display, containing available actions for the selected animation.
     */
    void displayAnimationMenu(AnimationMenu menu);

    /**
     * Updates the animation list with the provided animations.
     *
     * @param updatedAnimations A collection of updated animations to be displayed in the animation list.
     */
    void updateAnimations(@NotNull Collection<StatusReplyPacket.Animation> updatedAnimations);

    /**
     * Sets the state of the animation control buttons (e.g., play, pause, stop) based on the given file state.
     *
     * @param state The current state of the file, affecting the availability of control buttons.
     */
    void setAnimationControlButtonsState(@NotNull FileState state);

    /**
     * Changes the main content area of the window.
     *
     * @param widget The new widget to display in the main content area.
     */
    void changeMainContent(Widget widget);

    /**
     * Sets the sensitivity (enabled/disabled state) of the animation list.
     *
     * @param sensitive {@code true} to enable the animation list, {@code false} to disable it.
     */
    void setAnimationListSensitive(boolean sensitive);

    /**
     * Sets the visibility of the animations control buttons to the provided boolean value.
     *
     * @param visible {@code true} to show the animation control buttons, {@code false} to hide them.
     */
    void setAnimationControlButtonsVisible(boolean visible);
}
