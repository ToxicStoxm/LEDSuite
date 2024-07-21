package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.LEDSuite;
import com.toxicstoxm.LEDSuite.communication.network.Networking;
import com.toxicstoxm.LEDSuite.event_handling.Events;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import org.gnome.adw.*;
import org.gnome.gtk.Spinner;
import org.gnome.gtk.Widget;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class SettingsDialog extends PreferencesDialog {
    // boolean list to keep track of current user settings
    private final Boolean[] temp;
    // value to keep track of current brightness level
    private double prev1 = 0.0;
    // preferences group server settings
    private PreferencesGroup serverSettings = null;

    // settings dialog constructor
    public SettingsDialog() {
        // configuring settings window appearance
        setTitle("Settings");
        setSearchEnabled(true);

        // setting the default values
        temp = new Boolean[3];
        temp[0] = true;
        temp[1] = LEDSuite.settings.isDisplayStatusBar();
        //temp[2] = LEDSuite.settings.isAutoUpdateRemote();

        this.onClosed(() -> LEDSuite.mainWindow.resetSettingsDialog());
    }

    // function to generate a new settings dialog page
    private PreferencesPage get_user_pref_page() {
        // defining new preference page
        var user_pref_page = new PreferencesPage();
        user_pref_page.setTitle(LEDSuite.version);

        // defining new preferences group for general settings
        var generalSettings = new PreferencesGroup();
        generalSettings.setTitle("General Settings");

        // creating switch row to toggle the status bar
        var statusBarToggle = SwitchRow.builder()
                .setActive(LEDSuite.mainWindow.isBannerVisible())
                .setTitle("Status Bar")
                .setTooltipText("Toggles the small status bar on the main window.")
                .build();
        // enabled the status bar if it's not already activated
        statusBarToggle.getActivatableWidget().onStateFlagsChanged(_ -> {
            boolean active = statusBarToggle.getActive();
            if (!temp[1] == active) {
                LEDSuite.logger.debug("StatusToggle: " + active);
                // status bar is activated using the set banner visible function from the main window class
                LEDSuite.mainWindow.setBannerVisible(active);
                temp[1] = active;
            }
        });

        // adding the status bar toggle to the general settings group
        generalSettings.add(statusBarToggle);

        user_pref_page.add(generalSettings);

        serverSettings = new PreferencesGroup();
        serverSettings.setTitle("Cube Settings");

        var brightnessRow = SpinRow.withRange(0, 100, 1);
        brightnessRow.setValue(LEDSuite.server_settings.getLED_Brightness() * 100);
        brightnessRow.setSnapToTicks(true);
        brightnessRow.setWrap(false);
        brightnessRow.setClimbRate(2);
        brightnessRow.setNumeric(true);
        brightnessRow.setTitle("LED - Brightness");
        prev1 = brightnessRow.getValue();
        brightnessRow.onOutput(() -> {
            double val = brightnessRow.getValue();
            if (prev1 != val) {
                float newValue = (float) val;
                LEDSuite.logger.debug("Brightness changed: -> " + brightnessRow.getValue());
                LEDSuite.server_settings.setLED_Brightness(newValue);
                LEDSuite.eventManager.fireEvent(new Events.SettingChanged(Constants.Server_Config.BRIGHTNESS, newValue));
                prev1 = val;
            }
            return false;
        });
        this.setCanClose(true);
        serverSettings.add(brightnessRow);

        var spinner = new Spinner();

        var ipv4Row = EntryRow.builder().setTitle("IPv4").build();
        ipv4Row.setShowApplyButton(true);
        ipv4Row.setText(LEDSuite.server_settings.getIPv4());
        ipv4Row.setEnableUndo(true);
        AtomicReference<String> prevIPv4 = new AtomicReference<>(LEDSuite.server_settings.getIPv4());
        ipv4Row.onApply(() -> {
            if (!LEDSuite.settings.isCheckIPv4()) {
                LEDSuite.server_settings.setIPv4(ipv4Row.getText());
            } else {
                ipv4Row.setEditable(false);
                ipv4Row.addSuffix(spinner);
                spinner.setSpinning(true);
                new LEDSuiteRunnable() {
                    @Override
                    public void run() {
                        try {
                            String ip;
                            String text = ipv4Row.getText();

                            if (!LEDSuite.server_settings.getIPv4().equals(text)) {

                                try {
                                    ip = Networking.Validation.getValidIP(text, false);
                                } catch (IOException e) {
                                    LEDSuite.sysBeep();
                                    addToast(
                                            Toast.builder()
                                                    .setTitle("Server unreachable!")
                                                    .setTimeout(10)
                                                    .build()
                                    );
                                    ip = null;
                                }
                                if (ip != null) {
                                    try {
                                        LEDSuite.server_settings.setIPv4(ip);
                                        Networking.Communication.NetworkHandler.hostChanged();
                                        prevIPv4.set(ip);
                                    } catch (Networking.NetworkException e) {
                                        LEDSuite.server_settings.setIPv4(prevIPv4.get());
                                        try {
                                            Networking.Communication.NetworkHandler.hostChanged();
                                        } catch (Networking.NetworkException ex) {
                                            LEDSuite.logger.error("Fallback connection failed! Stopping network communication!");
                                            Networking.Communication.NetworkHandler.cancel();
                                        }
                                    }
                                }
                            }
                        } finally {
                            spinner.setSpinning(false);
                            ipv4Row.remove(spinner);
                            ipv4Row.setEditable(true);
                        }
                    }
                }.runTask();
            }
        });
        serverSettings.add(ipv4Row);

        var spinner1 = new Spinner();

        var port = EntryRow.builder().setTitle("Port").build();
        port.setShowApplyButton(true);
        port.setText(String.valueOf(LEDSuite.server_settings.getPort()));
        port.setEnableUndo(true);
        AtomicReference<String> prevPort = new AtomicReference<>(port.getText());
        port.onApply(() -> {
            spinner1.setSpinning(true);
            port.addSuffix(spinner1);
            port.setEditable(false);
            new LEDSuiteRunnable() {
                @Override
                public void run() {
                    String portVal = port.getText();
                    try {
                        int port = Integer.parseInt(portVal);
                        if (LEDSuite.server_settings.getPort() != port) {
                            if (Networking.Validation.isValidPORT(portVal)) {
                                LEDSuite.server_settings.setPort(port);
                                Networking.Communication.NetworkHandler.hostChanged();
                                prevPort.set(portVal);
                            } else {
                                throw new NumberFormatException();
                            }
                        }
                    } catch (NumberFormatException | Networking.NetworkException e) {
                        LEDSuite.sysBeep();
                        addToast(
                                Toast.builder()
                                        .setTitle("Invalid Port!")
                                        .setTimeout(10)
                                        .build()
                        );
                        try {
                            LEDSuite.server_settings.setPort(Integer.parseInt(prevPort.get()));
                            Networking.Communication.NetworkHandler.hostChanged();
                        } catch (NumberFormatException | Networking.NetworkException ex) {
                            LEDSuite.logger.error("Fallback connection failed! Stopping network communication!");
                            Networking.Communication.NetworkHandler.cancel();
                        }
                    } finally {
                        spinner1.setSpinning(false);
                        port.remove(spinner1);
                        port.setEditable(true);
                    }
                }
            }.runTask();
        });
        serverSettings.add(port);

        user_pref_page.add(serverSettings);
        return user_pref_page;
    }

    @Override
    public void present(Widget parent) {
        LEDSuite.logger.debug("Fulfilling SettingsDialog present request!");
        //if (LEDSuite.settings.isAutoUpdateRemote()) startRemoteUpdate();
        if (temp[0]) {
            add(get_user_pref_page());
            temp[0] = false;
        }
        super.present(parent);
    }
}
