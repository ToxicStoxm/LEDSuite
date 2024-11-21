package com.toxicstoxm.LEDSuite.ui.dialogs.status_dialog;

import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.status_reply.FileState;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.status_reply.LidState;
import com.toxicstoxm.LEDSuite.gettext.Translations;
import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.TemplateTypes;
import org.gnome.adw.ActionRow;
import org.gnome.adw.Dialog;
import org.gnome.glib.GLib;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.jetbrains.annotations.NotNull;

import java.lang.foreign.MemorySegment;
import java.util.Collection;
import java.util.List;

/**
 * StatusReply dialog template class with update functions.
 * <br>Template file: {@code StatusDialog.ui}
 * @since 1.0.0
 */
@GtkTemplate(name = "StatusDialog", ui = "/com/toxicstoxm/LEDSuite/StatusDialog.ui")
public class StatusDialog extends Dialog implements StatusDialogEndpoint {

    private static final Type gtype = TemplateTypes.register(StatusDialog.class);

    public StatusDialog(MemorySegment address) {
        super(address);
    }

    public static Type getType() {
        return gtype;
    }

    public static @NotNull StatusDialog create() {
        StatusDialog dialog = GObject.newInstance(getType());
        dialog.markAllUnavailable();
        return dialog;
    }

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
     * Updates the voltage value or hides the voltage row if no value was specified.
     * @param voltage the new voltage value or {@code null} to hide the voltage row
     */
    private void setVoltage(Double voltage) {
        if (voltage == null) {
            markUnavailable(this.voltage);
        } else {
            markAvailableWithValue(this.voltage, voltage + "V");
        }
    }

    /**
     * Updates the current-draw value or hides the current-draw row if no value was specified.
     * @param currentDraw the new current-draw value or {@code null} to hide the current-draw row
     */
    private void setCurrentDraw(Double currentDraw) {
        if (currentDraw == null) {
            markUnavailable(this.currentDraw);

        } else {
            this.markAvailableWithValue(this.currentDraw, currentDraw + "A");
        }
    }

    /**
     * Updates the file state value.
     * @param fileState the new file state value
     */
    private void setFileState(FileState fileState) {
        if (fileState == null) {
            markUnavailable(this.currentState);
        } else {
            this.markAvailableWithValue(this.currentState, fileState.name());
        }
    }

    /**
     * Updates the current-file value or hides the current-file row if no value was specified.
     * @param currentFile the new current-file value or {@code null} to hide the current-file row
     */
    private void setCurrentFile(String currentFile) {
        if (currentFile == null) {
            markUnavailable(this.currentFile);
        } else {
            markAvailableWithValue(this.currentFile, currentFile);
        }
    }

    /**
     * Updates the lid-state value or hides the lid-state row if no value was specified.
     * @param lidState the new lid-state value or {@code null} to hide the lid-state row
     */
    private void setLidState(LidState lidState) {
        if (lidState == null) {
            markUnavailable(this.lidState);
        } else {
            markAvailableWithValue(this.lidState, lidState.name());
        }
    }

    /**
     * Marks all rows as unavailable using {@link #markUnavailable(ActionRow)}
     * @see #markAvailableWithValue(ActionRow, String)
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

    public static void markAllUnavailable(@NotNull Collection<ActionRow> rows) {
        for (ActionRow row : rows) {
            markUnavailable(row);
        }
    }

    public static void markUnavailable(@NotNull ActionRow row) {
        row.setSubtitle(Translations.getText("N/A"));
        row.setSensitive(false);
    }

    /**
     * Marks the specified row as available by resetting its opacity and changing its value to the specified new value.
     * @param row the row to mark as available
     * @param value the new value to display in the row
     * @see #markAllUnavailable()
     */
    private void markAvailableWithValue(@NotNull ActionRow row, String value) {
        row.setSubtitle(value);
        row.setSensitive(true);
    }

    /**
     * Update the status values to the provided ones
     * and changes the status rows and groups visibility based on what values are provided.
     * @param statusUpdate the new values to display
     */
    public void update(@NotNull StatusUpdate statusUpdate) {
        GLib.idleAddOnce(() -> {
            setVoltage(statusUpdate.voltage());
            setCurrentDraw(statusUpdate.currentDraw());
            setLidState(statusUpdate.lidState());
            setFileState(statusUpdate.fileState());
            setCurrentFile(statusUpdate.currentFile());
        });
    }
}
