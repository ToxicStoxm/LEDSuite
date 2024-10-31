/*
package com.toxicstoxm.LEDSuite.ui.dialogs.shortcuts_dialog;


import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.TemplateTypes;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.ApplicationWindow;
import org.gnome.gtk.ShortcutsWindow;

import java.lang.foreign.MemorySegment;

@GtkTemplate(name = "LEDShortcutsDialog", ui = "/com/toxicstoxm/LEDSuite/ShortcutsDialog.ui")
public class ShortcutsDialog extends ShortcutsWindow {

    private static final Type gtype = TemplateTypes.register(ShortcutsDialog.class);

    public ShortcutsDialog(MemorySegment address) {
        super(address);
    }

    public static Type getType() {
        return gtype;
    }

    public static ShortcutsDialog create() {
        return GObject.newInstance(getType());
    }

    public void present(ApplicationWindow parent) {
        this.setParent(parent);
        super.present();
    }
}
*/