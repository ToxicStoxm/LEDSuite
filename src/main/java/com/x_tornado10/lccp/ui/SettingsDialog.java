package com.x_tornado10.lccp.ui;

import com.x_tornado10.lccp.LCCP;
import org.gnome.adw.*;
import org.gnome.gtk.Widget;

public class SettingsDialog extends PreferencesDialog {
    private boolean fT = true;
    private boolean prev0 = false;
    private boolean prev1 = false;

    public SettingsDialog() {
        setTitle("Settings");
        setSearchEnabled(true);
    }
    private PreferencesPage get_v0_0_1_page() {
        var v0_0_1 = new PreferencesPage();
        v0_0_1.setTitle(LCCP.version);

        var windowSettings = new PreferencesGroup();
        windowSettings.setTitle("Window");

        var statusBar = SwitchRow.builder()
                .setActive(LCCP.mainWindow.isBannerVisible())
                .setTitle("Toggle Status Bar")
                .build();
        statusBar.getActivatableWidget().onStateFlagsChanged(_ -> {
            boolean active = statusBar.getActive();
            if (!prev0 == active) {
                LCCP.logger.debug("StatusToggle: " + active);
                LCCP.mainWindow.setBannerVisible(active);
                prev0 = active;
            }
        });
        windowSettings.add(statusBar);

        v0_0_1.add(windowSettings);
        /*
        var devSettings = new PreferencesGroup();
        devSettings.setTitle("Developer Settings (DO NOT TOUCH)");
        var windowTitle = EntryRow.builder()
                .setTitle("Window Title")
                .setShowApplyButton(true)
                .setText(LCCP.settings.getWindowTitleRaw())
                .build();
        windowTitle.onApply(() -> {
            LCCP.settings.setWindowTitle(windowTitle.getText());
            LCCP.logger.debug("New Window Title: " + windowTitle.getText());
        });
        devSettings.add(windowTitle);

        v0_0_1.add(devSettings);
         */
        return v0_0_1;
    }

    @Override
    public void present(Widget parent) {
        if (fT) {
            add(get_v0_0_1_page());
            fT = false;
        }
        super.present(parent);
    }
}
