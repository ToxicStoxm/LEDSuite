package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.status_reply.FileState;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.status_reply.StatusReplyPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.AnimationDeleteRequestPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.RenameRequestPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.SettingsChangeRequestPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.StatusRequestPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.media_request.PauseRequestPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.media_request.PlayRequestPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.media_request.StopRequestPacket;
import com.toxicstoxm.LEDSuite.formatting.StringFormatter;
import com.toxicstoxm.LEDSuite.gettext.Translations;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import com.toxicstoxm.LEDSuite.time.CooldownManager;
import com.toxicstoxm.LEDSuite.ui.animation_menu.AnimationMenu;
import com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs.FileCollisionDialog;
import com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs.OverwriteConfirmationDialog;
import com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs.RenameDialog;
import com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog.ServerState;
import com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog.SettingsData;
import com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog.SettingsDialog;
import com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog.SettingsUpdate;
import com.toxicstoxm.LEDSuite.ui.dialogs.status_dialog.StatusDialog;
import com.toxicstoxm.LEDSuite.ui.dialogs.status_dialog.StatusUpdate;
import com.toxicstoxm.YAJL.Logger;
import com.toxicstoxm.YAJL.placeholders.LogMessagePlaceholder;
import io.github.jwharm.javagi.gobject.annotations.InstanceInit;
import io.github.jwharm.javagi.gtk.annotations.GtkCallback;
import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.TemplateTypes;
import lombok.Getter;
import lombok.Setter;
import org.gnome.adw.*;
import org.gnome.adw.AboutDialog;
import org.gnome.adw.AlertDialog;
import org.gnome.adw.Application;
import org.gnome.adw.ApplicationWindow;
import org.gnome.adw.Dialog;
import org.gnome.glib.GLib;
import org.gnome.gobject.GObject;
import org.gnome.gtk.*;
import org.jetbrains.annotations.NotNull;

import java.lang.foreign.MemorySegment;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Main application window class. Holds and manages all UI related elements.
 * <br>Template file: {@code LEDSuiteWindow.ui}
 * @since 1.0
 */
@GtkTemplate(name = "LEDSuiteWindow", ui = "/com/toxicstoxm/LEDSuite/LEDSuiteWindow.ui")
public class LEDSuiteWindow extends ApplicationWindow implements MainWindow {

    private static final Logger logger = Logger.autoConfigureLogger();

    static {
        TemplateTypes.register(LEDSuiteWindow.class);
    }

    public LEDSuiteWindow(MemorySegment address) {
        super(address);
        endpointProvider = EndpointProvider.builder().build();
    }

    public static LEDSuiteWindow create(Application app) {
        return GObject.newInstance(LEDSuiteWindow.class,
                "application", app);
    }

    @InstanceInit
    public void init() {
        // Sorts by last accessed
        animationList.setSortFunc((row1, row2) ->
                (row1 instanceof AnimationRow animationRow1 && row2 instanceof AnimationRow animationRow2) ?
                        Long.compare(animationRow1.getLastAccessed(), animationRow2.getLastAccessed()) * -1 : 0
        );
    }

    private final EndpointProvider endpointProvider;

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

    public void toggleSidebar() {
        if (sideBarBreakpointState) split_view.setShowSidebar(!split_view.getShowSidebar());
    }

    public void displayAboutDialog() {
        Dialog aboutDialog = AboutDialog.fromAppdata("/com/toxicstoxm/LEDSuite/com.toxicstoxm.LEDSuite.metainfo.xml", LEDSuiteApplication.version);
        aboutDialog.present(this);
    }

    @GtkChild(name = "shortcuts_dialog")
    public ShortcutsWindow shortcutsDialog;

    public void displayShortcutsWindow() {
        shortcutsDialog.present();
    }

    /**
     * Displays the status dialog if it isn't already open.
     */
    public void displayStatusDialog() {
        if (!endpointProvider.isStatusDialogEndpointConnected()) {
            var statusDialog = StatusDialog.create();
            endpointProvider.connectStatusDialogEndpoint(statusDialog);
            statusDialog.onClosed(endpointProvider::disconnectStatusDialogEndpoint);
            statusDialog.present(this);
            logger.info("Opened new status dialog!");
        } else {
            logger.info("Couldn't open status dialog because it is already open!");
        }
    }

