package com.x_tornado10.lccp.ui;

import com.x_tornado10.lccp.LCCP;
import org.gnome.adw.*;
import org.gnome.gtk.Widget;


public class SettingsDialog extends PreferencesDialog {
    private final Boolean[] temp;
    private double prev1 = 0.0;

    public SettingsDialog() {
        setTitle("Settings");
        setSearchEnabled(true);

        temp = new Boolean[3];
        temp[0] = true;
        temp[1] = LCCP.settings.isDisplayStatusBar();
        temp[2] = LCCP.settings.isAutoUpdateRemote();
    }
    private PreferencesPage get_v0_0_1_page() {
        var v0_0_1 = new PreferencesPage();
        v0_0_1.setTitle(LCCP.version);

        var windowSettings = new PreferencesGroup();
        windowSettings.setTitle("Window");

        var statusBar = SwitchRow.builder()
                .setActive(LCCP.mainWindow.isBannerVisible())
                .setTitle("Toggle status bar")
                .setTooltipText("Toggles the small status bar on the main window.")
                .build();
        statusBar.getActivatableWidget().onStateFlagsChanged(_ -> {
            boolean active = statusBar.getActive();
            if (!temp[1] == active) {
                LCCP.logger.debug("StatusToggle: " + active);
                LCCP.mainWindow.setBannerVisible(active);
                temp[1] = active;
            }
        });
        windowSettings.add(statusBar);

        v0_0_1.add(windowSettings);

        var generalSettings = new PreferencesGroup();
        generalSettings.setTitle("General Settings");

        var autoUpdateRemoteToggle = SwitchRow.builder()
                .setActive(LCCP.settings.isAutoUpdateRemote())
                .setTitle("Toggle autoupdate remote")
                .setTooltipText("Toggles autoupdates for the cube settings. Manual / Auto")
                .build();
        autoUpdateRemoteToggle.getActivatableWidget().onStateFlagsChanged(_ -> {
            boolean active = autoUpdateRemoteToggle.getActive();
            if (!temp[2] == active) {
                LCCP.logger.debug("AutoUpdateRemoteToggle: " + active);
                LCCP.settings.setAutoUpdateRemote(active);
                temp[2] = active;
            }
        });
        generalSettings.add(autoUpdateRemoteToggle);

        v0_0_1.add(generalSettings);


        var serverSettings = new PreferencesGroup();
        serverSettings.setTitle("Cube Settings");

        var brightness = SpinRow.withRange(0, 100, 1);
        brightness.setValue(LCCP.server_settings.getLED_Brightness() * 100);
        brightness.setSnapToTicks(true);
        brightness.setWrap(false);
        brightness.setClimbRate(2);
        brightness.setNumeric(true);
        brightness.setTitle("LED - Brightness");
        prev1 = brightness.getValue();
        brightness.onOutput(() -> {
            double val = brightness.getValue();
            if (prev1 != val) {
                LCCP.logger.debug(String.valueOf(brightness.getValue()));
                LCCP.server_settings.setLED_Brightness((float) val);
                prev1 = val;
            }
            return false;
        });
        serverSettings.add(brightness);
        v0_0_1.add(serverSettings);
        return v0_0_1;
    }

    @Override
    public void present(Widget parent) {
        if (temp[0]) {
            add(get_v0_0_1_page());
            temp[0] = false;
        }
        super.present(parent);
    }
}
