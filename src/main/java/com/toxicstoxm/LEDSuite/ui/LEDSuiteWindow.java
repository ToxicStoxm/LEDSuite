package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.communication.packet_management.packets.status.StatusRequestPacket;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.ui.dialogs.SettingsDialog;
import com.toxicstoxm.LEDSuite.ui.dialogs.StatusDialog;
import io.github.jwharm.javagi.gtk.annotations.GtkCallback;
import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.Types;
import org.gnome.adw.*;
import org.gnome.adw.AboutDialog;
import org.gnome.adw.Application;
import org.gnome.adw.ApplicationWindow;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.*;

import java.lang.foreign.MemorySegment;
import java.util.UUID;

/**
 * Main application window class. Holds and manages all UI related elements.
 * <br>Template file: {@code LEDSuiteWindow.ui}
 * @since 1.0
 */
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

    @GtkChild(name = "shortcuts_dialog")
    public ShortcutsWindow shortcutsDialog;

    public void displayShortcutsWindow() {
        shortcutsDialog.present();
    }

    public void displayStatusDialog() {
        StatusDialog.create().present(this);
    }

    public void displayPreferencesDialog() {
        SettingsDialog.create().present(this);
    }

    @GtkChild(name = "content-box-revealer")
    public Revealer contentBoxRevealer;

    @GtkChild(name = "content-box")
    public Box contentBox;

    /**
     * Clears the main window content box and display the specified object instead.
     * @param newChild the new object to display
     * @see #clearMainContent()
     */
    public void changeMainContent(Widget newChild) {
        clearMainContent();
        contentBox.append(newChild);
    }

    /**
     * Clears the main window content box.
     * @see #changeMainContent(Widget)
     */
    public void clearMainContent() {
        Widget child = contentBox.getFirstChild();
        if (child != null) contentBox.remove(child);
    }

    @GtkChild(name = "animation_list")
    public ListBox animationList;

    @GtkChild(name = "file_management_list")
    public ListBox fileManagementList;

    @GtkChild(name = "file_management_upload_files_page")
    public ListBoxRow fileManagementUploadFilesPage;

    public void uploadPageSelect() {
        LEDSuiteApplication.getLogger().info("Upload files page selected!", new LEDSuiteLogAreas.USER_INTERACTIONS());
        changeMainContent(com.toxicstoxm.LEDSuite.ui.UploadPage.create(this));
        animationList.setSelectionMode(SelectionMode.NONE);
        animationList.setSelectionMode(SelectionMode.BROWSE);
    }

    @Override
    public void present() {
        animationList.append(AnimationRow.create(getApplication(), "emoji-food-symbolic", "Test", String.valueOf(UUID.randomUUID()), () -> {
            clearMainContent();

            LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                    StatusRequestPacket.builder().build().serialize()
            );

            LEDSuiteApplication.getLogger().info("Test");
        }));
        animationList.append(AnimationRow.create(getApplication(), "media-optical-cd-audio-symbolic", "TestRow", String.valueOf(UUID.randomUUID()), () -> {
            clearMainContent();
            LEDSuiteApplication.getLogger().info("TestRow");
        }));

        animationList.append(AnimationRow.create(getApplication(), "media-optical-cd-audio-symbolic", "test", String.valueOf(UUID.randomUUID()), () -> {
            clearMainContent();
            LEDSuiteApplication.getLogger().info("test");
        }));
        animationList.append(AnimationRow.create(getApplication(), "media-optical-cd-audio-symbolic", "TestRow", String.valueOf(UUID.randomUUID()), () -> {
            clearMainContent();
            LEDSuiteApplication.getLogger().info("TestRow");
        }));
        animationList.append(AnimationRow.create(getApplication(), "media-optical-cd-audio-symbolic", "TestRow2", String.valueOf(UUID.randomUUID()), () -> {
            clearMainContent();
            LEDSuiteApplication.getLogger().info("TestRow2");

            changeMainContent(
                    LEDSuiteApplication.getAnimationMenuConstructor().constructMenuFromYAML("test")
            );
        }));
        super.present();
    }
}

/*
<attributes>
  <attribute end="-1" name="scale" value="2"/>
</attributes>
 */
