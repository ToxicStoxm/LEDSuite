package com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.SettingsRequestPacket;
import com.toxicstoxm.LEDSuite.communication.websocket.WebSocketClient;
import com.toxicstoxm.LEDSuite.settings.LEDSuiteSettingsBundle;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import com.toxicstoxm.LEDSuite.time.Action;
import com.toxicstoxm.LEDSuite.tools.UITools;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.LEDSuite.ui.dialogs.ProviderCallback;
import com.toxicstoxm.LEDSuite.ui.dialogs.UpdateCallback;
import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.Types;
import lombok.Getter;
import org.gnome.adw.*;
import org.gnome.glib.GLib;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.Button;
import org.gnome.gtk.Gtk;
import org.gnome.gtk.StringList;
import org.gnome.gtk.Widget;
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

    private static final Type gtype = Types.register(SettingsDialog.class);

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
        settingsDialog.markEverythingUnavailable();
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

    @GtkChild(name = "settings_supported_color_modes")
    public StringList supportedColorModes;

    @GtkChild(name = "settings_apply_button")
    public Button applyButton;

    private void setBrightness(Integer brightness) {
        if (brightness == null) {
            this.brightness.setEditable(false);
            UITools.markUnavailableWithoutSubtitle(this.brightness);
        } else {
            this.brightness.setValue(brightness);
            UITools.markAvailable(this.brightness);
            this.brightness.setEditable(true);
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
            UITools.markUnavailableWithoutSubtitle(this.colorMode);
            setColorMode(null);
        } else if (supportedColorModes.size() < 2) {
            UITools.markUnavailableWithoutSubtitle(this.colorMode);
            setColorMode(1);
        }
        else {
            this.supportedColorModes = StringList.builder()
                    .setStrings(supportedColorModes.toArray(new String[]{}))
                    .build();
            UITools.markAvailable(this.colorMode);
            setColorMode(selectedColorMode);
        }

    }

    private void initialize() {
        markEverythingUnavailable();
        serverAddress.setText(LEDSuiteSettingsBundle.WebsocketURI.getInstance().get());
    }

    private void markEverythingUnavailable() {
        UITools.markAllUnavailableWithoutSubtitle(
                List.of(
                        brightness,
                        colorMode
                )
        );
        brightness.setEditable(false);
    }

    private void update(@NotNull SettingsUpdate settingsUpdate) {
        GLib.idleAddOnce(() -> {
            setSupportedColorModes(settingsUpdate.supportedColorModes(), settingsUpdate.selectedColorMode());
            setBrightness(settingsUpdate.brightness());
            WebSocketClient webSocketClient = LEDSuiteApplication.getWebSocketCommunication();
            if (webSocketClient != null && webSocketClient.isConnected()) {
                System.out.println("shit");
                serverGroup.setOpacity(Constants.UI.DEFAULT_OPACITY);
            } else serverGroup.setOpacity(Constants.UI.REDUCED_OPACITY);
        });
    }

    @Contract(" -> new")
    private @NotNull SettingsData getData() {
        String temp = supportedColorModes.getString(colorMode.getSelected());
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

        new LEDSuiteRunnable() {
            @Override
            public void run() {
                GLib.idleAddOnce(() -> {
                    applyButton.setCssClasses(defaultCSS);
                    applyButton.setLabel(defaultLabel);
                });
            }
        }.runTaskLaterAsynchronously(500);
    }

    @Override
    public void present(@Nullable Widget parent) {
        LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                SettingsRequestPacket.builder().build().serialize()
        );
        super.present(parent);
    }
}
