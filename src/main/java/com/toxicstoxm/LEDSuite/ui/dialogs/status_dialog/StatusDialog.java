package com.toxicstoxm.LEDSuite.ui.dialogs.status_dialog;

import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.status_reply.FileState;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.status_reply.LidState;
import com.toxicstoxm.LEDSuite.gettext.Translations;
import com.toxicstoxm.YAJL.core.Logger;
import com.toxicstoxm.YAJL.core.LoggerManager;
import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import org.gnome.adw.ActionRow;
import org.gnome.adw.Dialog;
import org.gnome.glib.GLib;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * Represents a dialog for displaying the current status of various system parameters.
 * <p>This class manages the update of status information such as voltage, current draw, file state,
 * lid state, and the current file. It interacts with GTK-based UI components to dynamically show
 * or hide values based on the provided updates.</p>
 * <p>Template file: {@code StatusDialog.ui}</p>
 *
 * @since 1.0.0
 */
@GtkTemplate(name = "StatusDialog", ui = "/com/toxicstoxm/LEDSuite/StatusDialog.ui")
public class StatusDialog extends Dialog implements StatusDialogEndpoint {
    private static final Logger logger = LoggerManager.getLogger(StatusDialog.class);

    public StatusDialog() {
        super();
        logger.verbose("Configuring UI state");
        markAllUnavailable();
    }

    // GTK UI elements defined in the template
    @GtkChild(name = "status_lid_state")
    public ActionRow lidState;

    @GtkChild(name = "status_current_file")
    public ActionRow currentFile;

    @GtkChild(name = "status_current_state")
    public ActionRow currentState;

    @GtkChild(name = "status_current")
    public ActionRow currentDraw;

    @GtkChild(name = "status_voltage")
    public ActionRow voltage;

    /**
     * Updates the voltage value or hides the voltage row if no value is provided.
     *
     * @param voltage the voltage value to display or {@code null} to hide the voltage row
     */
    private void setVoltage(Double voltage) {
        if (voltage == null) {
            markUnavailable(this.voltage);
        } else {
            markAvailableWithValue(this.voltage, voltage + "V");
        }
    }

    /**
     * Updates the current draw value or hides the current draw row if no value is provided.
     *
     * @param currentDraw the current draw value to display or {@code null} to hide the row
     */
    private void setCurrentDraw(Double currentDraw) {
        if (currentDraw == null) {
            markUnavailable(this.currentDraw);
        } else {
            markAvailableWithValue(this.currentDraw, currentDraw + "A");
        }
    }

    /**
     * Updates the file state row with the provided value.
     *
     * @param fileState the file state to display or {@code null} to hide the row
     */
    private void setFileState(FileState fileState) {
        if (fileState == null) {
            markUnavailable(this.currentState);
        } else {
            markAvailableWithValue(this.currentState, fileState.name());
        }
    }

    /**
     * Updates the current file row with the provided file name.
     *
     * @param currentFile the name of the current file to display or {@code null} to hide the row
     */
    private void setCurrentFile(String currentFile) {
        if (currentFile == null) {
            markUnavailable(this.currentFile);
        } else {
            markAvailableWithValue(this.currentFile, currentFile);
        }
    }

    /**
     * Updates the lid state row with the provided value.
     *
     * @param lidState the lid state to display or {@code null} to hide the row
     */
    private void setLidState(LidState lidState) {
        if (lidState == null) {
            markUnavailable(this.lidState);
        } else {
            markAvailableWithValue(this.lidState, lidState.name());
        }
    }

    /**
     * Marks all status rows as unavailable by setting their subtitle to "N/A" and disabling interaction.
     */
    private void markAllUnavailable() {
        markAllUnavailable(
                List.of(
                        lidState,
                        currentDraw,
                        voltage,
                        currentFile,
                        currentState
                )
        );
    }

    /**
     * Marks the specified rows as unavailable by setting their subtitle to "N/A" and disabling interaction.
     *
     * @param rows the rows to mark as unavailable
     */
    public static void markAllUnavailable(@NotNull Collection<ActionRow> rows) {
        for (ActionRow row : rows) {
            markUnavailable(row);
        }
    }

    /**
     * Marks a specific row as unavailable by setting its subtitle to "N/A" and disabling it.
     *
     * @param row the row to mark as unavailable
     */
    public static void markUnavailable(@NotNull ActionRow row) {
        row.setSubtitle(Translations.getText("N/A"));
        row.setSensitive(false);
    }

    /**
     * Marks a specific row as available by setting its subtitle to the specified value and enabling it.
     *
     * @param row   the row to mark as available
     * @param value the value to display in the row
     */
    private void markAvailableWithValue(@NotNull ActionRow row, @NotNull String value) {
        row.setSubtitle(value.replaceAll("&", "&amp;")); // escape ampersand as '&amp;' for libadwaita
        row.setSensitive(true);
    }

    /**
     * Updates the dialog with the provided status information.
     * <p>This method updates the rows based on the given {@link StatusUpdate} and ensures that only
     * the relevant rows are visible.</p>
     *
     * @param statusUpdate the new status information to display in the dialog
     */
    public void update(@NotNull StatusUpdate statusUpdate) {
        logger.verbose("Updating status -> {}", statusUpdate);
        GLib.idleAddOnce(() -> {
            setVoltage(statusUpdate.voltage());
            setCurrentDraw(statusUpdate.currentDraw());
            setLidState(statusUpdate.lidState());
            setFileState(statusUpdate.fileState());
            setCurrentFile(statusUpdate.currentFile());
        });
    }
}
