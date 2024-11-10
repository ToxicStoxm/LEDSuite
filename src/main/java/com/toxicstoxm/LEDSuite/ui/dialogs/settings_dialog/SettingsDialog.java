package com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.SettingsRequestPacket;
import com.toxicstoxm.LEDSuite.communication.websocket.WebSocketClient;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.settings.LEDSuiteSettingsBundle;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import com.toxicstoxm.LEDSuite.time.Action;
import com.toxicstoxm.LEDSuite.time.CooldownManger;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.LEDSuite.ui.dialogs.ProviderCallback;
import com.toxicstoxm.LEDSuite.ui.dialogs.UpdateCallback;
import io.github.jwharm.javagi.gtk.annotations.GtkCallback;
import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.TemplateTypes;
import lombok.Getter;
import org.gnome.adw.Spinner;
import org.gnome.adw.*;
import org.gnome.glib.GLib;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Template class for the settings / preferences dialog.
 * <br>Template file: {@code SettingsDialog.ui}
 * @since 1.0.0
 */
@GtkTemplate(name = "SettingsDialog", ui = "/com/toxicstoxm/LEDSuite/SettingsDialog.ui")
public class SettingsDialog extends PreferencesDialog {

    private static final Type gtype = TemplateTypes.register(SettingsDialog.class);

    @Getter
    private UpdateCallback<SettingsUpdate> updater;

    @Getter
    private ProviderCallback<SettingsData> provider;

    @Getter
    private Action ApplyButtonCooldownTrigger;

    @Getter
    private ConnectivityStatus connectivityStatus;

    public SettingsDialog(MemorySegment address) {
        super(address);
        updater = this::update;
        provider = this::getData;
        ApplyButtonCooldownTrigger = this::applyButtonCooldown;
        connectivityStatus = new ConnectivityStatus() {
            @Override
            public void connected() {
                GLib.idleAddOnce(() -> setServerStateConnected());
            }

            @Override
            public void disconnected() {
                GLib.idleAddOnce(() -> setServerStateDisconnected());
            }

            @Override
            public void disconnecting() {
                GLib.idleAddOnce(() -> setSeverStateDisconnecting());
            }

            @Override
            public void connecting() {
                GLib.idleAddOnce(() -> setServerStateConnecting());
            }
        };
    }

    public static Type getType() {
        return gtype;
    }

    public static @NotNull SettingsDialog create() {
        SettingsDialog settingsDialog = GObject.newInstance(getType());
        settingsDialog.initialize();
        settingsDialog.markServerSettingsUnavailable();
        return settingsDialog;
    }

    @GtkChild(name = "settings_server_address")
    public EntryRow serverAddress;

    @GtkChild(name = "settings_server_group")
    public PreferencesGroup serverGroup;

    @GtkChild(name = "settings_brightness")
    public SpinRow brightness;

    @GtkChild(name = "settings_color_mode")
    public ComboRow colorMode;

    @GtkChild(name = "settings_restore_previous_state")
    public SwitchRow restorePreviousState;

    @GtkChild(name = "settings_apply_button")
    public Button applyButton;

    @GtkChild(name = "settings_server_group_suffix")
    public Button serverConnectivityButton;

    @GtkChild(name = "settings_server_group_suffix_box")
    public Box serverConnectivityButtonBox;

    @GtkChild(name = "settings_server_group_suffix_label")
    public Label serverConnectivityButtonLabel;

    @GtkChild(name = "settings_server_group_suffix_spinner")
    public Spinner serverConnectivityButtonSpinner;

    @GtkChild(name = "settings_server_group_suffix_spinner_revealer")
    public Revealer serverConnectivityButtonSpinnerRevealer;

    private void setBrightness(Integer brightness) {
        if (brightness == null) {
            this.brightness.setSensitive(false);
        } else {
            this.brightness.setValue(brightness);
            this.brightness.setSensitive(true);
        }
    }

    private void setColorMode(Integer selectedColorMode) {
        colorMode.setSelected(
                Objects.requireNonNullElse(
                        selectedColorMode,
                        Gtk.INVALID_LIST_POSITION
                )
        );
    }

