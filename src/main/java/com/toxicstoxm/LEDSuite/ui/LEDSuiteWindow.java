package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.status_reply.StatusReplyPacket;
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
import com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog.*;
import com.toxicstoxm.LEDSuite.ui.dialogs.status_dialog.StatusDialog;
import com.toxicstoxm.LEDSuite.ui.dialogs.status_dialog.StatusDialogEndpoint;
import io.github.jwharm.javagi.gtk.annotations.GtkCallback;
import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.TemplateTypes;
import lombok.Getter;
import org.gnome.adw.AboutDialog;
import org.gnome.adw.Application;
import org.gnome.adw.ApplicationWindow;
import org.gnome.adw.OverlaySplitView;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.*;
import org.jetbrains.annotations.NotNull;

import java.lang.foreign.MemorySegment;
import java.util.*;

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
    private StatusDialogEndpoint statusDialog;

    /**
     * Displays the status dialog if it isn't already open.
     */
    public void displayStatusDialog() {
        if (statusDialog == null) {
            var statusDialog = StatusDialog.create();
            this.statusDialog = statusDialog.getStatusDialogEndpoint();
            statusDialog.onClosed(() -> this.statusDialog = null);
            statusDialog.present(this);
            LEDSuiteApplication.getLogger().info("Opened new status dialog!", new LEDSuiteLogAreas.USER_INTERACTIONS());
        } else LEDSuiteApplication.getLogger().info("Couldn't open status dialog because it is already open!", new LEDSuiteLogAreas.USER_INTERACTIONS());
    }

    @Getter
    private SettingsDialogEndpoint settingsDialog;

    /**
     * Displays the preference dialog if it isn't already open.
     */
    public void displayPreferencesDialog() {
        if (settingsDialog == null) {
            var settingsDialog = SettingsDialog.create();
            this.settingsDialog = new SettingsDialogEndpoint() {
                @Override
                public ConnectivityStatus connectivityManager() {
                    return settingsDialog.getConnectivityStatus();
                }

                @Override
                public ProviderCallback<SettingsData> settingsManager() {
                    return settingsDialog.getProvider();
                }

                @Override
                public UpdateCallback<SettingsUpdate> updater() {
                    return settingsDialog.getUpdater();
                }

                @Override
                public Action applyButtonCooldown() {
                    return settingsDialog.getApplyButtonCooldownTrigger();
                }
            };
            settingsDialog.onClosed(() -> this.settingsDialog = null);
            settingsDialog.present(this);
            LEDSuiteApplication.getLogger().info("Opened new settings dialog!", new LEDSuiteLogAreas.USER_INTERACTIONS());
        } else LEDSuiteApplication.getLogger().info("Couldn't open settings dialog because it is already open!", new LEDSuiteLogAreas.USER_INTERACTIONS());
    }

    public void settingsDialogApply() {
        if (settingsDialog != null) {
            LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                    SettingsChangeRequestPacket
                            .fromSettingsData(settingsDialog.settingsManager().getData())
                            .serialize()
            );
            LEDSuiteApplication.getLogger().info("Applied settings and send changes to server!", new LEDSuiteLogAreas.USER_INTERACTIONS());
        } else LEDSuiteApplication.getLogger().warn("Couldn't apply settings because no settings data provider callback was found!", new LEDSuiteLogAreas.USER_INTERACTIONS());
    }

    public void settingsDialogApplyFail() {
        if (settingsDialog != null) {
            settingsDialog.applyButtonCooldown().run();
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

    private final HashMap<String, AnimationRow> animations = new HashMap<>();

    @Getter
    private String selectedAnimation;

    @GtkChild(name = "file_management_list")
    public ListBox fileManagementList;

    @GtkChild(name = "file_management_upload_files_page")
    public ListBoxRow fileManagementUploadFilesPage;

    @GtkChild(name = "sidebar_animation_group_title")
    public Label animationGroupTitle;

    public void uploadPageSelect() {
        LEDSuiteApplication.getLogger().info("Upload files page selected!", new LEDSuiteLogAreas.USER_INTERACTIONS());
        changeMainContent(com.toxicstoxm.LEDSuite.ui.UploadPage.create(this));
        animationList.setSelectionMode(SelectionMode.NONE);
        animationList.setSelectionMode(SelectionMode.BROWSE);
    }

    public void setServerConnected(boolean serverConnected) {
        if (!serverConnected) LEDSuiteApplication.getWindow().uploadPageSelect();
        animationList.setSensitive(serverConnected);
        animationGroupTitle.setSensitive(serverConnected);
    }

    /**
     * Updates the available animation list in the sidebar of the application.
     * @param updatedAnimations new available animation list to display
     */
    public void updateAnimations(@NotNull Collection<StatusReplyPacket.InteractiveAnimation> updatedAnimations) {
        HashMap<String, AnimationRow> newAnimationRows = new HashMap<>();

        LEDSuiteApplication.getLogger().verbose("Updating available animations. Count: " + updatedAnimations.size(), new LEDSuiteLogAreas.UI());

        // Construct animation rows for all new animations and store them in a temporary map
        for (StatusReplyPacket.InteractiveAnimation updatedAnimation : updatedAnimations) {
            var animationRow = AnimationRow.create(
                    AnimationRowData.builder()
                            .app(getApplication())
                            .iconName(updatedAnimation.iconName())
                            .label(updatedAnimation.label())
                            .animationID(updatedAnimation.id())
                            .action(() -> {
                                LEDSuiteApplication.getLogger().verbose("Requesting menu for animation '" + updatedAnimation.id() + "'", new LEDSuiteLogAreas.USER_INTERACTIONS());
                                LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                                        MenuRequestPacket.builder().requestFile(updatedAnimation.id()).build().serialize()
                                );
                            })
                            .cooldown(500L)
                            .build()
            );

            newAnimationRows.put(updatedAnimation.id(), animationRow);
        }

        List<String> removedAnimations = new ArrayList<>(animations.keySet());

        // Loop through all new or updated animations.
        // If an animation with the same key already exists in the current list, it is re-added to update its values,
        // except if that animation row is currently selected.
        // Then just the label and icon name is updated to prevent
        // the user from being kicked out of that animation's menu.
        // If no animation with the same name already exists, it's simply added to the list.
        for (Map.Entry<String, AnimationRow> entry : newAnimationRows.entrySet()) {
            String newAnimation = entry.getKey();
            AnimationRow newAnimationRow = entry.getValue();

            removedAnimations.remove(newAnimation);

            if (animations.containsKey(newAnimation)) {
                LEDSuiteApplication.getLogger().verbose("Updated animation: " + entry.getKey(), new LEDSuiteLogAreas.UI());
                if (selectedAnimation.equals(newAnimation)) {
                    ListBoxRow selectedRow = animationList.getSelectedRow();
                    if (selectedRow instanceof AnimationRow animationRow) {
                        animationRow.setAnimationLabel(newAnimationRow.animationRowLabel.getLabel());
                        animationRow.setIconName(newAnimationRow.animationIcon.getIconName());
                    } else LEDSuiteApplication.getLogger().warn("Wasn't able to update selected animation!", new LEDSuiteLogAreas.UI());
                    continue;
                }

                animationList.remove(animations.remove(entry.getKey()));
            } else LEDSuiteApplication.getLogger().verbose("Added animation: " + entry.getKey(), new LEDSuiteLogAreas.UI());

            animations.put(entry.getKey(), entry.getValue());
            animationList.append(entry.getValue());
        }

        // The remaining animations are simply removed from the animation row list.
        // Except if a row is selected, then it is first deselected and the upload page is selected by default.
        for (String removedAnimation : removedAnimations) {
            if (removedAnimation.equals(selectedAnimation)) {
                animationList.unselectRow(animations.get(removedAnimation));
                uploadPageSelect();
            }
            animationList.remove(animations.remove(removedAnimation));
            LEDSuiteApplication.getLogger().verbose("Removed animation: " + removedAnimation, new LEDSuiteLogAreas.UI());
        }
    }

    @Override
    public void present() {
        animationList.append(AnimationRow.create(
                AnimationRowData.builder()
                        .app(getApplication())
                        .iconName("emoji-food-symbolic")
                        .label("Test")
                        .animationID(String.valueOf(UUID.randomUUID()))
                        .action(() -> {
                            clearMainContent();

                            LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                                    StatusRequestPacket.builder().build().serialize()
                            );

                            LEDSuiteApplication.getLogger().info("Test");
                        })
                        .build()
        ));

        animationList.append(AnimationRow.create(
                AnimationRowData.builder()
                        .app(getApplication())
                        .iconName("media-optical-cd-audio-symbolic")
                        .label("TestRow")
                        .animationID(String.valueOf(UUID.randomUUID()))
                        .action(() -> {
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
                        })
                        .build()
        ));

        animationList.append(AnimationRow.create(
                AnimationRowData.builder()
                        .app(getApplication())
                        .iconName("media-optical-cd-audio-symbolic")
                        .label("test")
                        .animationID(String.valueOf(UUID.randomUUID()))
                        .action(() -> {
                            clearMainContent();
                            changeMainContent(AnimationMenu.create("lol").init());
                            LEDSuiteApplication.getLogger().info("test");
                        })
                        .build()
        ));

        animationList.append(AnimationRow.create(
                AnimationRowData.builder()
                        .app(getApplication())
                        .iconName("media-optical-cd-audio-symbolic")
                        .label("TestRow")
                        .animationID(String.valueOf(UUID.randomUUID()))
                        .action(() -> {
                            clearMainContent();

                    /*new LEDSuiteRunnable() {
                        @Override
                        public void run() {
                            LEDSuiteApplication.getPacketReceivedHandler().handleIncomingPacket(
                                    SettingsReplyPacket.builder()
                                            .brightness((int) Math.round(Math.random() * Math.random() * Math.pow(10000, Math.random()) % 100))
                                            .selectedColorMode(Math.random() > 0.77 ? Math.random() > 0.5 ? "RGB" : "RGBW" : null)
                                            .availableColorModes(List.of("RGB", "RGBW"))
                                            .build()
                            );
                        }
                    }.runTaskTimerAsynchronously(1000, 1000);*/

                            LEDSuiteApplication.getLogger().info("TestRow");
                        })
                        .build()
        ));

        animationList.append(AnimationRow.create(
                AnimationRowData.builder()
                        .app(getApplication())
                        .iconName("media-optical-cd-audio-symbolic")
                        .label("TestRow2")
                        .animationID(String.valueOf(UUID.randomUUID()))
                        .action(() -> {
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
                        })
                        .build()
        ));

        updateAnimations(Collections.singleton(new StatusReplyPacket.InteractiveAnimation(
                String.valueOf(UUID.randomUUID()),
                "test-animation",
                "media-optical-cd-audio-symbolic",
                false
        )));

        super.present();
    }
}

/*
<attributes>
  <attribute end="-1" name="scale" value="2"/>
</attributes>
 */
