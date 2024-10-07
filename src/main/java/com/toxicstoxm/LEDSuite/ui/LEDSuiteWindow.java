package com.toxicstoxm.LEDSuite.ui;

import io.github.jwharm.javagi.gtk.annotations.GtkCallback;
import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.Types;
import org.gnome.adw.*;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.Box;
import org.gnome.gtk.Revealer;
import org.gnome.gtk.ShortcutsWindow;

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

    @GtkChild
    public OverlaySplitView split_view;

    private boolean sideBarBreakpointState = false;

    @GtkCallback(name = "sidebar_breakpoint_apply")
    public void sidebarBreakpointApply() {
        sideBarBreakpointState = true;
    }

    @GtkCallback(name = "sidebar_breakpoint_unapply")
    public void sidebarBreakpointUnapply() {
        sideBarBreakpointState = false;
    }

    public void toggle_sidebar() {
        if (sideBarBreakpointState) split_view.setShowSidebar(!split_view.getShowSidebar());
    }

    public void displayAboutDialog() {
        AboutDialog aboutDialog = AboutDialog.fromAppdata("/com/toxicstoxm/LEDSuite/com.toxicstoxm.LEDSuite.appdata.xml", LEDSuiteApplication.version);
        aboutDialog.setApplicationIcon("com.toxicstoxm.LEDSuite");
        aboutDialog.present(this);
    }

    @GtkChild
    public ShortcutsWindow shortcuts_window;

    public void displayShortcutsWindow() {
        shortcuts_window.present();
    }

    @GtkChild
    public PreferencesDialog settings_dialog;

    public void displayPreferencesDialog() {
        settings_dialog.present(this);
    }

    @GtkChild
    public Dialog status_dialog;

    public void displayStatusDialog() {
        status_dialog.present(this);
    }

    @GtkChild(name = "content-box-revealer")
    public Revealer contentBoxRevealer;

    @GtkChild(name = "content-box")
    public Box contentBox;

    @Override
    public void present() {
        contentBox.append(UploadPage.create(this));
        super.present();
    }
}
