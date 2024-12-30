package com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.SettingsRequestPacket;
import com.toxicstoxm.LEDSuite.communication.websocket.WebSocketClient;
import com.toxicstoxm.LEDSuite.gettext.Translations;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.settings.LEDSuiteSettingsBundle;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import com.toxicstoxm.LEDSuite.time.CooldownManager;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs.authentication.AuthenticationDialog;
import io.github.jwharm.javagi.gtk.annotations.GtkCallback;
import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.TemplateTypes;
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
public class SettingsDialog extends PreferencesDialog implements SettingsDialogEndpoint {

    private static final Type gtype = TemplateTypes.register(SettingsDialog.class);

    public SettingsDialog(MemorySegment address) {
        super(address);
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

    @GtkChild(name = "settings_server_group_cnct_button")
    public Button serverConnectivityButton;

    @GtkChild(name = "settings_server_group_cnct_button_box")
    public Box serverConnectivityButtonBox;

    @GtkChild(name = "settings_server_group_cnct_button_label")
    public Label serverConnectivityButtonLabel;

    @GtkChild(name = "settings_server_group_cnct_button_spinner")
    public Spinner serverConnectivityButtonSpinner;

    @GtkChild(name = "settings_server_group_cnct_button_spinner_revealer")
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
        CooldownManager.addAction("serverConnectivityButtonCb", () -> {
            if (isServerConnected()) {
                setServerStateDisconnecting();
                triggerDisconnect();
            } else {
                this.setServerStateConnecting();
                triggerConnect();
            }
        }, 500, true);

        markServerSettingsUnavailable();
        serverAddress.setText(LEDSuiteSettingsBundle.WebsocketURI.getInstance().get());
        serverAddress.setShowApplyButton(true);
        serverAddress.onApply(() -> LEDSuiteSettingsBundle.WebsocketURI.getInstance().set(serverAddress.getText()));
        serverAddress.setSensitive(false);

        updateServerState();

        if (isServerConnected()) {
            setAuthenticated(true, LEDSuiteApplication.getAuthManager().getUsername());
        }
    }

