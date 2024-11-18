package com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs;

import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteTask;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.TemplateTypes;
import lombok.Getter;
import org.gnome.adw.AlertDialog;
import org.gnome.adw.EntryRow;
import org.gnome.glib.GLib;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.Widget;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.MemorySegment;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@GtkTemplate(name = "RenameDialog", ui = "/com/toxicstoxm/LEDSuite/RenameDialog.ui")
public class RenameDialog extends AlertDialog {

    private static final Type gtype = TemplateTypes.register(RenameDialog.class);

    public RenameDialog(MemorySegment address) {
        super(address);
    }

    public static Type getType() {
        return gtype;
    }

    @Contract("null -> fail")
    public static @NotNull RenameDialog create(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("Input filename can't be null or empty!");
        }
        RenameDialog renameDialog = GObject.newInstance(getType());
        renameDialog.init(fileName);
        return renameDialog;
    }

    @Getter
    private String newName;
    private String currentName;
    private LEDSuiteTask nameCheckerTask;

    protected void init(String fileName) {
        currentName = fileName;
        this.fileNameRow.setText(currentName);
        this.setHeading("Rename " + currentName);
        newName = fileName;
    }

    private ResponseCallback responseCallback;

    public void onResponse(ResponseCallback responseCallback) {
        this.responseCallback = responseCallback;
    }

    @Override
    protected void response(@NotNull String response) {
        nameCheckerTask.cancel();
        if (response.equals("rename") && newName.equals(currentName)) {
            LEDSuiteApplication.getLogger().warn("New name equals current name after rename response!", new LEDSuiteLogAreas.USER_INTERACTIONS());
            var dialog = create(currentName);
            dialog.onResponse(responseCallback);
            dialog.present(getParent());
        } else {
            if (responseCallback != null) {
                responseCallback.run(response);
            }
        }
    }

    @GtkChild(name = "filename_row")
    public EntryRow fileNameRow;

    @Override
    public void present(@Nullable Widget parent) {
        AtomicReference<String> last = new AtomicReference<>(newName);

        nameCheckerTask = new LEDSuiteRunnable() {
            @Override
            public void run() {
                GLib.idleAddOnce(() -> newName = fileNameRow.getText());
            if (!Objects.equals(last.get(), newName)) {
                    last.set(newName);
                    GLib.idleAddOnce(() -> {
                        setResponseEnabled("rename", !newName.equals(currentName) && !newName.isBlank());
                        newName = fileNameRow.getText();
                    });
                }
            }
        }.runTaskTimerAsynchronously(10, 1);
        super.present(parent);
    }


}
