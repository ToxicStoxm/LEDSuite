package com.x_tornado10.lccp.ui;

import com.x_tornado10.lccp.LCCP;
import org.gnome.adw.*;
import org.gnome.gtk.Box;
import org.gnome.gtk.Label;
import org.gnome.gtk.Orientation;
import org.gnome.gtk.Widget;

public class StatusDialog extends PreferencesDialog {
    public StatusDialog() {
        var statusList = Box.builder()
                .setOrientation(Orientation.VERTICAL)
                .setSpacing(12)
                .setHexpand(true)
                .build();
        var clamp = Clamp.builder()
                .setMaximumSize(500)
                .setTighteningThreshold(400)
                .setChild(statusList)
                .build();

        var powerUsage = PreferencesGroup.builder()
                .setCssClasses(new String[]{"background"})
                .setTitle("Power")
                .build();

        var currentDraw = ActionRow.builder()
                .setTitle("Current Draw")
                .setSubtitle("12A")
                .setSubtitleSelectable(true)
                .setCssClasses(new String[]{"property"})
                .build();
        var voltage = ActionRow.builder()
                .setTitle("Voltage")
                .setSubtitle("220V")
                .setSubtitleSelectable(true)
                .setCssClasses(new String[]{"property"})
                .build();

        var fileStats = PreferencesGroup.builder()
                .setCssClasses(new String[]{"background"})
                .setTitle("Animation")
                .build();

        var currentFile = ActionRow.builder()
                .setTitle("Current File")
                .setSubtitle("test-file.mp4")
                .setSubtitleSelectable(true)
                .setCssClasses(new String[]{"property"})
                .build();

        var fileState = ActionRow.builder()
                .setTitle("Current State")
                .setSubtitle("playing")
                .setSubtitleSelectable(true)
                .setCssClasses(new String[]{"property"})
                .build();

        var general = PreferencesGroup.builder()
                .setTitle("General")
                .build();

        var lidState = ActionRow.builder()
                .setTitle("Lid State")
                .setSubtitle("open")
                .setSubtitleSelectable(true)
                .setCssClasses(new String[]{"property"})
                .build();

        general.add(lidState);

        powerUsage.add(voltage);
        powerUsage.add(currentDraw);

        fileStats.add(fileState);
        fileStats.add(currentFile);

        statusList.append(general);
        statusList.append(fileStats);
        statusList.append(powerUsage);


        var statusPage = StatusPage.builder()
                .setIconName("LCCP-logo-256x256")
                .setTitle("LED Cube Status")
                .setChild(clamp)
                .build();

        var toolbarView = ToolbarView.builder()
                .setContent(statusPage)
                .build();

        var headerBar1 = HeaderBar.builder()
                .setShowTitle(false)
                .setTitleWidget(Label.builder().setLabel("LED Cube Status").build())
                .build();

        toolbarView.addTopBar(headerBar1);

        this.setChild(toolbarView);
        this.setSearchEnabled(true);
    }
    @Override
    public void present(Widget parent) {
        LCCP.logger.debug("Fulfilling StatusDialog present request!");
        super.present(parent);
    }
}
