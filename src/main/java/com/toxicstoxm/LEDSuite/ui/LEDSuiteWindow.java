package com.toxicstoxm.LEDSuite.ui;

import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.Types;
import org.gnome.adw.Application;
import org.gnome.adw.ApplicationWindow;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;

import java.lang.foreign.MemorySegment;

@GtkTemplate(name = "LEDSuiteWindow", ui = "/com/toxicstoxm/LEDSuite/LEDSuiteWindow.ui")
public class LEDSuiteWindow extends ApplicationWindow {

    private static final Type gtype = Types.register(LEDSuiteWindow.class);

    public LEDSuiteWindow(MemorySegment address) {
        super(address);
    }

    public static Type getType() {
        return gtype;
    }

    public static LEDSuiteWindow create(Application app) {
        return GObject.newInstance(getType(),
                "application", app);
    }

}
