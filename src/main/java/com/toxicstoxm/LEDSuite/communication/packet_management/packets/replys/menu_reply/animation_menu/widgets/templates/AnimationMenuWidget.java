package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.widgets.templates;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.ErrorCode;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.DeserializableWidget;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.WidgetType;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.widgets.row_widgets.*;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.widgets.special_widgets.ButtonWidget;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.MenuChangeRequestPacket;
import com.toxicstoxm.LEDSuite.communication.websocket.WebSocketClient;
import com.toxicstoxm.LEDSuite.gettext.Translations;
import com.toxicstoxm.LEDSuite.tools.YamlTools;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs.ErrorData;
import com.toxicstoxm.YAJL.Logger;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.ConfigurationSection;
import lombok.Getter;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.Widget;
import org.jetbrains.annotations.NotNull;

/**
 * Wrapper class for animation menu widgets.
 * This class includes some useful methods for the child classes.
 * @since 1.0.0
 */
@Getter
public abstract class AnimationMenuWidget<T extends Widget> implements com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.Widget {

    private static final Logger logger = Logger.autoConfigureLogger();

    protected String widgetID;

    protected String animationName;

    protected ConfigurationSection widgetSection;

    protected T widget;

    @Override
    public abstract String getType();

    public abstract Type getWidgetType();

    @Override
    public DeserializableWidget serialize() {
        return save();
    }

    protected DeserializableWidget save() {
        YamlConfiguration yaml = new YamlConfiguration();
        LEDSuiteApplication.handleError(
                ErrorData.builder()
                        .message(Translations.getText("Serialization of animation menu widgets is not supported!"))
                        .logger(logger)
                        .build()
        );
        yaml.set(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE, getWidgetID());
        return DeserializableWidget.builder()
                .widgetSection(yaml)
                .widgetKey(getWidgetID())
                .animationName(getAnimationName())
                .build();
    }

    public AnimationMenuWidget<?> cpy() {
        String type = getType();
        return switch (WidgetType.valueOf(type.toUpperCase())) {
            case GROUP -> null;
            case BUTTON_ROW -> new ButtonRowWidget();
            case BUTTON -> new ButtonWidget();
            case ENTRY_ROW -> new EntryRowWidget();
            case PROPERTY_ROW -> new PropertyRowWidget();
            case COMBO_ROW -> new ComboRowWidget();
            case SWITCH_ROW -> new SwitchRowWidget();
            case SPIN_ROW -> new SpinRowWidget();
            case EXPANDER_ROW -> new ExpanderRowWidget();
        };
    }

    protected void sendMenuChangeRequest(Object objectValue) {
        if (LEDSuiteApplication.getWindow().checkRenamePending(getAnimationName())) {
            logger.debug("Denied menu change request from '{}', because a rename request for that animation is pending!", getAnimationName());
            return;
        }
        try {
            getChangeCallback().enqueueMessage(
                    MenuChangeRequestPacket.builder()
                            .fileName(getAnimationName())
                            .objectId(getWidgetID())
                            .objectValue(objectValue)
                            .build().serialize()
            );
        } catch (NullPointerException e) {
            logger.warn(e.getMessage());
        }
    }

    protected void sendMenuChangeRequestWithoutValue() {
        if (LEDSuiteApplication.getWindow().checkRenamePending(getAnimationName())) {
            logger.debug("Denied menu change request from '{}', because a rename request for that animation is pending!", getAnimationName());
            return;
        }
        try {
            getChangeCallback().enqueueMessage(
                    MenuChangeRequestPacket.builder()
                            .fileName(getAnimationName())
                            .objectId(getWidgetID())
                            .build().serialize()
            );
        } catch (NullPointerException e) {
            logger.warn(e.getMessage());
        }
    }

    protected WebSocketClient getChangeCallback() {
        WebSocketClient webSocketClient = LEDSuiteApplication.getWebSocketCommunication();
        if (webSocketClient == null) {
            throw new NullPointerException("Wasn't able to send menu change request. Not connected to server.");
        }
        return webSocketClient;
    }

    @Override
    public T deserialize(@NotNull DeserializableWidget deserializableWidget) throws DeserializationException {
        this.widgetID = deserializableWidget.widgetKey();
        widgetSection = deserializableWidget.widgetSection();
        if (widgetSection == null || deserializableWidget.animationName() == null) throw new DeserializationException("Widget section for widget '" + getWidgetID() + "' is null or invalid!", ErrorCode.WidgetSectionEmptyOrMissing);

        widget = GObject.newInstance(getWidgetType());

        this.animationName = deserializableWidget.animationName();
        return widget;
    }

    protected boolean checkIfKeyExists(String key) {
        return YamlTools.checkIfKeyExists(key, widgetSection);
    }

    protected void ensureKeyExists(String key) {
        YamlTools.ensureKeyExists(key, widgetSection);
    }

    protected boolean checkIfKeyExists(String key, @NotNull ConfigurationSection yaml) {
        return YamlTools.checkIfKeyExists(key, yaml);
    }

    protected void ensureKeyExists(String key, @NotNull ConfigurationSection yaml) {
        YamlTools.ensureKeyExists(key, yaml);
    }

    protected void onChanged(ChangedCallback changedCallback) {
        widget.onNotify(null, _ -> changedCallback.onChanged());
    }

    protected String getStringIfAvailable(String key) {
        return YamlTools.getStringIfAvailable(key, widgetSection);
    }

    protected String getStringIfAvailable(String key, String defaultValue) {
        return YamlTools.getStringIfAvailable(key, defaultValue, widgetSection);
    }

    protected boolean getBooleanIfAvailable(String key) {
        return YamlTools.getBooleanIfAvailable(key, widgetSection);
    }

    protected boolean getBooleanIfAvailable(String key, boolean defaultValue) {
        return YamlTools.getBooleanIfAvailable(key, defaultValue, widgetSection);
    }

    protected int getIntIfAvailable(String key) {
        return YamlTools.getIntIfAvailable(key, widgetSection);
    }

    protected int getIntIfAvailable(String key, int defaultValue) {
        return YamlTools.getIntIfAvailable(key, defaultValue, widgetSection);
    }

    protected double getDoubleIfAvailable(String key) {
        return YamlTools.getDoubleIfAvailable(key, widgetSection);
    }

    protected double getDoubleIfAvailable(String key, double defaultValue) {
        return YamlTools.getDoubleIfAvailable(key, defaultValue, widgetSection);
    }

    protected long getLongIfAvailable(String key) {
        return YamlTools.getLongIfAvailable(key, widgetSection);
    }

    protected long getLongIfAvailable(String key, long defaultValue) {
        return YamlTools.getLongIfAvailable(key, defaultValue, widgetSection);
    }
}
