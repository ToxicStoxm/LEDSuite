package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.status_reply.FileState;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.status_reply.StatusReplyPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.AnimationDeleteRequestPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.RenameRequestPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.SettingsChangeRequestPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.media_request.PauseRequestPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.media_request.PlayRequestPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.media_request.StopRequestPacket;
import com.toxicstoxm.LEDSuite.formatting.StringFormatter;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import com.toxicstoxm.LEDSuite.time.Action;
import com.toxicstoxm.LEDSuite.ui.animation_menu.AnimationMenu;
import com.toxicstoxm.LEDSuite.ui.dialogs.ProviderCallback;
import com.toxicstoxm.LEDSuite.ui.dialogs.UpdateCallback;
import com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialog.FileCollisionDialog;
import com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialog.OverwriteConfirmationDialog;
import com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialog.RenameDialog;
import com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog.*;
import com.toxicstoxm.LEDSuite.ui.dialogs.status_dialog.StatusDialog;
import com.toxicstoxm.LEDSuite.ui.dialogs.status_dialog.StatusDialogEndpoint;
import io.github.jwharm.javagi.gtk.annotations.GtkCallback;
import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.TemplateTypes;
import lombok.Getter;
import lombok.Setter;
import org.gnome.adw.AboutDialog;
import org.gnome.adw.AlertDialog;
import org.gnome.adw.Application;
import org.gnome.adw.ApplicationWindow;
import org.gnome.adw.*;
import org.gnome.glib.GLib;
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
        } else
            LEDSuiteApplication.getLogger().info("Couldn't open status dialog because it is already open!", new LEDSuiteLogAreas.USER_INTERACTIONS());
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
        } else {
            LEDSuiteApplication.getLogger().info("Couldn't open settings dialog because it is already open!", new LEDSuiteLogAreas.USER_INTERACTIONS());
        }
    }

    public void settingsDialogApply() {
        if (settingsDialog != null) {
            LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                    SettingsChangeRequestPacket
                            .fromSettingsData(settingsDialog.settingsManager().getData())
                            .serialize()
            );
            LEDSuiteApplication.getLogger().info("Applied settings and send changes to server!", new LEDSuiteLogAreas.USER_INTERACTIONS());
        } else {
            LEDSuiteApplication.getLogger().warn("Couldn't apply settings because no settings data provider callback was found!", new LEDSuiteLogAreas.USER_INTERACTIONS());
        }
    }

    public void settingsDialogApplyFail() {
        if (settingsDialog != null) {
            settingsDialog.applyButtonCooldown().run();
            LEDSuiteApplication.getLogger().info("Settings dialog apply failed, on cooldown!", new LEDSuiteLogAreas.USER_INTERACTIONS());
        } else {
            LEDSuiteApplication.getLogger().info("Couldn't fail settings dialog apply, apply fail callback missing!", new LEDSuiteLogAreas.USER_INTERACTIONS());
        }
    }

    @GtkChild(name = "content-box-revealer")
    public Revealer contentBoxRevealer;

    @GtkChild(name = "content-box")
    public Box contentBox;

    /**
     * Clears the main window content box and display the specified object instead.
     *
     * @param newChild the new object to display
     * @see #clearMainContent()
     */
    public void changeMainContent(@NotNull Widget newChild) {
        LEDSuiteApplication.getLogger().info("Changing main view to: " + StringFormatter.getClassName(newChild.getClass()), new LEDSuiteLogAreas.UI());
        clearMainContent();
        GLib.idleAddOnce(() -> contentBox.append(newChild));
    }

    /**
     * Clears the main window content box.
     *
     * @see #changeMainContent(Widget)
     */
    public void clearMainContent() {
        GLib.idleAddOnce(() -> {
            Widget child = contentBox.getFirstChild();
            while (child != null) {
                LEDSuiteApplication.getLogger().info("Removed " + StringFormatter.getClassName(child.getClass()) + " from main view!", new LEDSuiteLogAreas.UI());
                contentBox.remove(child);
                child = contentBox.getFirstChild();
            }
        });
    }

    /**
     * Displays the provided animation menu if the corresponding animation (by id) is currently selected.
     *
     * @param menu the menu to display
     */
    public void displayAnimationMenu(@NotNull AnimationMenu menu) {
        String animationID = menu.getMenuID();
        if (Objects.equals(animationID, selectedAnimation)) {
            AnimationRow selectedRow = (AnimationRow) animationList.getSelectedRow();
            if (selectedRow != null) {
                AnimationMenu animationMenu = menu.init(selectedRow);
                selectedRow.setMenuReference(animationMenu);
                changeMainContent(animationMenu);
                setAnimationControlButtonsVisible(true);
                LEDSuiteApplication.getLogger().verbose("Displaying animation menu with id '" + animationID + "'!", new LEDSuiteLogAreas.UI());
            }
        } else {
            LEDSuiteApplication.getLogger().debug("Canceled display attempt for animation menu with animation id '" + animationID + "'!", new LEDSuiteLogAreas.UI());
        }
    }

    @GtkChild(name = "animation_list")
    public ListBox animationList;

    private final HashMap<String, AnimationRow> animations = new HashMap<>();

    @Getter
    @Setter
    private String selectedAnimation;

    @GtkChild(name = "file_management_list")
    public ListBox fileManagementList;

    @GtkChild(name = "file_management_upload_files_page")
    public ListBoxRow fileManagementUploadFilesPage;

    @GtkChild(name = "sidebar_animation_group_title")
    public Label animationGroupTitle;

    @GtkChild(name = "animation_list_spinner_revealer")
    public Revealer animationListSpinnerRevealer;

    @GtkChild(name = "main_view_overlay")
    public Overlay mainViewOverlay;

    @Getter
    private UploadPageEndpoint uploadPageEndpoint;

    public void uploadPageSelect() {
        LEDSuiteApplication.getLogger().info("Upload files page selected!", new LEDSuiteLogAreas.USER_INTERACTIONS());
        UploadPage uploadPage = UploadPage.create(this);
        uploadPageEndpoint = new UploadPageEndpoint() {
            @Override
            public UpdateCallback<Boolean> connectivityUpdater() {
                return uploadPage.getConnectivityUpdater();
            }

            @Override
            public UpdateCallback<UploadStatistics> uploadStatisticsUpdater() {
                return uploadPage.getUploadStatisticsUpdater();
            }

            @Override
            public UpdateCallback<Boolean> uploadButtonState() {
                return uploadPage.getUploadButtonState();
            }

            @Override
            public UpdateCallback<Boolean> uploadSuccessCallback() {
                return uploadPage.getUploadCallback();
            }
        };
        setAnimationControlButtonsVisible(false);
        changeMainContent(uploadPage);
        GLib.idleAddOnce(() -> {
            if (uploadProgressBarRevealer.getChildRevealed()) {
                uploadPageEndpoint.uploadButtonState().update(true);
            }
            animationList.setSelectionMode(SelectionMode.NONE);
            animationList.setSelectionMode(SelectionMode.BROWSE);
        });
    }

    public void showAnimationListSpinner(boolean show) {
        animationListSpinnerRevealer.setRevealChild(show);
    }

    public void setServerConnected(boolean serverConnected) {
        if (!serverConnected) LEDSuiteApplication.getWindow().uploadPageSelect();
        GLib.idleAddOnce(() -> {
            animationList.setSensitive(serverConnected);
            animationGroupTitle.setSensitive(serverConnected);
            if (!serverConnected) showAnimationListSpinner(false);
            if (uploadPageEndpoint != null) uploadPageEndpoint.connectivityUpdater().update(serverConnected);
        });
    }

    /**
     * Updates the available animation list in the sidebar of the application.
     *
     * @param updatedAnimations new available animation list to display
     */
    public void updateAnimations(@NotNull Collection<StatusReplyPacket.Animation> updatedAnimations) {
        LEDSuiteApplication.getLogger().verbose("Updating available animations. Count: " + updatedAnimations.size(), new LEDSuiteLogAreas.UI());

        List<String> removedAnimations = new ArrayList<>(animations.keySet());

        for (StatusReplyPacket.Animation updatedAnimation : updatedAnimations) {
            String newAnimationName = updatedAnimation.id();
            removedAnimations.remove(newAnimationName);

            // Check if this animation already exists
            if (animations.containsKey(newAnimationName)) {
                AnimationRow animationRow = animations.get(newAnimationName);

                // Update the animation row with new data
                GLib.idleAddOnce(() -> {
                    animationRow.update(updatedAnimation.label(), updatedAnimation.iconName());
                    LEDSuiteApplication.getLogger().verbose("Updated animation: " + newAnimationName, new LEDSuiteLogAreas.UI());
                });

                // Update the animations map to ensure consistency
                animations.put(newAnimationName, animationRow);

                // Handle additional update if this row is selected
                Widget selectedRow = animationList.getSelectedRow();
                if (selectedRow != null && selectedRow.equals(animationRow)) {
                    LEDSuiteApplication.getLogger().verbose("Animation is currently selected: " + newAnimationName, new LEDSuiteLogAreas.UI());
                }
            } else {
                // Add new animation row if it doesn't already exist
                var newAnimationRow = AnimationRow.create(
                        AnimationRowData.builder()
                                .app(getApplication())
                                .iconName(updatedAnimation.iconName())
                                .label(updatedAnimation.label())
                                .animationID(updatedAnimation.id())
                                .cooldown(500L)
                                .build()
                );
                animations.put(newAnimationName, newAnimationRow);

                GLib.idleAddOnce(() -> {
                    animationList.append(newAnimationRow);
                    LEDSuiteApplication.getLogger().verbose("Added animation: " + newAnimationName, new LEDSuiteLogAreas.UI());
                });
            }
        }

        // Remove any animations that are no longer in the updated list
        GLib.idleAddOnce(() -> {
            for (String removedAnimation : removedAnimations) {
                if (Objects.equals(removedAnimation, selectedAnimation)) {
                    animationList.unselectRow(animations.get(removedAnimation));
                    uploadPageSelect();
                }

                removeAction(removedAnimation);

                var widget = animations.remove(removedAnimation);
                if (widget != null && widget.isAncestor(animationList)) {
                    animationList.remove(widget);
                }

                LEDSuiteApplication.getLogger().verbose("Removed animation: " + removedAnimation, new LEDSuiteLogAreas.UI());
            }
        });
    }

    @GtkChild(name = "upload_progress_bar_revealer")
    public Revealer uploadProgressBarRevealer;

    @GtkChild(name = "upload_progress_bar")
    public ProgressBar uploadProgressBar;

    public void setUploadProgress(double fraction) {
        GLib.idleAddOnce(() -> {
            if (!uploadProgressBarRevealer.getChildRevealed()) {
                resetUploadProgress();
                uploadProgressBarRevealer.setRevealChild(true);
            }
            uploadProgressBar.setFraction(fraction);
        });
    }

    public void uploadFinished() {
        GLib.idleAddOnce(() -> uploadProgressBar.setFraction(1.0));

        new LEDSuiteRunnable() {
            @Override
            public void run() {
                GLib.idleAddOnce(() -> uploadProgressBarRevealer.setRevealChild(false));
            }
        }.runTaskLaterAsynchronously(1000);

    }

    public void resetUploadProgress() {
        uploadProgressBar.setFraction(0.0);
    }

    @GtkChild(name = "animation_control_buttons_revealer")
    public Revealer animationControlButtonsRevealer;

    @GtkChild(name = "play_pause_button")
    public Button playButton;

    private boolean playing = false;

    @GtkCallback(name = "play_pause_button_cb")
    public void playPauseButtonClicked() {
        if (playing) {
            GLib.idleAddOnce(() -> {
                playButton.setIconName("media-playback-start");
                stopButtonRevealer.setRevealChild(true);
                playing = false;
            });
            LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                    PauseRequestPacket.builder()
                            .requestFile(getSelectedAnimation())
                            .build().serialize()
            );
        } else {
            playing = true;
            GLib.idleAddOnce(() -> {
                playButton.setIconName("media-playback-pause");
                stopButtonRevealer.setRevealChild(true);
            });
            LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                    PlayRequestPacket.builder()
                            .requestFile(getSelectedAnimation())
                            .build().serialize()
            );
        }
    }

    @GtkChild(name = "stop_button_revealer")
    public Revealer stopButtonRevealer;

    @GtkCallback(name = "stop_button_cb")
    public void stopButtonClicked() {
        resetAnimationControlButtons(false);
        LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                StopRequestPacket.builder()
                        .requestFile(getSelectedAnimation())
                        .build().serialize()
        );
    }

    public void setAnimationControlButtonsState(@NotNull FileState state) {
        if (selectedAnimation != null) {
            switch (state) {
                case playing -> {
                    playing = true;
                    stopButtonRevealer.setRevealChild(true);
                    playButton.setIconName("media-playback-pause");
                }
                case paused -> {
                    playing = false;
                    stopButtonRevealer.setRevealChild(true);
                    playButton.setIconName("media-playback-start");
                }
                case idle -> resetAnimationControlButtons(false);
            }
        }
    }

    public void setAnimationControlButtonsVisible(boolean visible) {
        if (visible) {
            resetAnimationControlButtons(false);
            GLib.idleAddOnce(() -> animationControlButtonsRevealer.setRevealChild(true));
        } else {
            resetAnimationControlButtons(true);
        }
    }

    public void resetAnimationControlButtons(boolean hide) {
        playing = false;
        GLib.idleAddOnce(() -> {
            if (hide) animationControlButtonsRevealer.setRevealChild(false);
            stopButtonRevealer.setRevealChild(false);
            playButton.setIconName("media-playback-start");
        });
    }

    public void displayFileCollisionDialog(AlertDialog.ResponseCallback cb, String body) {
        var fileCollisionDialog = FileCollisionDialog.create();
        fileCollisionDialog.onResponse(cb);
        fileCollisionDialog.setBody(body);
        GLib.idleAddOnce(() -> fileCollisionDialog.present(this));
    }

    @GtkCallback(name = "delete_button_cb")
    public void deleteButtonClicked() {
        String animation = selectedAnimation;
        if (animation != null && !animation.isBlank()) {
            var deleteConfirmDialog = OverwriteConfirmationDialog.create();
            deleteConfirmDialog.setHeading("Confirm deletion");
            deleteConfirmDialog.setBody("Are you sure that you want to delete '" + animation + "'?");
            deleteConfirmDialog.setResponseLabel("overwrite", "Delete");
            deleteConfirmDialog.onResponse(response -> {
                if (Objects.equals(response, "overwrite")) {
                    LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                            AnimationDeleteRequestPacket.builder()
                                    .fileName(animation)
                                    .build().serialize()
                    );
                } else {
                    LEDSuiteApplication.getLogger().info("Cancelled animation deletion!", new LEDSuiteLogAreas.USER_INTERACTIONS());
                }
            });
            deleteConfirmDialog.present(this);
        } else {
            LEDSuiteApplication.getLogger().warn("Can't delete animation, because no animation is currently selected!", new LEDSuiteLogAreas.USER_INTERACTIONS());
        }
    }

    @GtkCallback(name = "rename_button_cb")
    public void renameButtonClicked() {
        String animation = selectedAnimation;
        if (animation != null && !animation.isBlank()) {
            var renameDialog = RenameDialog.create(animation);
            renameDialog.onResponse(response ->  {
                if (Objects.equals(response, "rename")) {
                    String newName = renameDialog.getNewName();
                    if (newName != null && !newName.isBlank()) {
                        LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                                RenameRequestPacket.builder()
                                        .requestFile(animation)
                                        .newName(newName)
                                        .build().serialize()
                        );
                    }
                } else {
                    LEDSuiteApplication.getLogger().info("Cancelled animation rename!", new LEDSuiteLogAreas.USER_INTERACTIONS());
                }
            });
            renameDialog.present(this);
        } else {
            LEDSuiteApplication.getLogger().warn("Can't rename animation, because no animation is currently selected!", new LEDSuiteLogAreas.USER_INTERACTIONS());
        }
    }
}