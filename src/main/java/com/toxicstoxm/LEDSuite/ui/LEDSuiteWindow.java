package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.*;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.media_request.PauseRequestPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.media_request.PlayRequestPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.media_request.StopRequestPacket;
import com.toxicstoxm.LEDSuite.communication.websocket.WebSocketClient;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import com.toxicstoxm.LEDSuite.time.Action;
import com.toxicstoxm.LEDSuite.ui.animation_menu.AnimationMenu;
import com.toxicstoxm.LEDSuite.ui.dialogs.ProviderCallback;
import com.toxicstoxm.LEDSuite.ui.dialogs.UpdateCallback;
import com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog.SettingsData;
import com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog.SettingsDialog;
import com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog.SettingsUpdate;
import com.toxicstoxm.LEDSuite.ui.dialogs.status_dialog.StatusDialog;
import com.toxicstoxm.LEDSuite.ui.dialogs.status_dialog.StatusUpdate;
import io.github.jwharm.javagi.gtk.annotations.GtkCallback;
import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.TemplateTypes;
import io.github.jwharm.javagi.gtk.types.Types;
import lombok.Getter;
import org.gnome.adw.AboutDialog;
import org.gnome.adw.Application;
import org.gnome.adw.ApplicationWindow;
import org.gnome.adw.OverlaySplitView;
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

    private static final Type gtype = TemplateTypes.register(LEDSuiteWindow.class);

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

    @Getter
    private UpdateCallback<StatusUpdate> statusDialogUpdateCallback;

    /**
     * Displays the status dialog if it isn't already open.
     */
    public void displayStatusDialog() {
        if (statusDialogUpdateCallback == null) {
            var statusDialog = StatusDialog.create();
            statusDialogUpdateCallback = statusDialog.getUpdater();
            statusDialog.onClosed(() -> statusDialogUpdateCallback = null);
            statusDialog.present(this);
            LEDSuiteApplication.getLogger().info("Opened new status dialog!", new LEDSuiteLogAreas.USER_INTERACTIONS());
        } else LEDSuiteApplication.getLogger().info("Couldn't open status dialog because it is already open!", new LEDSuiteLogAreas.USER_INTERACTIONS());
    }

    @Getter
    private UpdateCallback<SettingsUpdate> settingsDialogUpdateCallback;

    @Getter
    private ProviderCallback<SettingsData> settingsDataProviderCallback;

    private Action settingsDialogApplyFail;

    /**
     * Displays the preference dialog if it isn't already open.
     */
    public void displayPreferencesDialog() {
        if (settingsDialogUpdateCallback == null) {
            var settingsDialog = SettingsDialog.create();
            settingsDialogUpdateCallback = settingsDialog.getUpdater();
            settingsDataProviderCallback = settingsDialog.getProvider();
            settingsDialogApplyFail = settingsDialog.getApplyFail();
            settingsDialog.onClosed(() -> {
                settingsDialogUpdateCallback = null;
                settingsDataProviderCallback = null;
                settingsDialogApplyFail = null;
            });
            settingsDialog.present(this);
            LEDSuiteApplication.getLogger().info("Opened new settings dialog!", new LEDSuiteLogAreas.USER_INTERACTIONS());
        } else LEDSuiteApplication.getLogger().info("Couldn't open settings dialog because it is already open!", new LEDSuiteLogAreas.USER_INTERACTIONS());
    }

    public void settingsDialogApply() {
        if (settingsDataProviderCallback != null) {
            LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                    SettingsChangeRequestPacket
                            .fromSettingsData(settingsDataProviderCallback.getData())
                            .serialize()
            );
            LEDSuiteApplication.getLogger().info("Applied settings and send changes to server!", new LEDSuiteLogAreas.USER_INTERACTIONS());
        } else LEDSuiteApplication.getLogger().warn("Couldn't apply settings because no settings data provider callback was found!", new LEDSuiteLogAreas.USER_INTERACTIONS());
    }

    public void settingsDialogApplyFail() {
        if (settingsDialogApplyFail != null) {
            settingsDialogApplyFail.run();
            LEDSuiteApplication.getLogger().info("Settings dialog apply failed, on cooldown!", new LEDSuiteLogAreas.USER_INTERACTIONS());
        } else LEDSuiteApplication.getLogger().info("Couldn't fail settings dialog apply, apply fail callback missing!", new LEDSuiteLogAreas.USER_INTERACTIONS());
    }

    @GtkChild(name = "content-box-revealer")
    public Revealer contentBoxRevealer;

    @GtkChild(name = "content-box")
    public Box contentBox;

    /**
     * Clears the main window content box and display the specified object instead.
     * Must be sync with UI thread!
     * @param newChild the new object to display
     * @see #clearMainContent()
     */
    public void changeMainContent(Widget newChild) {
        clearMainContent();
        contentBox.append(newChild);
    }

    /**
     * Clears the main window content box.
     * Must be sync with UI thread!
     * @see #changeMainContent(Widget)
     */
    public void clearMainContent() {
        Widget child = contentBox.getFirstChild();
        while (child != null) {
            contentBox.remove(child);
            child = contentBox.getFirstChild();
        }
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

            /*new LEDSuiteRunnable() {
                @Override
                public void run() {
                    LEDSuiteApplication.getPacketReceivedHandler().handleIncomingPacket(
                            StatusReplyPacket.builder()
                                    .fileState(Math.random() > 0.77 ? Math.random() > 0.5 ? FileState.idle : FileState.playing : FileState.paused)
                                    .selectedFile("Test-Animation-" + Math.round(Math.random()))
                                    .currentDraw(Math.random() > 0.5 ? null : (double) (Math.round(Math.random() * Math.random() * 1000)) / 1000)
                                    .voltage(Math.random() > 0.5 ? null : (double) (Math.round(Math.random() * Math.random() * 1000)) / 1000)
                                    .lidState(Math.random() > 0.5 ? null : Math.random() > 0.5 ? LidState.open : LidState.closed)
                                    .animations(List.of(
                                            new StatusReplyPacket.InteractiveAnimation("1", "Test-Animation1", "some-gnome-label", true),
                                            new StatusReplyPacket.InteractiveAnimation("1", "Test-Animation2", "some-gnome-label", false),
                                            new StatusReplyPacket.InteractiveAnimation("1", "Test-Animation3", "some-gnome-label", true)
                                    ))
                                    .build()
                    );
                }
            }.runTaskTimerAsynchronously(1000, 500);*/

            LEDSuiteApplication.getLogger().info("TestRow");
        }));

        animationList.append(AnimationRow.create(getApplication(), "media-optical-cd-audio-symbolic", "test", String.valueOf(UUID.randomUUID()), () -> {
            clearMainContent();
            changeMainContent(AnimationMenu.create("lol").init());
            LEDSuiteApplication.getLogger().info("test");
        }));
        animationList.append(AnimationRow.create(getApplication(), "media-optical-cd-audio-symbolic", "TestRow", String.valueOf(UUID.randomUUID()), () -> {
            clearMainContent();

            /*new LEDSuiteRunnable() {
                @Override
                public void run() {
                    LEDSuiteApplication.getPacketReceivedHandler().handleIncomingPacket(
                            SettingsReplyPacket.builder()
                                    .brightness((int) Math.round(Math.random() * Math.random() * Math.pow(10000, Math.random()) % 100))
                                    .selectedColorMode(Math.random() > 0.77 ? Math.random() > 0.5 ? "RGB" : "RGBW" : null)
                                    .availableColorModes(/*Math.random() > 0.77 ? Math.random() > 0.5 ? List.of("RGB", "RGBW") : List.of("RGB") : null*//* List.of("RGB", "RGBW"))
                                    .build()
                    );
                }
            }.runTaskTimerAsynchronously(1000, 1000);*/

            LEDSuiteApplication.getLogger().info("TestRow");
        }));
        animationList.append(AnimationRow.create(getApplication(), "media-optical-cd-audio-symbolic", "TestRow2", String.valueOf(UUID.randomUUID()), () -> {
            clearMainContent();

            new LEDSuiteRunnable() {
                @Override
                public void run() {
                    WebSocketClient client = LEDSuiteApplication.getWebSocketCommunication();

                    client.enqueueMessage(
                            PlayRequestPacket.builder().requestFile("test-animation").build().serialize()
                    );

                    client.enqueueMessage(
                            PauseRequestPacket.builder().requestFile("test-animation").build().serialize()
                    );

                    client.enqueueMessage(
                            StopRequestPacket.builder().requestFile("test-animation").build().serialize()
                    );

                    client.enqueueMessage(
                            FileUploadRequestPacket.builder().uploadSessionId(String.valueOf(UUID.randomUUID())).requestFile("test-animation").build().serialize()
                    );

                    client.enqueueMessage(
                            RenameRequestPacket.builder().newName("new-name").requestFile("old-name").build().serialize()
                    );
                    client.enqueueMessage(
                            MenuChangeRequestPacket.builder().objectId(String.valueOf(UUID.randomUUID())).objectValue("sdhfjsdhfs").fileName("Test-Animation").build().serialize()
                    );
                }
            }.runTaskAsynchronously();

            LEDSuiteApplication.getLogger().info("TestRow2");
        }));
        super.present();
    }
}

/*
<attributes>
  <attribute end="-1" name="scale" value="2"/>
</attributes>
 */