    private void setSupportedColorModes(Collection<String> supportedColorModes, Integer selectedColorMode) {
        if (supportedColorModes == null) {
            this.colorMode.setSensitive(false);
            setColorMode(null);
        } else if (supportedColorModes.size() == 1) {
            this.colorMode.setSensitive(false);
            setColorMode(0);
        } else {
            String[] supported = supportedColorModes.toArray(new String[]{});
            this.colorMode.setSensitive(true);
            colorMode.setModel(
                    StringList.builder()
                            .setStrings(supported)
                            .build()
            );
            setColorMode(selectedColorMode);
        }

    }

    private void setRestorePreviousState(Boolean restorePreviousState) {
        if (restorePreviousState == null) {
            this.restorePreviousState.setSensitive(false);
        } else {
            this.restorePreviousState.setSensitive(true);
            this.restorePreviousState.setActive(restorePreviousState);
        }
    }

    private void initialize() {
        CooldownManger.addAction("serverConnectivityButtonCb", () -> {
            if (isServerConnected()) {
                connectivityStatus.disconnecting();
                triggerDisconnect();
            } else {
                connectivityStatus.connecting();
                triggerConnect();
            }
        }, 500, true);

        markServerSettingsUnavailable();
        serverAddress.setText(LEDSuiteSettingsBundle.WebsocketURI.getInstance().get());
        serverAddress.setShowApplyButton(true);
        serverAddress.onApply(() -> LEDSuiteSettingsBundle.WebsocketURI.getInstance().set(serverAddress.getText()));
        serverAddress.setSensitive(false);

        updateServerState();
    }

    private void update(@NotNull SettingsUpdate settingsUpdate) {
        GLib.idleAddOnce(() -> {
            setSupportedColorModes(settingsUpdate.supportedColorModes(), settingsUpdate.selectedColorMode());
            setBrightness(settingsUpdate.brightness());
            setRestorePreviousState(settingsUpdate.restorePreviousState());

            updateServerState();
        });
    }

    private void updateServerState() {
        if (!serverConnectivityButton.isSensitive()) return;
        if (isServerConnected()) {
            connectivityStatus.connected();
        } else {
            connectivityStatus.disconnected();
        }
    }

    private void setServerStateConnected() {
        serverStateNormal();

        applyButton.setSensitive(true);

        setServerGroupSuffixStyle(Constants.UI.SettingsDialog.CONNECTED_CSS);
        serverConnectivityButtonLabel.setLabel(Constants.UI.SettingsDialog.CONNECTED);
        serverConnectivityButton.setTooltipText(Constants.UI.SettingsDialog.CONNECTED_TOOLTIP);

        markServerSettingsAvailable();
    }

    private void setServerStateDisconnected() {
        serverStateNormal();

        serverAddress.setSensitive(true);
        setServerGroupSuffixStyle(Constants.UI.SettingsDialog.DISCONNECTED_CSS);
        serverConnectivityButtonLabel.setLabel(Constants.UI.SettingsDialog.DISCONNECTED);
        serverConnectivityButton.setTooltipText(Constants.UI.SettingsDialog.DISCONNECTED_TOOLTIP);

        markServerSettingsUnavailable();
    }

    private void serverStateNormal() {
        serverConnectivityButton.setSensitive(true);
        serverConnectivityButtonBox.setSpacing(0);
        serverConnectivityButtonSpinnerRevealer.setRevealChild(false);
    }

    private void markServerSettingsUnavailable() {
        changeServerSettingsAvailability(false);
    }

    private void markServerSettingsAvailable() {
       changeServerSettingsAvailability(true);
    }

    private void changeServerSettingsAvailability(boolean available) {
        colorMode.setSensitive(available);
        brightness.setSensitive(available);
        applyButton.setSensitive(available);
        restorePreviousState.setSensitive(available);
    }

    private void setServerStateConnecting() {
        serverStateChanging();
        serverConnectivityButtonLabel.setLabel(Constants.UI.SettingsDialog.CONNECTING);
    }

    private void setSeverStateDisconnecting() {
        serverStateChanging();
        serverConnectivityButtonLabel.setLabel(Constants.UI.SettingsDialog.DISCONNECTING);
    }

    private void serverStateChanging() {
        markServerSettingsUnavailable();
        serverConnectivityButton.setSensitive(false);
        serverConnectivityButton.setTooltipText(null);
        serverConnectivityButtonBox.setSpacing(8);
        serverConnectivityButtonSpinnerRevealer.setRevealChild(true);
        setServerGroupSuffixStyle(Constants.UI.SettingsDialog.CHANGING_CSS);
        serverAddress.setSensitive(false);
    }

