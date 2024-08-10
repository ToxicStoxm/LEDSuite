package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.LEDSuite;
import com.toxicstoxm.LEDSuite.event_handling.EventHandler;
import com.toxicstoxm.LEDSuite.event_handling.Events;
import com.toxicstoxm.LEDSuite.event_handling.listener.EventListener;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteGuiRunnable;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteTask;
import com.toxicstoxm.LEDSuite.yaml_factory.wrappers.message_wrappers.StatusUpdate;
import lombok.NonNull;
import org.gnome.adw.*;
import org.gnome.gtk.Box;
import org.gnome.gtk.Label;
import org.gnome.gtk.Orientation;
import org.gnome.gtk.Widget;

/**
 * Represents a status dialog for displaying server status and related information.
 * This dialog is updated with server status information and provides an interface
 * to show the current state of the server.
 *
 * @since 1.0.0
 */
public class StatusDialog extends Dialog implements EventListener {
    private int checksum = 0; // Checksum to track changes in status updates
    private ActionRow currentDraw; // Displays current draw information
    private ActionRow voltage; // Displays voltage information
    private ActionRow currentFile; // Displays current file information
    private ActionRow fileState; // Displays file state information
    private ActionRow lidState; // Displays lid state information
    private StatusPage statusPage; // Page to hold status-related UI elements
    private boolean statusInit = false; // Flag to track if status has been initialized
    private boolean reboot = false; // Flag to indicate if a reboot is necessary
    private long lastUpdate; // Timestamp of the last update

    /**
     * Initializes the status dialog with default UI elements and settings.
     *
     * @since 1.0.0
     */
    private void initialize() {
        // Initialize action rows for different status elements
        currentDraw = ActionRow.builder()
                .setTitle(LEDSuite.i18n("status_dialog_current_draw_title"))
                .setSubtitleSelectable(true)
                .setCssClasses(new String[]{"property"})
                .build();
        voltage = ActionRow.builder()
                .setTitle(LEDSuite.i18n("status_dialog_voltage_title"))
                .setSubtitleSelectable(true)
                .setCssClasses(new String[]{"property"})
                .build();
        currentFile = ActionRow.builder()
                .setTitle(LEDSuite.i18n("status_dialog_current_file_title"))
                .setSubtitleSelectable(true)
                .setCssClasses(new String[]{"property"})
                .build();
        fileState = ActionRow.builder()
                .setTitle(LEDSuite.i18n("status_dialog_file_state_title"))
                .setSubtitleSelectable(true)
                .setCssClasses(new String[]{"property"})
                .build();
        lidState = ActionRow.builder()
                .setTitle(LEDSuite.i18n("status_dialog_lid_state_title"))
                .setSubtitleSelectable(true)
                .setCssClasses(new String[]{"property"})
                .build();

        // Initialize the status page
        statusPage = StatusPage.builder()
                .setIconName(Constants.Application.ICON)
                .setTitle(LEDSuite.i18n("status_dialog_title"))
                .build();
    }

    /**
     * Constructs a new StatusDialog.
     * Sets up the dialog with its UI elements and initial configuration.
     *
     * @since 1.0.0
     */
    public StatusDialog() {
        // Initialize the dialog
        initialize();

        // Create a clamp to contain the status page
        var clamp = Clamp.builder()
                .setMaximumSize(700)
                .setTighteningThreshold(600)
                .setChild(statusPage)
                .build();

        // Create a toolbar view containing the clamp
        var toolbarView = ToolbarView.builder()
                .setContent(clamp)
                .build();

        // Create a header bar with a title widget
        var headerBar1 = HeaderBar.builder()
                .setShowTitle(false)
                .setTitleWidget(Label.builder().setLabel(LEDSuite.i18n("status_dialog_title")).build())
                .build();

        // Add the header bar to the toolbar view
        toolbarView.addTopBar(headerBar1);

        // Set the child of this dialog to the toolbar view
        this.setChild(toolbarView);

        // Configure the dialog with initial status
        configure(StatusUpdate.notConnected());

        // Handle dialog closure
        this.onClosed(() -> {
            if (updateTask != null) {
                LEDSuite.eventManager.unregisterEvents(this);
                updateTask.cancel();
                updateTask = null;
            }
        });

        // Allow the dialog to follow content size changes
        this.setFollowsContentSize(true);
    }

    private LEDSuiteTask updateTask = null;

    /**
     * Presents the status dialog to the user and starts the status update loop.
     *
     * @param parent The parent widget to which the dialog is attached.
     * @since 1.0.0
     */
    @Override
    public void present(Widget parent) {
        LEDSuite.logger.debug("Fulfilling StatusDialog present request!");
        // Start the update loop
        updateTask = updateLoop();
        super.present(parent);
    }