    public void update(@NotNull SettingsUpdate settingsUpdate) {
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
            this.setServerStateConnected();
        } else {
            this.setServerStateDisconnected();
        }
    }

    public void setServerState(@NotNull ServerState serverState) {
        switch (serverState) {
            case CONNECTED -> setServerStateConnected();
            case CONNECTING -> setServerStateConnecting();
            case DISCONNECTED -> setServerStateDisconnected();
            case DISCONNECTING -> setServerStateDisconnecting();
            default -> throw new IllegalArgumentException("Unknown server state!");
        }
    }

    private void setServerStateConnected() {
        serverStateNormal();

        applyButton.setSensitive(true);

        String connectedString = Translations.getText("Connected");
        setServerGroupSuffixStyle(Constants.UI.CSS.CONNECTED_CSS);
        serverConnectivityButtonLabel.setLabel(connectedString);
        serverConnectivityButton.setTooltipText(connectedString);

        markServerSettingsAvailable();
    }

    private void setServerStateDisconnected() {
        serverStateNormal();

        serverAddress.setSensitive(true);

        String disconnectedString = Translations.getText("Disconnected");
        setServerGroupSuffixStyle(Constants.UI.CSS.DISCONNECTED_CSS);
        serverConnectivityButtonLabel.setLabel(disconnectedString);
        serverConnectivityButton.setTooltipText(disconnectedString);

        markServerSettingsUnavailable();
    }

    public void serverStateNormal() {
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
        serverConnectivityButtonLabel.setLabel(Translations.getText("Connecting"));
    }

    private void setServerStateDisconnecting() {
        serverStateChanging();
        serverConnectivityButtonLabel.setLabel(Translations.getText("Disconnecting"));
    }

    private void serverStateChanging() {
        markServerSettingsUnavailable();
        serverConnectivityButton.setSensitive(false);
        serverConnectivityButton.setTooltipText(null);
        serverConnectivityButtonBox.setSpacing(8);
        serverConnectivityButtonSpinnerRevealer.setRevealChild(true);
        setServerGroupSuffixStyle(Constants.UI.CSS.CHANGING_CSS);
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
                        Thread.sleep(Constants.UI.Intervals.MINIMUM_DELAY);
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
                        SettingsDialog.this.setServerStateConnected();
                        requestSettings();
                    } else {
                        SettingsDialog.this.setServerStateDisconnected();
                    }
                } else {
                    LEDSuiteApplication.getLogger().info("Skipping connect attempt because communication websocket is already connected!", new LEDSuiteLogAreas.NETWORK());
                    SettingsDialog.this.setServerStateConnected();
                    requestSettings();
                }
            }
        }.runTaskLaterAsynchronously(100);
    }

    @GtkCallback(name = "settings_server_cnct_button_clicked")
    public void serverCnctButtonClicked() {
        if (!CooldownManager.call("serverConnectivityButtonCb")) {
            LEDSuiteApplication.getLogger().verbose("Connectivity button on cooldown!", new LEDSuiteLogAreas.USER_INTERACTIONS());
        }
    }

    @Contract(" -> new")
    public @NotNull SettingsData getData() {
        String temp = ((StringList<?>) colorMode.getModel()).getString(colorMode.getSelected());
        return new SettingsData(
                (int) brightness.getValue(),
                temp == null || temp.equals(Translations.getText("N/A")) ? null : temp,
                restorePreviousState.getActive()
        );
    }

    public void applyButtonCooldown() {
        String[] defaultCSS = applyButton.getCssClasses();
        List<String> cssFail = new java.util.ArrayList<>(Arrays.stream(defaultCSS).toList());
        cssFail.remove("suggested-action");
        cssFail.add("regular");
        applyButton.setCssClasses(cssFail.toArray(new String[]{}));
        String defaultLabel = applyButton.getLabel();
        applyButton.setLabel(Translations.getText("Slow down!"));
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
            this.setServerStateConnecting();
            long start = LEDSuiteApplication.getConnectionAttempt();
            new LEDSuiteRunnable() {
                @Override
                public void run() {
                    if (isServerConnected()) {
                        SettingsDialog.this.setServerStateConnected();
                        cancel();
                    }
                    if (System.currentTimeMillis() - start > Constants.UI.Intervals.CONNECTION_TIMEOUT) {
                        SettingsDialog.this.setServerStateDisconnected();
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

    @GtkChild(name = "settings_server_group_auth_button")
    public Button serverAuthButton;

    @GtkChild(name = "settings_server_group_auth_button_box")
    public Box serverAuthButtonBox;

    @GtkChild(name = "settings_server_group_auth_button_label")
    public Label serverAuthButtonLabel;

    @GtkChild(name = "settings_server_group_auth_button_spinner")
    public Spinner serverAuthButtonSpinner;

    @GtkChild(name = "settings_server_group_auth_button_spinner_revealer")
    public Revealer serverAuthButtonSpinnerRevealer;

    public void setAuthenticated(boolean authenticated, String username) {
       setAuthenticated(authenticated, username, false);
    }

    public void setAuthenticated(boolean authenticated, String username, boolean setAuthenticating) {
        GLib.idleAddOnce(() -> {
            if (authenticated) {
                if (username != null) {
                    serverAuthButtonLabel.setLabel(Translations.getText("Authenticated as $", username));
                } else {
                    // Prevent recursion by breaking the loop
                    setAuthenticated(false);
                    return;
                }
            } else {
                serverAuthButtonLabel.setLabel(Translations.getText("Authenticate"));
            }

            // Use predefined CSS class constants
            serverAuthButton.setCssClasses(authenticated ? new String[]{"success"} : new String[]{"suggested-action"});
            serverAuthButton.setSensitive(true);
            serverAuthButtonSpinnerRevealer.setRevealChild(false);

            serverAuthButtonBox.setSpacing(0);
            if (setAuthenticating) setAuthenticating();
        });
    }


    public void setAuthenticating() {
        serverAuthButtonLabel.setLabel(Translations.getText("Authenticating"));
        serverAuthButtonSpinnerRevealer.setRevealChild(true);
        serverAuthButtonBox.setSpacing(8);
        serverAuthButton.setSensitive(false);
    }

    @GtkCallback(name = "settings_server_auth_button_clicked")
    public void serverAuthButtonClicked() {
        if (LEDSuiteApplication.getWebSocketCommunication().isConnected()) {
            setAuthenticated(false, null, true);
            AuthenticationDialog dialog = AuthenticationDialog.create();
            dialog.present(getParent());
        }
    }

}
