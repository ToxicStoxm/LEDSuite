package com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs;

import com.toxicstoxm.LEDSuite.gettext.Translations;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteTask;
import com.toxicstoxm.YAJL.Logger;
import com.toxicstoxm.YAJL.placeholders.StringPlaceholder;
import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.TemplateTypes;
import lombok.Getter;
import org.gnome.adw.AlertDialog;
import org.gnome.adw.EntryRow;
import org.gnome.glib.GLib;
import org.gnome.gobject.GObject;
import org.gnome.gtk.Revealer;
import org.gnome.gtk.Widget;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.MemorySegment;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A dialog for renaming a file or item.
 * <p>
 * This dialog allows the user to provide a new name for an existing file or item.
 * It validates the input, ensuring the new name is different from the current name,
 * and that the name doesn't exceed the allowed character limit.
 * </p>
 *
 * @since 1.0.0
 */
@GtkTemplate(name = "RenameDialog", ui = "/com/toxicstoxm/LEDSuite/RenameDialog.ui")
public class RenameDialog extends AlertDialog {

    private static final Logger logger = Logger.autoConfigureLogger();

    static {
        TemplateTypes.register(RenameDialog.class);
    }

    /**
     * Constructs a new instance of the RenameDialog using the provided memory address.
     *
     * @param address the memory segment address used to create the dialog instance
     */
    public RenameDialog(MemorySegment address) {
        super(address);
    }

    /**
     * Creates and returns a new instance of the RenameDialog with the specified file name.
     *
     * @param fileName the name of the file to rename
     * @return a new RenameDialog instance
     * @throws IllegalArgumentException if the file name is null or empty
     */
    @Contract("null -> fail")
    public static @NotNull RenameDialog create(String fileName) {
        logger.verbose("Creating new rename dialog from file name -> '{}'", fileName);
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("Input filename can't be null or empty!");
        }
        RenameDialog renameDialog = GObject.newInstance(RenameDialog.class);
        renameDialog.init(fileName);
        return renameDialog;
    }

    // The new name entered by the user
    @Getter
    private String newName;

    // The current name of the file being renamed
    private String currentName;

    // Task that checks the validity of the entered name
    private LEDSuiteTask nameCheckerTask;

    /**
     * Initializes the dialog with the given file name.
     * Sets the current name and updates the UI with the file name.
     *
     * @param fileName the name of the file to rename
     */
    protected void init(String fileName) {
        logger.verbose("Initializing instance with file name -> '{}'", fileName);
        logger.verbose("Configuring UI state");
        currentName = fileName;
        this.fileNameRow.setText(currentName);
        this.setHeading(Translations.getText("Rename") + " " + currentName);
        newName = fileName;
        logger.verbose("Instance initialized, ready");
    }

    // Callback to handle user response actions
    private ResponseCallback responseCallback;

    /**
     * Sets the callback to be executed when a response is received from the dialog.
     *
     * @param responseCallback the callback to be executed when the user responds
     */
    public void onResponse(ResponseCallback responseCallback) {
        this.responseCallback = responseCallback;
    }

    /**
     * Handles the response triggered by the user in the dialog.
     * This method checks if the new name is different from the current name and if it's valid.
     * If the user attempts to rename the file with the same name, a new dialog will be presented.
     *
     * @param response the response selected by the user (e.g., "rename")
     */
    @Override
    protected void response(@NotNull String response) {
        logger.verbose("Received rename response -> '{}'", response);
        nameCheckerTask.cancel();
        logger.verbose("Cancelled name checker task");
        if (response.equals("rename") && newName.equals(currentName)) {
            logger.warn("New name equals current name after rename response!");
            logger.verbose("Calling parent dialog to handle the duplicate name");
            var dialog = create(currentName);
            dialog.onResponse(responseCallback);
            dialog.present(getParent());
        } else {
            logger.verbose("Calling response handler");
            if (responseCallback != null) {
                responseCallback.run(response);
            }
        }
    }

    // GtkChild element representing the entry row for the file name
    @GtkChild(name = "filename_row")
    public EntryRow fileNameRow;

    // GtkChild element for revealing the "name too long" message
    @GtkChild(name = "filename_too_long_revealer")
    public Revealer fileNameTooLongRevealer;

    /**
     * Presents the dialog to the user, periodically checking if the entered file name is valid.
     * Enables or disables the "rename" button based on the validity of the new name.
     *
     * @param parent the parent widget, to which the dialog is attached, can be null
     */
    @Override
    public void present(@Nullable Widget parent) {
        logger.verbose("Received display request with parent widget -> '{}'",
                (StringPlaceholder) () -> parent == null ? "unknown" : parent.getName()
        );

        logger.verbose("Starting name checker task");
        AtomicReference<String> last = new AtomicReference<>(newName);
        // Task to periodically check the validity of the file name
        nameCheckerTask = new LEDSuiteRunnable() {
            @Override
            public void run() {
                GLib.idleAddOnce(() -> newName = fileNameRow.getText());
                if (!Objects.equals(last.get(), newName)) {
                    last.set(newName);
                    GLib.idleAddOnce(() -> {
                        boolean fileNameTooLong = newName.length() > 255;
                        fileNameTooLongRevealer.setRevealChild(fileNameTooLong);
                        setResponseEnabled("rename", !newName.equals(currentName) && !newName.isBlank() && !fileNameTooLong);
                        newName = fileNameRow.getText();
                    });
                }
            }
        }.runTaskTimerAsynchronously(10, 1);

        logger.verbose("Displaying rename dialog");
        super.present(parent);
    }
}
