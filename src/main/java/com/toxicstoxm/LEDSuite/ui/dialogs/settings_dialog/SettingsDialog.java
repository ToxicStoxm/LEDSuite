package com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.SettingsRequestPacket;
import com.toxicstoxm.LEDSuite.communication.websocket.WebSocketClient;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.settings.LEDSuiteSettingsBundle;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import com.toxicstoxm.LEDSuite.time.Action;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.LEDSuite.ui.dialogs.ProviderCallback;
import com.toxicstoxm.LEDSuite.ui.dialogs.UpdateCallback;
import io.github.jwharm.javagi.gobject.types.Types;
import io.github.jwharm.javagi.gtk.annotations.GtkCallback;
import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.TemplateTypes;
import lombok.Getter;
import org.gnome.adw.*;
import org.gnome.adw.Spinner;
import org.gnome.gio.ListModel;
import org.gnome.glib.GLib;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.*;
import org.gnome.gtk.Box;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
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
    private Action applyFail;

    public SettingsDialog(MemorySegment address) {
        super(address);
        updater = this::update;
        provider = this::getData;
        applyFail = this::applyFail;
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

    private void initialize() {
        markServerSettingsUnavailable();
        serverAddress.setText(LEDSuiteSettingsBundle.WebsocketURI.getInstance().get());
        serverAddress.setSensitive(false);
        updateServerState();
    }

    private void markServerSettingsUnavailable() {
        this.colorMode.setSensitive(false);
        brightness.setSensitive(false);
        applyButton.setSensitive(false);
    }

    private void update(@NotNull SettingsUpdate settingsUpdate) {
        GLib.idleAddOnce(() -> {
            setSupportedColorModes(settingsUpdate.supportedColorModes(), settingsUpdate.selectedColorMode());
            setBrightness(settingsUpdate.brightness());
            updateServerState();
        });
    }

    private void setServerStateConnected() {
        setServerStateFixedState();
        applyButton.setSensitive(true);
        setServerGroupSuffixStyle(Constants.UI.SettingsDialog.CONNECTED_CSS);
        serverConnectivityButtonLabel.setLabel(Constants.UI.SettingsDialog.CONNECTED);
        serverConnectivityButton.setTooltipText(Constants.UI.SettingsDialog.CONNECTED_TOOLTIP);
    }

    private void setServerStateDisconnected() {
        setServerStateFixedState();
        setServerGroupSuffixStyle(Constants.UI.SettingsDialog.DISCONNECTED_CSS);
        serverConnectivityButtonLabel.setLabel(Constants.UI.SettingsDialog.DISCONNECTED);
        serverConnectivityButton.setTooltipText(Constants.UI.SettingsDialog.DISCONNECTED_TOOLTIP);
    }

    private void setServerStateConnecting() {
        setServerStateChanging();
        serverConnectivityButtonLabel.setLabel(Constants.UI.SettingsDialog.CONNECTING);
    }

    private void setSeverStateDisconnecting() {
        setServerStateChanging();
        serverConnectivityButtonLabel.setLabel(Constants.UI.SettingsDialog.DISCONNECTING);
    }

    private void setServerStateChanging() {
        markServerSettingsUnavailable();
        serverConnectivityButton.setSensitive(false);
        serverConnectivityButton.setTooltipText(null);
        serverConnectivityButtonBox.setSpacing(8);
        serverConnectivityButtonSpinnerRevealer.setRevealChild(true);
        setServerGroupSuffixStyle(Constants.UI.SettingsDialog.CHANGING_CSS);
    }

    private void setServerStateFixedState() {
        serverConnectivityButton.setSensitive(true);
        serverConnectivityButtonBox.setSpacing(0);
        serverConnectivityButtonSpinnerRevealer.setRevealChild(false);
    }

    private void setServerGroupSuffixStyle(String[] css) {
        serverConnectivityButton.setCssClasses(css);
        serverConnectivityButtonLabel.setCssClasses(css);
    }

    private void triggerDisconnect() {
        new LEDSuiteRunnable() {
            @Override
            public void run() {
                WebSocketClient webSocketClient = LEDSuiteApplication.getWebSocketCommunication();
                if (webSocketClient == null || !webSocketClient.isConnected()) {
                    GLib.idleAddOnce(() -> {
                        LEDSuiteApplication.getLogger().info("Skipping disconnect attempt because communication websocket is already disconnected!", new LEDSuiteLogAreas.NETWORK());
                        setServerStateDisconnected();
                    });
                } else {
                    webSocketClient.shutdown();
                    try {
                        Thread.sleep(Constants.UI.SettingsDialog.MINIMUM_DELAY);
                    } catch (InterruptedException e) {
                        LEDSuiteApplication.getLogger().warn("Minimum delay sleeper was interrupted!", new LEDSuiteLogAreas.USER_INTERACTIONS());
                    } finally {
                        GLib.idleAddOnce(() -> setServerStateDisconnected());
                    }
                }
            }
        }.runTaskLaterAsynchronously(100);
    }

    private void triggerConnect() {
        new LEDSuiteRunnable() {
            @Override
            public void run() {
                WebSocketClient webSocketClient = LEDSuiteApplication.getWebSocketCommunication();
                if (webSocketClient == null || !webSocketClient.isConnected()) {
                    if (LEDSuiteApplication.startCommunicationSocket()) {
                        GLib.idleAddOnce(() -> setServerStateConnected());
                        requestSettings();
                    } else {
                        GLib.idleAddOnce(() -> setServerStateDisconnected());
                    }
                } else {
                    GLib.idleAddOnce(() -> {
                        LEDSuiteApplication.getLogger().info("Skipping connect attempt because communication websocket is already connected!", new LEDSuiteLogAreas.NETWORK());
                        setServerStateConnected();
                        requestSettings();
                    });
                }
            }
        }.runTaskLaterAsynchronously(100);
    }


    @GtkCallback(name = "settings_server_connectivity_button_cb")
    public void serverConnectivityButtonCb() {
        if (serverConnectivityButtonLabel.getLabel().equals(Constants.UI.SettingsDialog.CONNECTED)) {
            setSeverStateDisconnecting();
            triggerDisconnect();
        } else {
            setServerStateConnecting();
            triggerConnect();
        }
    }

    private void updateServerState() {
        if (!serverConnectivityButton.isSensitive()) return;
        WebSocketClient webSocketClient = LEDSuiteApplication.getWebSocketCommunication();
        if (webSocketClient != null && webSocketClient.isConnected()) {
            setServerStateConnected();
        } else {
            setServerStateDisconnected();
        }
    }

    @Contract(" -> new")
    private @NotNull SettingsData getData() {
        String temp = ((StringList<?>) colorMode.getModel()).getString(colorMode.getSelected());
        return new SettingsData(
                (int) brightness.getValue(),
                temp == null || temp.equals(Constants.UI.NOT_AVAILABLE_VALUE) ? null : temp
        );
    }

    private void applyFail() {
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
        requestSettings();
        super.present(parent);
    }
}
