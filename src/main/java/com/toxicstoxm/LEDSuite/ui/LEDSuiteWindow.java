package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.status_reply.StatusReplyPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.*;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.media_request.PauseRequestPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.media_request.PlayRequestPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.media_request.StopRequestPacket;
import com.toxicstoxm.LEDSuite.communication.websocket.WebSocketClient;
import com.toxicstoxm.LEDSuite.formatting.StringFormatter;
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
import lombok.Setter;
import org.gnome.adw.AboutDialog;
import org.gnome.adw.Application;
import org.gnome.adw.ApplicationWindow;
import org.gnome.adw.OverlaySplitView;
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
     * @param menu the menu to display
     */
    public void displayAnimationManu(@NotNull AnimationMenu menu) {
        String animationID = menu.getMenuID();
        if (Objects.equals(animationID, selectedAnimation)) {
            changeMainContent(menu.init((AnimationRow) animationList.getSelectedRow()));
            setAnimationControlButtonsVisible(true);
            LEDSuiteApplication.getLogger().verbose("Displaying animation menu with id '" + animationID + "'!", new LEDSuiteLogAreas.UI());
        } else LEDSuiteApplication.getLogger().debug("Canceled display attempt for animation menu with animation id '" + animationID + "'!", new LEDSuiteLogAreas.UI());
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
        };
        setAnimationControlButtonsVisible(false);
        changeMainContent(uploadPage);
        GLib.idleAddOnce(() -> {
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
     * @param updatedAnimations new available animation list to display
     */
    public void updateAnimations(@NotNull Collection<StatusReplyPacket.Animation> updatedAnimations) {
        HashMap<String, AnimationRow> newAnimationRows = new HashMap<>();

        LEDSuiteApplication.getLogger().verbose("Updating available animations. Count: " + updatedAnimations.size(), new LEDSuiteLogAreas.UI());

        // Construct animation rows for all new animations and store them in a temporary map
        for (StatusReplyPacket.Animation updatedAnimation : updatedAnimations) {
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
                if (Objects.equals(selectedAnimation, newAnimation)) {
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

    @Override
    public void present() {

        tests();

        super.present();
    }

    private void tests() {
        animationList.append(AnimationRow.create(
                AnimationRowData.builder()
                        .app(getApplication())
                        .iconName("emoji-food-symbolic")
                        .label("Test status request")
                        .animationID(String.valueOf(UUID.randomUUID()))
                        .action(() -> {
                            clearMainContent();

                            LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                                    StatusRequestPacket.builder().build().serialize()
                            );
                        })
                        .build()
        ));

        String testID = String.valueOf(UUID.randomUUID());

        animationList.append(AnimationRow.create(
                AnimationRowData.builder()
                        .app(getApplication())
                        .iconName("media-optical-cd-audio-symbolic")
                        .label("Test menu reply!")
                        .animationID(testID)
                        .action(() -> {
                            clearMainContent();

                            /*
                            YamlConfiguration yaml = new YamlConfiguration();
                            yaml.set(Constants.Communication.YAML.Keys.Reply.MenuReply.FILENAME, testID);

                            for (int i = 0; i < 10; i++) {
                                String randomID = String.valueOf(UUID.randomUUID());
                                String prefix = Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT + "." + randomID + ".";
                                yaml.set(prefix + Constants.Communication.YAML.Keys.Reply.MenuReply.LABEL, "group-" + i);
                                yaml.set(prefix + Constants.Communication.YAML.Keys.Reply.MenuReply.TOOLTIP, "group-" + i);

                                YamlConfiguration suffixYAML = new YamlConfiguration();

                                suffixYAML.set(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE, WidgetType.BUTTON.getName());
                                suffixYAML.set(Constants.Communication.YAML.Keys.Reply.MenuReply.LABEL, "Header-Suffix-" + i);
                                suffixYAML.set(Constants.Communication.YAML.Keys.Reply.MenuReply.Button.BLOCK_AFTER_CLICKED, true);
                                suffixYAML.set(Constants.Communication.YAML.Keys.Reply.MenuReply.Button.SPIN_ON_CLICKED, true);


                                YamlConfiguration propertyYAML = new YamlConfiguration();

                                propertyYAML.set(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE, WidgetType.PROPERTY_ROW.getName());
                                propertyYAML.set(Constants.Communication.YAML.Keys.Reply.MenuReply.LABEL, "Special-" + i);
                                propertyYAML.set(Constants.Communication.YAML.Keys.Reply.MenuReply.SUBTITLE, "SpecialValue-" + i);

                                yaml.createSection(prefix + Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT + "." + UUID.randomUUID(), propertyYAML.getValues(true));

                                YamlConfiguration entryYAML = new YamlConfiguration();

                                entryYAML.set(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE, WidgetType.ENTRY_ROW.getName());
                                entryYAML.set(Constants.Communication.YAML.Keys.Reply.MenuReply.LABEL, "Entry-" + i);
                                entryYAML.set(Constants.Communication.YAML.Keys.Reply.MenuReply.SUBTITLE, "Placeholder");
                                entryYAML.set(Constants.Communication.YAML.Keys.Reply.MenuReply.EntryRow.APPLY_BUTTON, false);

                                yaml.createSection(prefix + Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT + "." + UUID.randomUUID(), entryYAML.getValues(true));

                                YamlConfiguration spinYAML = new YamlConfiguration();

                                spinYAML.set(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE, WidgetType.SPIN_ROW.getName());
                                spinYAML.set(Constants.Communication.YAML.Keys.Reply.MenuReply.LABEL, "Spin-" + i);
                                spinYAML.set(Constants.Communication.YAML.Keys.Reply.MenuReply.SUBTITLE, "Subtitle");
                                spinYAML.set(Constants.Communication.YAML.Keys.Reply.MenuReply.VALUE, 100.99);
                                spinYAML.set(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.MINIMUM, 0);
                                spinYAML.set(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.MAXIMUM, 500);
                                spinYAML.set(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.DIGITS, 2);
                                spinYAML.set(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.INCREMENT, 1);
                                spinYAML.set(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.PAGE_INCREMENT, 10);
                                spinYAML.set(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.CLIMB_RATE, 2);
                                spinYAML.set(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.SNAP, true);
                                spinYAML.set(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.WRAP, true);
                                spinYAML.set(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.UPDATE_COOLDOWN, 1000);

                                yaml.createSection(prefix + Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT + "." + UUID.randomUUID(), spinYAML.getValues(true));

                                YamlConfiguration buttonRow = new YamlConfiguration();

                                buttonRow.set(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE, WidgetType.BUTTON_ROW.getName());
                                buttonRow.set(Constants.Communication.YAML.Keys.Reply.MenuReply.LABEL, "Button-" + i);
                                buttonRow.set(Constants.Communication.YAML.Keys.Reply.MenuReply.ButtonRow.START_ICON_NAME, "battery-level-0-symbolic");
                                buttonRow.set(Constants.Communication.YAML.Keys.Reply.MenuReply.ButtonRow.END_ICON_NAME, "firefox-symbolic");


                                yaml.createSection(prefix + Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT + "." + UUID.randomUUID(), buttonRow.getValues(true));

                                YamlConfiguration comboRow = new YamlConfiguration();

                                comboRow.set(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE, WidgetType.COMBO_ROW.getName());
                                comboRow.set(Constants.Communication.YAML.Keys.Reply.MenuReply.LABEL, "ComboRow-" + i);
                                comboRow.set(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT, List.of("Test", "Test1", "Test2"));
                                comboRow.set(Constants.Communication.YAML.Keys.Reply.MenuReply.VALUE, "Test1");
                                comboRow.set(Constants.Communication.YAML.Keys.Reply.MenuReply.ComboRow.ENABLE_SEARCH, true);

                                yaml.createSection(prefix + Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT + "." + UUID.randomUUID(), comboRow.getValues(true));

                                YamlConfiguration switchRow = new YamlConfiguration();

                                switchRow.set(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE, WidgetType.SWITCH_ROW.getName());
                                switchRow.set(Constants.Communication.YAML.Keys.Reply.MenuReply.LABEL, "SwitchRow-" + i);
                                switchRow.set(Constants.Communication.YAML.Keys.Reply.MenuReply.VALUE, true);

                                yaml.createSection(prefix + Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT + "." + UUID.randomUUID(), switchRow.getValues(true));

                                YamlConfiguration expanderRow = new YamlConfiguration();

                                expanderRow.set(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE, WidgetType.EXPANDER_ROW.getName());
                                expanderRow.set(Constants.Communication.YAML.Keys.Reply.MenuReply.LABEL, "ExpanderRow-" + i);
                                expanderRow.set(Constants.Communication.YAML.Keys.Reply.MenuReply.ExpanderRow.EXPANDED, true);
                                expanderRow.set(Constants.Communication.YAML.Keys.Reply.MenuReply.ExpanderRow.ENABLE_EXPANSION, true);
                                expanderRow.set(Constants.Communication.YAML.Keys.Reply.MenuReply.ExpanderRow.WITH_SWITCH, true);

                                YamlConfiguration expanderRowContent = new YamlConfiguration();

                                YamlConfiguration propertyYAML1 = new YamlConfiguration();

                                propertyYAML1.set(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE, WidgetType.PROPERTY_ROW.getName());
                                propertyYAML1.set(Constants.Communication.YAML.Keys.Reply.MenuReply.LABEL, "Special-" + i);
                                propertyYAML1.set(Constants.Communication.YAML.Keys.Reply.MenuReply.SUBTITLE, "SpecialValue-" + i);

                                expanderRowContent.createSection(String.valueOf(UUID.randomUUID()), propertyYAML1.getValues(true));

                                YamlConfiguration entryYAML1 = new YamlConfiguration();

                                entryYAML1.set(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE, WidgetType.ENTRY_ROW.getName());
                                entryYAML1.set(Constants.Communication.YAML.Keys.Reply.MenuReply.LABEL, "Entry-" + i);
                                entryYAML1.set(Constants.Communication.YAML.Keys.Reply.MenuReply.SUBTITLE, "Placeholder");
                                entryYAML1.set(Constants.Communication.YAML.Keys.Reply.MenuReply.EntryRow.APPLY_BUTTON, false);

                                expanderRowContent.createSection(String.valueOf(UUID.randomUUID()), entryYAML1.getValues(true));

                                YamlConfiguration spinYAML1 = new YamlConfiguration();

                                spinYAML1.set(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE, WidgetType.SPIN_ROW.getName());
                                spinYAML1.set(Constants.Communication.YAML.Keys.Reply.MenuReply.LABEL, "Spin-" + i);
                                spinYAML1.set(Constants.Communication.YAML.Keys.Reply.MenuReply.SUBTITLE, "Subtitle");
                                spinYAML1.set(Constants.Communication.YAML.Keys.Reply.MenuReply.VALUE, 100.99);
                                spinYAML1.set(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.MINIMUM, 0);
                                spinYAML1.set(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.MAXIMUM, 500);
                                spinYAML1.set(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.DIGITS, 2);
                                spinYAML1.set(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.INCREMENT, 1);
                                spinYAML1.set(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.PAGE_INCREMENT, 10);
                                spinYAML1.set(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.CLIMB_RATE, 2);
                                spinYAML1.set(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.SNAP, true);
                                spinYAML1.set(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.WRAP, true);
                                spinYAML1.set(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.UPDATE_COOLDOWN, 1000);

                                expanderRowContent.createSection(String.valueOf(UUID.randomUUID()), spinYAML1.getValues(true));

                                YamlConfiguration buttonRow1 = new YamlConfiguration();

                                buttonRow1.set(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE, WidgetType.BUTTON_ROW.getName());
                                buttonRow1.set(Constants.Communication.YAML.Keys.Reply.MenuReply.LABEL, "Button-" + i);
                                buttonRow1.set(Constants.Communication.YAML.Keys.Reply.MenuReply.ButtonRow.START_ICON_NAME, "battery-level-0-symbolic");
                                buttonRow1.set(Constants.Communication.YAML.Keys.Reply.MenuReply.ButtonRow.END_ICON_NAME, "firefox-symbolic");


                                expanderRowContent.createSection(String.valueOf(UUID.randomUUID()), buttonRow1.getValues(true));

                                YamlConfiguration comboRow1 = new YamlConfiguration();

                                comboRow1.set(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE, WidgetType.COMBO_ROW.getName());
                                comboRow1.set(Constants.Communication.YAML.Keys.Reply.MenuReply.LABEL, "ComboRow-" + i);
                                comboRow1.set(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT, List.of("Test", "Test1", "Test2"));
                                comboRow1.set(Constants.Communication.YAML.Keys.Reply.MenuReply.VALUE, "Test1");
                                comboRow1.set(Constants.Communication.YAML.Keys.Reply.MenuReply.ComboRow.ENABLE_SEARCH, true);

                                expanderRowContent.createSection(String.valueOf(UUID.randomUUID()), comboRow1.getValues(true));

                                YamlConfiguration switchRow1 = new YamlConfiguration();

                                switchRow1.set(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE, WidgetType.SWITCH_ROW.getName());
                                switchRow1.set(Constants.Communication.YAML.Keys.Reply.MenuReply.LABEL, "SwitchRow-" + i);
                                switchRow1.set(Constants.Communication.YAML.Keys.Reply.MenuReply.VALUE, true);

                                expanderRowContent.createSection(String.valueOf(UUID.randomUUID()), switchRow1.getValues(true));

                                expanderRow.createSection(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT, expanderRowContent.getValues(true));

                                yaml.createSection(prefix + Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT + "." + UUID.randomUUID(), expanderRow.getValues(true));

                                YamlConfiguration button = new YamlConfiguration();
                                button.set(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE, WidgetType.BUTTON.getName());
                                button.set(Constants.Communication.YAML.Keys.Reply.MenuReply.LABEL, "Button-" + i);
                                button.set(Constants.Communication.YAML.Keys.Reply.MenuReply.TOOLTIP, "This is a tooltip");
                                button.set(Constants.Communication.YAML.Keys.Reply.MenuReply.Button.BLOCK_AFTER_CLICKED, true);
                                button.set(Constants.Communication.YAML.Keys.Reply.MenuReply.Button.SPIN_ON_CLICKED, true);

                                yaml.createSection(prefix + Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT + "." + UUID.randomUUID(), button.getValues(true));

                            }

                            new LEDSuiteRunnable() {
                                @Override
                                public void run() {
                                    GLib.idleAddOnce(() -> {
                                        LEDSuiteApplication.getPacketReceivedHandler().handleIncomingPacket(
                                                MenuReplyPacket.builder().menuYAML(yaml.saveToString()).build()
                                        );
                                    });
                                }
                            }.runTaskLaterAsynchronously(50);
                             */
                        })
                        .build()
        ));

        animationList.append(AnimationRow.create(
                AnimationRowData.builder()
                        .app(getApplication())
                        .iconName("media-optical-cd-audio-symbolic")
                        .label("Test media requests!")
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
                        })
                        .build()
        ));

        animationList.append(AnimationRow.create(
                AnimationRowData.builder()
                        .app(getApplication())
                        .iconName("media-optical-cd-audio-symbolic")
                        .label("Test progress bar requests!")
                        .animationID(String.valueOf(UUID.randomUUID()))
                        .action(() -> {
                            clearMainContent();

                            new LEDSuiteRunnable() {
                                @Override
                                public void run() {
                                    for (int i = 0; i < 1000; i++) {
                                        setUploadProgress((double) i / 1000);
                                        try {
                                            Thread.sleep(1);
                                        } catch (InterruptedException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                    uploadFinished();
                                }
                            }.runTaskAsynchronously();
                        })
                        .build()
        ));

        updateAnimations(Collections.singleton(new StatusReplyPacket.Animation(
                String.valueOf(UUID.randomUUID()),
                "test-animation",
                "media-optical-cd-audio-symbolic",
                false
        )));
    }
}

/*
<attributes>
  <attribute end="-1" name="scale" value="2"/>
</attributes>
 */
