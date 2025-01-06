/*
package com.toxicstoxm.LEDSuite.ui.dialogs.shortcuts_dialog;


import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.TemplateTypes;
import org.gnome.gobject.GObject;
import org.gnome.gtk.ApplicationWindow;
import org.gnome.gtk.ShortcutsWindow;

import java.lang.foreign.MemorySegment;

@GtkTemplate(name = "LEDShortcutsDialog", ui = "/com/toxicstoxm/LEDSuite/ShortcutsDialog.ui")
public class ShortcutsDialog extends ShortcutsWindow {

    static {
        TemplateTypes.register(ShortcutsDialog.class);
    }

    public ShortcutsDialog(MemorySegment address) {
        super(address);
    }

    public static ShortcutsDialog create() {
        return GObject.newInstance(ShortcutsDialog.class);
    }

    public void present(ApplicationWindow parent) {
        this.setParent(parent);
        super.present();
    }
}
*/