    /**
     * Displays the preference dialog if it isn't already open.
     */
    public void displayPreferencesDialog() {
        if (!endpointProvider.isSettingsDialogEndpointConnected()) {
            var settingsDialog = SettingsDialog.create();
            endpointProvider.connectSettingsDialogEndpoint(settingsDialog);
            settingsDialog.onClosed(endpointProvider::disconnectSettingsDialogEndpoint);
            settingsDialog.present(this);
            logger.info("Opened new settings dialog!");
        } else {
            logger.info("Couldn't open settings dialog because it is already open!");
        }
    }

    public void settingsDialogApply() {
        logger.debug("Requesting settings change.");
        LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                SettingsChangeRequestPacket
                        .fromSettingsData(getData())
                        .serialize()
        );
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
        logger.verbose("Changing main view to: {}", StringFormatter.getClassName(newChild.getClass()));
        clearMainContent();
        GLib.idleAddOnce(() -> contentBox.append(newChild));
    }

    /**
     * Clears the main window content box.
     *
     * @see #changeMainContent(Widget)
     * @return atomic boolean reference which will remain {@code false} until clear is complete.
     */
    public AtomicBoolean clearMainContent() {
        AtomicBoolean awaitClear = new AtomicBoolean(false);
        GLib.idleAddOnce(() -> {
            Widget child = contentBox.getFirstChild();
            while (child != null) {
                logger.verbose("Removed {} from main view!", StringFormatter.getClassName(child.getClass()));

                contentBox.remove(child);

                if (child instanceof AnimationMenu menu && animations.get(menu.getMenuID()) != null) {
                    animations.get(menu.getMenuID()).setAnimationMenuReference(null);
                }
                
                child.runDispose();
                child.emitDestroy();
                child = contentBox.getFirstChild();
            }
            awaitClear.set(true);
        });
        return awaitClear;
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
                selectedRow.setAnimationMenuReference(animationMenu);
                changeMainContent(animationMenu);
                setAnimationControlButtonsVisible(true);
                setAnimationControlButtonsSensitive(false);
                LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                        StatusRequestPacket.builder().build().serialize()
                );
                logger.verbose("Displaying animation menu with id '{}'!", animationID);
            }
        } else {
            logger.debug("Canceled display attempt for animation menu with animation id '{}'!", animationID);
        }
    }

    @GtkChild(name = "animation_list")
    public ListBox animationList;

    public void setAnimationListSensitive(boolean sensitive) {
        if (animationList != null) animationList.setSensitive(sensitive);
    }

    private final HashMap<String, AnimationRow> animations = new HashMap<>();

    private final HashMap<String, String> renamedAnimations = new HashMap<>();

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
    public void uploadPageSelect() {
        logger.info("Upload files page selected!");
        UploadPage uploadPage = UploadPage.create(this);
        endpointProvider.connectUploadPageEndpoint(uploadPage);
        setAnimationControlButtonsVisible(false);
        changeMainContent(uploadPage);
        GLib.idleAddOnce(() -> {
            if (uploadProgressBarRevealer.getChildRevealed()) {
                setUploadButtonActive(true);
            }
            animationList.setSelectionMode(SelectionMode.NONE);
            animationList.setSelectionMode(SelectionMode.BROWSE);
        });
    }

    public void showAnimationListSpinner(boolean show) {
        animationListSpinnerRevealer.setRevealChild(show);
    }

    @Override
    public ApplicationWindow asApplicationWindow() {
        return this;
    }

    public void setServerConnected(boolean serverConnected) {
        if (!serverConnected) {
            LEDSuiteApplication.getWindow().uploadPageSelect();
        }
        GLib.idleAddOnce(() -> {
            animationList.removeAll();
            animations.clear();
            animationList.setSensitive(serverConnected);
            animationGroupTitle.setSensitive(serverConnected);
            if (!serverConnected) showAnimationListSpinner(false);
            setServerState(serverConnected ? ServerState.CONNECTED : ServerState.DISCONNECTED);
        });
    }

    @Override
    public void setUploadStatistics(UploadStatistics uploadStatistics) {
        endpointProvider.getUploadPageEndpoint().setUploadStatistics(uploadStatistics);
    }

    @Override
    public void setUploadButtonActive(boolean active) {
        endpointProvider.getUploadPageEndpoint().setUploadButtonActive(active);
    }

    @Override
    public void uploadCompleted(boolean success) {
        endpointProvider.getUploadPageEndpoint().uploadCompleted(success);
    }

    /**
     * Updates the available animation list in the sidebar of the application.
     *
     * @param updatedAnimations new available animation list to display
     */
    public void updateAnimations(@NotNull Collection<StatusReplyPacket.Animation> updatedAnimations) {
        logger.verbose("Updating available animations. Count: {}", updatedAnimations.size());

        List<String> removedAnimations = new ArrayList<>(animations.keySet());
        AtomicReference<String> recognized = new AtomicReference<>("");

        GLib.idleAddOnce(() -> {
            for (StatusReplyPacket.Animation updatedAnimation : updatedAnimations) {
                String newAnimationName = updatedAnimation.id();
                removedAnimations.remove(newAnimationName);

                // Check if this animation already exists
                if (animations.containsKey(newAnimationName)) {
                    AnimationRow animationRow = animations.get(newAnimationName);

                    // Update the animation row with new data
                    animationRow.update(updatedAnimation.label(), updatedAnimation.iconString(), updatedAnimation.iconIsName(), updatedAnimation.lastAccessed());
                    logger.verbose("Updated animation: {}", newAnimationName);

                    // Update the animation map to ensure consistency
                    animations.put(newAnimationName, animationRow);

                    // Handle additional update if this row is selected
                    Widget selectedRow = animationList.getSelectedRow();
                    if (selectedRow != null && selectedRow.equals(animationRow)) {
                        logger.verbose("Animation is currently selected: {}", newAnimationName);
                    }
                } else {
                    // Add new animation row if it doesn't already exist
                    var newAnimationRow = AnimationRow.create(
                            AnimationRowData.builder()
                                    .app(getApplication())
                                    .iconString(updatedAnimation.iconString())
                                    .iconIsName(updatedAnimation.iconIsName())
                                    .label(updatedAnimation.label())
                                    .animationID(updatedAnimation.id())
                                    .cooldown(500L)
                                    .lastAccessed(updatedAnimation.lastAccessed())
                                    .pauseable(updatedAnimation.pauseable())
                                    .build()
                    );
                    animations.put(newAnimationName, newAnimationRow);

                    animationList.append(newAnimationRow);
                    logger.verbose("Added animation: {}", newAnimationName);
                }
            }

            // Remove any animations that are no longer in the updated list and resort animation rows
            for (String removedAnimation : removedAnimations) {
                if (Objects.equals(removedAnimation, selectedAnimation)) {
                    AnimationRow animationRow = animations.get(removedAnimation);
                    if (animationRow != null && animationRow.isAncestor(animationList)) {
                        animationList.unselectRow(animationRow);
                        uploadPageSelect();
                    }
                }

                removeAction(removedAnimation);

                var widget = animations.remove(removedAnimation);
                if (widget != null && widget.isAncestor(animationList)) {
                    animationList.remove(widget);
                }

                logger.verbose("Removed animation: {}", removedAnimation);

                // Checks if the removed animation is the old name of a renamed animation
                if (removedAnimations.contains(removedAnimation)) {
                    recognized.set(removedAnimation);
                }
            }

            // Resort the animation list
            animationList.invalidateSort();

            // If any old, renamed animations were recognized tries to find the new renamed animation and selects it.
            if (!recognized.get().isBlank()) {
                logger.debug("Recognized old animation a rename was requested for, searching for renamed animation.");
                String toSelect = renamedAnimations.remove(recognized.get());
                if (selectedAnimation == null || selectedAnimation.isBlank() || selectedAnimation.equals(recognized.get())) {
                    logger.debug("Found renamed animation '{}'! Selecting it...", toSelect);
                    AnimationRow animationRow = animations.get(toSelect);
                    if (animationRow != null) {
                        GLib.idleAddOnce(() -> {
                            CooldownManager.clearCooldown(toSelect);
                            animationRow.emitActivate();
                        });
                    }
                }
            }
        });
    }

    @GtkChild(name = "upload_progress_bar_revealer")
    public Revealer uploadProgressBarRevealer;

    @GtkChild(name = "upload_progress_bar")
    public ProgressBar uploadProgressBar;

    public void setUploadProgress(double fraction) {
        logger.verbose("Updating UI to -> Upload progress: {}", (LogMessagePlaceholder) () -> Math.round(fraction * 100000) / 1000);
        GLib.idleAddOnce(() -> {
            if (!uploadProgressBarRevealer.getChildRevealed()) {
                resetUploadProgress();
                uploadProgressBarRevealer.setRevealChild(true);
            }
            uploadProgressBar.setFraction(fraction);
        });
    }

    public void uploadFinished() {
        logger.debug("Updating UI to -> Upload progress finished.");
        GLib.idleAddOnce(() -> uploadProgressBar.setFraction(1.0));

        new LEDSuiteRunnable() {
            @Override
            public void run() {
                GLib.idleAddOnce(() -> uploadProgressBarRevealer.setRevealChild(false));
            }
        }.runTaskLaterAsynchronously(1000);

    }

    public void resetUploadProgress() {
        logger.debug("Updating UI to -> Upload progress reset.");
        uploadProgressBar.setFraction(0.0);
    }

    @GtkChild(name = "animation_control_buttons_revealer")
    public Revealer animationControlButtonsRevealer;

    @GtkChild(name = "play_pause_button_revealer")
    public Revealer playPauseButtonRevealer;

    @GtkChild(name = "play_pause_button")
    public Button playPauseButton;

    @GtkChild(name = "stop_button")
    public Button stopButton;

    private boolean playing = false;

    @GtkCallback(name = "play_pause_button_cb")
    public void playPauseButtonClicked() {
        logger.debug("{}-button clicked.", playing ? "Pause" : "Play");
        playPauseButton.setSensitive(false);
        stopButton.setSensitive(false);
        if (playing) {
            logger.debug("Requesting animation-pause.");
            LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                    PauseRequestPacket.builder()
                            .requestFile(getSelectedAnimation())
                            .build().serialize()
            );
        } else {
            logger.debug("Requesting animation-play.");
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
        logger.debug("Stop-button clicked.");
        logger.debug("Requesting animation-stop.");
        LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                StopRequestPacket.builder()
                        .requestFile(getSelectedAnimation())
                        .build().serialize()
        );
    }

    public void setAnimationControlButtonsState(@NotNull FileState state, String currentAnimation) {
        if (selectedAnimation != null) {
            if (selectedAnimation.equals(currentAnimation)) {
                playPauseButton.setSensitive(true);
                stopButton.setSensitive(true);
                switch (state) {
                    case playing -> {
                        playing = true;
                        stopButtonRevealer.setRevealChild(true);
                        logger.debug("Updating UI to -> animation-playing.");
                        if (animationList.getSelectedRow() instanceof AnimationRow row) {
                            if (row.isPauseable()) {
                                playPauseButton.setIconName("media-playback-pause");
                            } else {
                                playPauseButtonRevealer.setRevealChild(false);
                            }
                        } else {
                            playPauseButton.setIconName("media-playback-pause");
                        }
                    }
                    case paused -> {
                        logger.debug("Updating UI to -> animation-paused.");
                        playing = false;
                        stopButtonRevealer.setRevealChild(true);
                        playPauseButton.setIconName("media-playback-start");
                    }
                    case idle -> {
                        logger.debug("Updating UI to -> animation-idle.");
                        resetAnimationControlButtons(false);
                    }
                }
            } else {
                logger.debug("Cancelled -> Updating UI to: current animation is not selected.");
                resetAnimationControlButtons(false);
                setAnimationControlButtonsSensitive(true);
            }
        }
    }

    public void setAnimationControlButtonsVisible(boolean visible) {
        logger.debug("Updating UI to -> animation-ctrl-buttons-visible: '{}'", visible);
        if (visible) {
            resetAnimationControlButtons(false);
            GLib.idleAddOnce(() -> animationControlButtonsRevealer.setRevealChild(true));
        } else {
            resetAnimationControlButtons(true);
        }
    }

    public void resetAnimationControlButtons(boolean hide) {
        logger.debug("Resetting animation control buttons");
        playing = false;
        GLib.idleAddOnce(() -> {
            if (hide) animationControlButtonsRevealer.setRevealChild(false);
            stopButtonRevealer.setRevealChild(false);
            playPauseButtonRevealer.setRevealChild(true);
            playPauseButton.setIconName("media-playback-start");
        });
    }

    public void setAnimationControlButtonsSensitive(boolean sensitive) {
        logger.debug("Updating UI to -> animation-ctrl-buttons-sensitive: '{}'", sensitive);
        stopButton.setSensitive(sensitive);
        playPauseButton.setSensitive(sensitive);
    }

    public void displayFileCollisionDialog(AlertDialog.ResponseCallback cb, String body) {
        logger.debug("Updating UI to -> file-collision-dialog-display.");
        var fileCollisionDialog = FileCollisionDialog.create();
        fileCollisionDialog.onResponse(cb);
        fileCollisionDialog.setBody(body);
        GLib.idleAddOnce(() -> fileCollisionDialog.present(this));
    }

    @GtkCallback(name = "delete_button_cb")
    public void deleteButtonClicked() {
        logger.debug("Delete-button clicked.");
        String animation = selectedAnimation;
        if (animation != null && !animation.isBlank()) {
            var deleteConfirmDialog = OverwriteConfirmationDialog.create();
            deleteConfirmDialog.setHeading(Translations.getText("Confirm deletion"));
            deleteConfirmDialog.setBody(Translations.getText("Are you sure that you want to delete '$'?", animation));
            deleteConfirmDialog.setResponseLabel("overwrite", Translations.getText("Delete"));
            deleteConfirmDialog.onResponse(response -> {
                if (Objects.equals(response, "overwrite")) {
                    logger.debug("Sending delete-request for current animation '{}'.", animation);
                    LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                            AnimationDeleteRequestPacket.builder()
                                    .fileName(animation)
                                    .build().serialize()
                    );
                } else {
                    logger.info("Cancelled animation deletion!");
                }
            });
            deleteConfirmDialog.present(this);
        } else {
            logger.warn("Can't delete animation, because no animation is currently selected!");
        }
    }

    @GtkCallback(name = "rename_button_cb")
    public void renameButtonClicked() {
        logger.debug("Rename-button clicked.");
        String animation = selectedAnimation;
        if (animation != null && !animation.isBlank()) {
            var renameDialog = RenameDialog.create(animation);
            renameDialog.onResponse(response ->  {
                if (Objects.equals(response, "rename")) {
                    String newName = renameDialog.getNewName();
                    if (newName != null && !newName.isBlank()) {
                        logger.debug("Sending rename-request for current animation '{}'", animation);
                        LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                                RenameRequestPacket.builder()
                                        .requestFile(animation)
                                        .newName(newName)
                                        .build().serialize()
                        );
                        renamedAnimations.put(animation, newName);
                    }
                } else {
                    animations.get(selectedAnimation).setRenamePending(false);
                    logger.info("Cancelled animation rename!");
                }
            });
            animations.get(selectedAnimation).setRenamePending(true);
            renameDialog.present(this);
        } else {
            logger.warn("Can't rename animation, because no animation is currently selected!");
        }
    }

    @Override
    public void update(SettingsUpdate settingsUpdate) {
        endpointProvider.getSettingsDialogInstance().update(settingsUpdate);
    }

    @Override
    public void applyButtonCooldown() {
        endpointProvider.getSettingsDialogInstance().applyButtonCooldown();
    }

    @Override
    public SettingsData getData() {
        return endpointProvider.getSettingsDialogInstance().getData();
    }

    @Override
    public void setAuthenticating() {
        endpointProvider.getSettingsDialogInstance().setAuthenticating();
    }

    @Override
    public void setAuthenticated(boolean authenticated, String username) {
        endpointProvider.getSettingsDialogInstance().setAuthenticated(authenticated, username);
    }

    @Override
    public void setServerState(@NotNull ServerState serverState) {
        endpointProvider.getSettingsDialogInstance().setServerState(serverState);
    }

    @Override
    public void update(StatusUpdate statusUpdate) {
        endpointProvider.getStatusDialogInstance().update(statusUpdate);
    }

    @Override
    public boolean checkRenamePending(String fileName) {
        return !animations.containsKey(fileName) || animations.get(fileName).getRenamePending();
    }
}