package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.widgets.templates;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.ErrorCode;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.DeserializableWidget;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.MenuChangeRequestPacket;
import com.toxicstoxm.LEDSuite.communication.websocket.WebSocketClient;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.tools.YamlTools;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.ConfigurationSection;
import lombok.Getter;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.StateFlags;
import org.gnome.gtk.Widget;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class AnimationMenuWidget<T extends Widget> implements com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.Widget {

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
        LEDSuiteApplication.getLogger().error("Serialization of animation menu widgets is not supported!", new LEDSuiteLogAreas.UI_CONSTRUCTION());
        yaml.set(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE, getWidgetID());
        return new DeserializableWidget(
                yaml,
                getWidgetID(),
                getAnimationName()

        );
    }

    protected void sendMenuChangeRequest(String objectValue) {
        try {
            getChangeCallback().enqueueMessage(
                    MenuChangeRequestPacket.builder()
                            .fileName(getAnimationName())
                            .objectId(getWidgetID())
                            .objectValue(objectValue)
                            .build().serialize()
            );
        } catch (NullPointerException e) {
            LEDSuiteApplication.getLogger().warn(e.getMessage());
        }
    }

    protected void sendMenuChangeRequestWithoutValue() {
        try {
            getChangeCallback().enqueueMessage(
                    MenuChangeRequestPacket.builder()
                            .fileName(getAnimationName())
                            .objectId(getWidgetID())
                            .build().serialize()
            );
        } catch (NullPointerException e) {
            LEDSuiteApplication.getLogger().warn(e.getMessage());
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
        widget.onStateFlagsChanged(flags -> {
           if (flags.contains(StateFlags.ACTIVE)) changedCallback.onChanged();
        });
    }

    protected String getStringIfAvailable(String key) {
        if (checkIfKeyExists(key)) {
            return widgetSection.getString(key);
        }
        return "";
    }

    protected String getStringIfAvailable(String key, String defaultValue) {
        if (checkIfKeyExists(key)) {
            return widgetSection.getString(key);
        }
        return defaultValue;
    }

    protected boolean getBooleanIfAvailable(String key) {
        if (checkIfKeyExists(key)) {
            return widgetSection.getBoolean(key);
        }
        return false;
    }

    protected boolean getBooleanIfAvailable(String key, boolean defaultValue) {
        if (checkIfKeyExists(key)) {
            return widgetSection.getBoolean(key);
        }
        return defaultValue;
    }

    protected int getIntIfAvailable(String key) {
        if (checkIfKeyExists(key)) {
            return widgetSection.getInt(key);
        }
        return 0;
    }

    protected int getIntIfAvailable(String key, int defaultValue) {
        if (checkIfKeyExists(key)) {
            return widgetSection.getInt(key);
        }
        return defaultValue;
    }

    protected double getDoubleIfAvailable(String key) {
        if (checkIfKeyExists(key)) {
            return widgetSection.getDouble(key);
        }
        return 0;
    }

    protected double getDoubleIfAvailable(String key, double defaultValue) {
        if (checkIfKeyExists(key)) {
            return widgetSection.getDouble(key);
        }
        return defaultValue;
    }

    protected long getLongIfAvailable(String key) {
        if (checkIfKeyExists(key)) {
            return widgetSection.getLong(key);
        }
        return 0;
    }

    protected long getLongIfAvailable(String key, long defaultValue) {
        if (checkIfKeyExists(key)) {
            return widgetSection.getLong(key);
        }
        return defaultValue;
    }
}