    private void setServerGroupSuffixStyle(String[] css) {
        serverConnectivityButton.setCssClasses(css);
        serverConnectivityButtonLabel.setCssClasses(css);
    }

    private void triggerDisconnect() {
        new LEDSuiteRunnable() {
            @Override
            public void run() {

                if (isServerConnected()) {

                    try {
                        Thread.sleep(Constants.UI.SettingsDialog.MINIMUM_DELAY);
                    } catch (InterruptedException e) {
                        LEDSuiteApplication.getLogger().warn("Minimum delay sleeper was interrupted!", new LEDSuiteLogAreas.USER_INTERACTIONS());
                    }

                    LEDSuiteApplication.getWebSocketCommunication().shutdown();
                } else {
                    LEDSuiteApplication.getLogger().info("Skipping disconnect attempt because communication websocket is already disconnected!", new LEDSuiteLogAreas.NETWORK());
                }
            }
        }.runTaskLaterAsynchronously(100);
    }

    private void triggerConnect() {
        new LEDSuiteRunnable() {
            @Override
            public void run() {
                if (!isServerConnected()) {
                    if (LEDSuiteApplication.startCommunicationSocket()) {
                        connectivityStatus.connected();
                        requestSettings();
                    } else {
                        connectivityStatus.disconnected();
                    }
                } else {
                    LEDSuiteApplication.getLogger().info("Skipping connect attempt because communication websocket is already connected!", new LEDSuiteLogAreas.NETWORK());
                    connectivityStatus.connected();
                    requestSettings();
                }
            }
        }.runTaskLaterAsynchronously(100);
    }


    @GtkCallback(name = "settings_server_connectivity_button_cb")
    public void serverConnectivityButtonCb() {
        if (!CooldownManger.call("serverConnectivityButtonCb")) {
            LEDSuiteApplication.getLogger().verbose("Connectivity button on cooldown!", new LEDSuiteLogAreas.USER_INTERACTIONS());
        }
    }

    @Contract(" -> new")
    private @NotNull SettingsData getData() {
        String temp = ((StringList<?>) colorMode.getModel()).getString(colorMode.getSelected());
        return new SettingsData(
                (int) brightness.getValue(),
                temp == null || temp.equals(Constants.UI.NOT_AVAILABLE_VALUE) ? null : temp,
                restorePreviousState.getActive()
        );
    }

    private void applyButtonCooldown() {
        String[] defaultCSS = applyButton.getCssClasses();
        List<String> cssFail = new java.util.ArrayList<>(Arrays.stream(defaultCSS).toList());
        cssFail.remove("suggested-action");
        cssFail.add("regular");
        applyButton.setCssClasses(cssFail.toArray(new String[]{}));
        String defaultLabel = applyButton.getLabel();
        applyButton.setLabel("Slow down!");
        applyButton.setSensitive(false);

        new LEDSuiteRunnable() {
            @Override
            public void run() {
                GLib.idleAddOnce(() -> {
                    applyButton.setCssClasses(defaultCSS);
                    applyButton.setLabel(defaultLabel);
                    applyButton.setSensitive(true);
                });
            }
        }.runTaskLaterAsynchronously(500);
    }

    private void requestSettings() {
        LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                SettingsRequestPacket.builder().build().serialize()
        );
    }

    @Override
    public void present(@Nullable Widget parent) {
        if (isServerConnected()) requestSettings();

        updateServerState();

        if (LEDSuiteApplication.isConnecting()) {
            connectivityStatus.connecting();
            long start = LEDSuiteApplication.getConnectionAttempt();
            new LEDSuiteRunnable() {
                @Override
                public void run() {
                    if (isServerConnected()) {
                        connectivityStatus.connected();
                        cancel();
                    }
                    if (System.currentTimeMillis() - start > Constants.UI.SettingsDialog.CONNECTION_TIMEOUT) {
                        connectivityStatus.disconnected();
                        cancel();
                    }
                }
            }.runTaskTimerAsynchronously(0, 10);
        }

        super.present(parent);
    }

    private boolean isServerConnected() {
        WebSocketClient webSocketClient = LEDSuiteApplication.getWebSocketCommunication();
        return webSocketClient != null && webSocketClient.isConnected();
    }
}
