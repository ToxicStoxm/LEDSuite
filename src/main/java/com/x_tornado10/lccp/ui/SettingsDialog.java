package com.x_tornado10.lccp.ui;

import com.x_tornado10.lccp.LCCP;
import com.x_tornado10.lccp.util.Networking;
import org.gnome.adw.*;
import org.gnome.gtk.*;

import java.io.IOException;
import java.net.UnknownHostException;


public class SettingsDialog extends PreferencesDialog {
    private final Boolean[] temp;
    private double prev1 = 0.0;
    private AutoUpdateRemote autoUpdateRemote = null;
    private PreferencesGroup serverSettings = null;

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

        var generalSettings = new PreferencesGroup();
        generalSettings.setTitle("General Settings");

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

        generalSettings.add(statusBar);

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


        serverSettings = new PreferencesGroup();
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
        this.setCanClose(true);
        onClosed(this::stopRemoteUpdate);
        serverSettings.add(brightness);

        var spinner = new Spinner();

        var ipv4 = EntryRow.builder().setTitle("IPv4").build();
        ipv4.setShowApplyButton(true);
        ipv4.setText(LCCP.server_settings.getIPv4());
        ipv4.onApply(() -> {
            if (!LCCP.settings.isCheckIPv4()) {
                LCCP.server_settings.setIPv4(ipv4.getText());
            } else {
                Thread f = new Thread(() -> {
                    spinner.setSpinning(false);
                    ipv4.remove(spinner);
                    ipv4.setEditable(true);
                });

                Thread t = new Thread(() -> {
                    String ip = "";
                    String text = ipv4.getText();
                    LCCP.logger.debug(ip);
                    try {
                        ip = Networking.getValidIP(text, false);
                    } catch (IOException e) {
                        LCCP.sysBeep();
                        this.addToast(
                                Toast.builder()
                                        .setTitle("Server unreachable!")
                                        .setTimeout(10)
                                        .build()
                        );
                        ip = null;
                    }
                    if (ip != null) {
                        LCCP.server_settings.setIPv4(ip);
                    }
                    f.start();
                });
                ipv4.setEditable(false);
                ipv4.addSuffix(spinner);
                spinner.setSpinning(true);
                t.start();
            }
        });
        serverSettings.add(ipv4);

        if (!LCCP.settings.isAutoUpdateRemote()) addManualRemoteApplySwitch();

        v0_0_1.add(serverSettings);
        return v0_0_1;
    }

    private ActionRow manualRemoteApplySwitchRow = null;
    private ActionRow getManualRemoteApplySwitchRow() {
        if (manualRemoteApplySwitchRow == null) {
            manualRemoteApplySwitchRow = new ActionRow();
            manualRemoteApplySwitchRow.setTitle("Apply");

            var applyButton = new Button();
            applyButton.setSizeRequest(50, 50);
            applyButton.setLabel("Apply");
            applyButton.setIconName("emblem-synchronizing-symbolic");
            manualRemoteApplySwitchRow.addSuffix(applyButton);
            manualRemoteApplySwitchRow.setActivatableWidget(applyButton);
            applyButton.onClicked(() -> {
                LCCP.logger.debug("Apply remote request through manual button press");
                LCCP.updateRemoteConfig();
            });
        }
        return manualRemoteApplySwitchRow;
    }
    public void addManualRemoteApplySwitch() {

        serverSettings.add(getManualRemoteApplySwitchRow());
    }

    public void removeManualRemoteApplySwitch() {
        serverSettings.remove(getManualRemoteApplySwitchRow());
    }

    private static class AutoUpdateRemote extends Thread {

        public AutoUpdateRemote() {
            setName("AutoUpdateRemoteTask_" + getName());
            this.start();
        }

        private boolean stop = false;

        @Override
        public void run() {
            long tick = Math.round(LCCP.settings.getAutoUpdateRemoteTick() * 1000);
            long last = System.currentTimeMillis() - (tick + 1);
            while (true) {
                if (stop) return;
                long current = System.currentTimeMillis();
                if (current - last >= tick) {
                    LCCP.updateRemoteConfig();
                    last = current;
                }
                if (!LCCP.mainWindow.isSettingsDialogVisible()) stop = true;
            }
        }
    }

    @Override
    public void present(Widget parent) {
        LCCP.logger.debug("Fulfilling SettingsDialog present request!");
        if (LCCP.settings.isAutoUpdateRemote()) startRemoteUpdate();
        if (temp[0]) {
            add(get_v0_0_1_page());
            temp[0] = false;
        }
        super.present(parent);
    }

    public void startRemoteUpdate() {
        if (autoUpdateRemote == null) {
            autoUpdateRemote = new AutoUpdateRemote();
            LCCP.logger.debug("Started autoRemoteUpdateTask!");
        }
    }
    public void stopRemoteUpdate() {
        if (autoUpdateRemote != null) {
            autoUpdateRemote.stop = true;
            LCCP.logger.debug("Stopped autoRemoteUpdateTask " + autoUpdateRemote + " !");
            autoUpdateRemote = null;
        }
    }
}
