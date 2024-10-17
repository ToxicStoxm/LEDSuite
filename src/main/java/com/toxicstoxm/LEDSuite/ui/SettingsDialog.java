package com.toxicstoxm.LEDSuite.ui;

import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.Types;
import org.gnome.adw.PreferencesDialog;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;

import java.lang.foreign.MemorySegment;

@GtkTemplate(name = "SettingsDialog", ui = "/com/toxicstoxm/LEDSuite/SettingsDialog.ui")
public class SettingsDialog extends PreferencesDialog {

    private static final Type gtype = Types.register(SettingsDialog.class);

    public SettingsDialog(MemorySegment address) {
        super(address);
    }

    public static Type getType() {
        return gtype;
    }

    public static SettingsDialog create() {
        return GObject.newInstance(getType());
    }

}
