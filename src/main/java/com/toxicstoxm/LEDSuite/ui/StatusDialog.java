package com.toxicstoxm.LEDSuite.ui;

import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.Types;
import org.gnome.adw.Dialog;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;

import java.lang.foreign.MemorySegment;

@GtkTemplate(name = "StatusDialog", ui = "/com/toxicstoxm/LEDSuite/StatusDialog.ui")
public class StatusDialog extends Dialog {

    private static final Type gtype = Types.register(StatusDialog.class);

    public StatusDialog(MemorySegment address) {
        super(address);
    }

    public static Type getType() {
        return gtype;
    }

    public static StatusDialog create() {
        return GObject.newInstance(getType());
    }

}
