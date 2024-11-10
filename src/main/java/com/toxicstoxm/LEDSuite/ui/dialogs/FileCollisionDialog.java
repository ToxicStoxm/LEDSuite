package com.toxicstoxm.LEDSuite.ui.dialogs;

import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import io.github.jwharm.javagi.gtk.annotations.GtkCallback;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.TemplateTypes;
import org.gnome.adw.AlertDialog;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;

import java.lang.foreign.MemorySegment;

@GtkTemplate(name = "FileCollisionDialog", ui = "/com/toxicstoxm/LEDSuite/FileCollisionDialog.ui")
public class FileCollisionDialog extends AlertDialog {

    private static final Type gtype = TemplateTypes.register(FileCollisionDialog.class);

    public FileCollisionDialog(MemorySegment address) {
        super(address);
    }

    public static Type getType() {
        return gtype;
    }

    public static FileCollisionDialog create() {
        return GObject.newInstance(getType());
    }

    @GtkCallback(name = "file_collision_response_cb")
    public void onResponse(String response) {
        LEDSuiteApplication.getLogger().info(response);
    }
}
