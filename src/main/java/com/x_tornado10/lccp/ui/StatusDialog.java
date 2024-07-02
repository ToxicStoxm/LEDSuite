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
    public StatusDialog() {
        configure(StatusUpdate.blank());
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

    private void configure(StatusUpdate statusUpdate) {

        if (statusUpdate.isBlank()) {
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

            /*
            statusList.append(general);
            statusList.append(fileStats);
            statusList.append(powerUsage);
             */

            var statusPage = StatusPage.builder()
                    .setIconName("LCCP-logo-256x256")
                    .setTitle("LED Cube Status")
                    .setChild(statusList)
                    .build();

            var clamp = Clamp.builder()
                    .setMaximumSize(500)
                    .setTighteningThreshold(400)
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
        } else {

            var statusList = Box.builder()
                    .setOrientation(Orientation.VERTICAL)
                    .setSpacing(12)
                    .setHexpand(true)
                    .build();

            var powerUsage = PreferencesGroup.builder()
                    .setCssClasses(new String[]{"background"})
                    .setTitle("Power")
                    .build();

            var currentDraw = ActionRow.builder()
                    .setTitle("Current Draw")
                    .setSubtitle(statusUpdate.getCurrentDraw() + "A")
                    .setSubtitleSelectable(true)
                    .setCssClasses(new String[]{"property"})
                    .build();
            var voltage = ActionRow.builder()
                    .setTitle("Voltage")
                    .setSubtitle(statusUpdate.getVoltage() + "V")
                    .setSubtitleSelectable(true)
                    .setCssClasses(new String[]{"property"})
                    .build();

            var fileStats = PreferencesGroup.builder()
                    .setCssClasses(new String[]{"background"})
                    .setTitle("Animation")
                    .build();

            if (statusUpdate.isFileLoaded()) {

                var currentFile = ActionRow.builder()
                        .setTitle("Current File")
                        .setSubtitle(statusUpdate.getFileSelected())
                        .setSubtitleSelectable(true)
                        .setCssClasses(new String[]{"property"})
                        .build();
                fileStats.add(currentFile);
                var fileState = ActionRow.builder()
                        .setTitle("Current State")
                        .setSubtitle(statusUpdate.getFileState().name())
                        .setSubtitleSelectable(true)
                        .setCssClasses(new String[]{"property"})
                        .build();

                fileStats.add(fileState);
            } else {
                var fileState = ActionRow.builder()
                        .setTitle("Current State")
                        .setSubtitle("No animation selected!")
                        .setSubtitleSelectable(true)
                        .setCssClasses(new String[]{"property"})
                        .build();

                fileStats.add(fileState);
            }


            var general = PreferencesGroup.builder()
                    .setTitle("General")
                    .build();

            var lidState = ActionRow.builder()
                    .setTitle("Lid State")
                    .setSubtitle(statusUpdate.humanReadableLidState(statusUpdate.isLidState()))
                    .setSubtitleSelectable(true)
                    .setCssClasses(new String[]{"property"})
                    .build();

            general.add(lidState);

            powerUsage.add(voltage);
            powerUsage.add(currentDraw);

            statusList.append(general);
            statusList.append(fileStats);
            statusList.append(powerUsage);


            var statusPage = StatusPage.builder()
                    .setIconName("LCCP-logo-256x256")
                    .setTitle("LED Cube Status")
                    .setChild(statusList)
                    .build();

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
        }
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