    /**
     * Initializes the status page with groups and action rows for displaying status information.
     *
     * @since 1.0.0
     */
    private void initStatus() {
        // Create a vertical box to contain status elements
        var statusList = Box.builder()
                .setOrientation(Orientation.VERTICAL)
                .setSpacing(12)
                .setHexpand(true)
                .build();

        // Create and configure groups for power usage and file stats
        var powerUsage = PreferencesGroup.builder()
                .setCssClasses(new String[]{"background"})
                .setTitle(LEDSuite.i18n("status_dialog_power_section_title"))
                .build();

        var fileStats = PreferencesGroup.builder()
                .setCssClasses(new String[]{"background"})
                .setTitle(LEDSuite.i18n("status_dialog_animation_section_title"))
                .build();

        // Add action rows to the groups
        fileStats.add(currentFile);
        fileStats.add(fileState);

        var general = PreferencesGroup.builder()
                .setTitle(LEDSuite.i18n("status_dialog_general_section"))
                .build();

        general.add(lidState);
        powerUsage.add(voltage);
        powerUsage.add(currentDraw);

        // Append the groups to the status list
        statusList.append(general);
        statusList.append(fileStats);
        statusList.append(powerUsage);

        // Set the child of the status page to the status list
        statusPage.setChild(statusList);
        statusInit = true;
    }

    /**
     * Configures the dialog based on the provided status update.
     *
     * @param statusUpdate The status update to configure the dialog with.
     * @since 1.0.0
     */
    private void configure(StatusUpdate statusUpdate) {
        if (!statusUpdate.isNotConnected()) lastUpdate = System.currentTimeMillis();
        int checksum = statusUpdate.hashCode();
        if (this.checksum == checksum) {
            LEDSuite.logger.debug("Skipping display request for status update '" + statusUpdate.getNetworkEventID() + "' because checksum '" + checksum + "' didn't change");
            return;
        } else this.checksum = checksum;

        if (statusUpdate.isNotConnected()) {
            if (statusInit) reboot = true;
            var statusList = Box.builder()
                    .setOrientation(Orientation.VERTICAL)
                    .setSpacing(12)
                    .setHexpand(true)
                    .build();

            // Add not connected status information
            statusList.append(
                    ActionRow.builder().setTitle(LEDSuite.i18n("status_dialog_not_connected"))
                            .setSubtitle(LEDSuite.server_settings.getIPv4() + ":" + LEDSuite.server_settings.getPort() + " is currently not responding!").build()
            );
            statusList.append(
                    ActionRow.builder().setTitle(LEDSuite.i18n("status_dialog_not_connected_causes_title"))
                            .setSubtitle(LEDSuite.i18n("status_dialog_not_connected_causes_subtitle")).build()
            );

            statusPage.setChild(statusList);
            return;
        }

        if (reboot) {
            this.close();
            StatusDialog dialog = new StatusDialog();
            dialog.configure(statusUpdate);
            dialog.present(LEDSuite.mainWindow);
            return;
        }

        if (!statusInit) {
            initStatus();
        }

        // Update action rows with new status information
        currentDraw.setSubtitle(statusUpdate.getCurrentDraw() + "A");
        voltage.setSubtitle(statusUpdate.getVoltage() + "V");
        if (statusUpdate.isFileLoaded()) {
            currentFile.setSubtitle(statusUpdate.getFileSelected());
            fileState.setSubtitle(LEDSuite.i18n(statusUpdate.getFileState().getI18nKey()));
        } else {
            currentFile.setSubtitle("N/A");
            fileState.setSubtitle(LEDSuite.i18n(statusUpdate.getFileState().getI18nKey()));
            fileState.setSubtitle(LEDSuite.i18n("status_dialog_no_animation_loaded"));
        }

        lidState.setSubtitle(statusUpdate.isLidState() ? LEDSuite.i18n("lid_state_closed") : LEDSuite.i18n("lid_state_open"));
    }

    /**
     * Updates the dialog status based on the provided status update.
     *
     * @param statusUpdate The status update to use for configuring the dialog.
     * @since 1.0.0
     */
    private void updateStatus(StatusUpdate statusUpdate) {
        configure(statusUpdate);
    }

    private final long maxDelay = 10000; // Maximum delay between status updates

    /**
     * Creates and starts a task to periodically update the status of the dialog.
     *
     * @return The LEDSuiteTask responsible for updating the status.
     * @since 1.0.0
     */
    private LEDSuiteTask updateLoop() {
        @NonNull var statusBar = LEDSuite.mainWindow.widgetCache.get(Banner.class, "statusBar");
        LEDSuite.eventManager.registerEvents(this);
        if (!statusBar.getRevealed()) {
            return new LEDSuiteGuiRunnable() {
                @Override
                public void processGui() {
                    LEDSuite.mainWindow.getStatus(_ -> {});
                    if (System.currentTimeMillis() - lastUpdate >= maxDelay) configure(StatusUpdate.notConnected());
                }
            }.runTaskTimerAsynchronously(0, LEDSuite.argumentsSettings.getStatusRequestClockActive());
        }
        return new LEDSuiteGuiRunnable() {
            @Override
            public void processGui() {
                LEDSuite.mainWindow.getStatus(_ -> {});
                LEDSuite.logger.debug("Status updater already active. Terminating new instance");
            }
        }.runTask();
    }

    /**
     * Handles status events and updates the dialog accordingly.
     *
     * @param e The status event containing the status update.
     * @since 1.0.0
     */
    @EventHandler
    public void onStatus(Events.Status e) {
        if (this.getVisible()) updateStatus(e.statusUpdate());
    }
}
