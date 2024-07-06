package com.x_tornado10.lccp.ui;

import com.x_tornado10.lccp.LCCP;
import com.x_tornado10.lccp.event_handling.EventHandler;
import com.x_tornado10.lccp.event_handling.Events;
import com.x_tornado10.lccp.event_handling.listener.EventListener;
import com.x_tornado10.lccp.task_scheduler.LCCPRunnable;
import com.x_tornado10.lccp.task_scheduler.LCCPTask;
import com.x_tornado10.lccp.yaml_factory.wrappers.message_wrappers.StatusUpdate;
import org.gnome.adw.*;
import org.gnome.gtk.Box;
import org.gnome.gtk.Label;
import org.gnome.gtk.Orientation;
import org.gnome.gtk.Widget;

public class StatusDialog extends PreferencesDialog implements EventListener {
    private static int checksum = 0;
    private final static ActionRow currentDraw = ActionRow.builder()
            .setTitle("Current Draw")
            .setSubtitleSelectable(true)
            .setCssClasses(new String[]{"property"})
            .build();
    private final static ActionRow voltage = ActionRow.builder()
            .setTitle("Voltage")
            .setSubtitleSelectable(true)
            .setCssClasses(new String[]{"property"})
            .build();

    private final static ActionRow currentFile = ActionRow.builder()
            .setTitle("Current File")
            .setSubtitleSelectable(true)
            .setCssClasses(new String[]{"property"})
            .build();
    private final static ActionRow fileState = ActionRow.builder()
            .setTitle("Current State")
            .setSubtitleSelectable(true)
            .setCssClasses(new String[]{"property"})
            .build();

    private final static ActionRow lidState = ActionRow.builder()
            .setTitle("Lid State")
            .setSubtitleSelectable(true)
            .setCssClasses(new String[]{"property"})
            .build();
    private final static StatusPage statusPage = StatusPage.builder()
            .setIconName("com.x_tornado10.lccp")
            .setTitle("LED Cube Status")
            .build();
    private static boolean init = false;

    public StatusDialog() {
        configure(StatusUpdate.blank());
        this.init();
        this.onClosed(() -> {
            if (updateTask != null) {
                LCCP.eventManager.unregisterEvents(this);
                updateTask.cancel();
                updateTask = null;
            }
        });

        this.setFollowsContentSize(true);

    }
    private LCCPTask updateTask = null;
    @Override
    public void present(Widget parent) {
        LCCP.logger.debug("Fulfilling StatusDialog present request!");
        updateTask = updateLoop();
        super.present(parent);
    }

    private void init() {

        var statusList = Box.builder()
                .setOrientation(Orientation.VERTICAL)
                .setSpacing(12)
                .setHexpand(true)
                .build();

        var powerUsage = PreferencesGroup.builder()
                .setCssClasses(new String[]{"background"})
                .setTitle("Power")
                .build();

        var fileStats = PreferencesGroup.builder()
                .setCssClasses(new String[]{"background"})
                .setTitle("Animation")
                .build();

        fileStats.add(currentFile);
        fileStats.add(fileState);

        var general = PreferencesGroup.builder()
                .setTitle("General")
                .build();

        general.add(lidState);

        powerUsage.add(voltage);
        powerUsage.add(currentDraw);

        statusList.append(general);
        statusList.append(fileStats);
        statusList.append(powerUsage);

        statusPage.setChild(statusList);

        var clamp = Clamp.builder()
                .setMaximumSize(700)
                .setTighteningThreshold(600)
                .setChild(statusPage)
                .build();

        var toolbarView = ToolbarView.builder()
                .setContent(clamp)
                .build();

        var headerBar1 = HeaderBar.builder()
                .setShowTitle(false)
                .setTitleWidget(Label.builder().setLabel("LED Cube Status").build())
                .build();

        toolbarView.addTopBar(headerBar1);

        this.setChild(toolbarView);
        this.setSearchEnabled(true);
        init = true;
    }

    private void configure(StatusUpdate statusUpdate) {
        int checksum = statusUpdate.hashCode();
        if (StatusDialog.checksum == checksum) {
            LCCP.logger.debug("Skipping display request for status update '" + statusUpdate.getNetworkEventID() + "' because checksum '" + checksum + "' didn't change");
            return;
        } else StatusDialog.checksum = checksum;

        if (statusUpdate.isBlank()) {
            init = false;

            var statusList = Box.builder()
                    .setOrientation(Orientation.VERTICAL)
                    .setSpacing(12)
                    .setHexpand(true)
                    .build();

            statusList.append(
                    ActionRow.builder().setTitle("Not connected to Cube!").setSubtitle(LCCP.server_settings.getIPv4() + ":" + LCCP.server_settings.getPort() + " is currently not responding!").build()
            );
            statusList.append(
                    ActionRow.builder().setTitle("Possible causes").setSubtitle("Waiting for response, Connection failed due to invalid host / port, Connection refused by host").build()
            );

            statusPage.setChild(statusList);
            return;
        }

        if (!init) init();

        currentDraw.setSubtitle(statusUpdate.getCurrentDraw() + "A");
        voltage.setSubtitle(statusUpdate.getVoltage() + "V");
        if (statusUpdate.isFileLoaded()) {
            currentFile.setSubtitle(statusUpdate.getFileSelected());
            fileState.setSubtitle(statusUpdate.getFileState().name());
        } else {
            currentFile.setSubtitle("N/A");
            fileState.setSubtitle(statusUpdate.getFileState().name());
            fileState.setSubtitle("No animation selected!");
        }

        lidState.setSubtitle(statusUpdate.humanReadableLidState(statusUpdate.isLidState()));
    }

    private void updateStatus(StatusUpdate statusUpdate) {
        configure(statusUpdate);
    }

    private LCCPTask updateLoop() {
        LCCP.eventManager.registerEvents(this);
        if (!LCCP.mainWindow.isBannerVisible()) {
            return new LCCPRunnable() {
                @Override
                public void run() {
                    LCCP.mainWindow.getStatus();
                }
            }.runTaskTimerAsynchronously(0, 1000);
        }
        return new LCCPRunnable() {
            @Override
            public void run() {
                LCCP.logger.debug("Status updater already active. Terminating new instance");
            }
        }.runTask();
    }

    @EventHandler
    public void onStatus(Events.Status e) {
        updateStatus(e.statusUpdate());
    }
}