package com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs;

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

    private ResponseCallback responseCallback;

    public void onResponse(ResponseCallback responseCallback) {
        this.responseCallback = responseCallback;
    }

    @Override
    protected void response(String response) {
        if (responseCallback != null) {
            responseCallback.run(response);
        }
    }
}
