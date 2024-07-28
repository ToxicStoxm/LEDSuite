package com.toxicstoxm.LEDSuite.event_handling;

import com.toxicstoxm.LEDSuite.yaml_factory.YAMLMessage;
import com.toxicstoxm.LEDSuite.yaml_factory.wrappers.message_wrappers.ServerError;
import com.toxicstoxm.LEDSuite.yaml_factory.wrappers.message_wrappers.StatusUpdate;
import org.apache.commons.configuration2.YAMLConfiguration;

import java.util.HashMap;

/**
 * The `Events` class contains nested record types representing various events that
 * can be triggered within the LEDSuite application.
 *
 * <p>These events are intended to be used with an event handling system, where methods
 * annotated with `@EventHandler` will respond to the specific events.
 *
 * @see EventHandler
 * @see EventManager
 * @see com.toxicstoxm.LEDSuite.event_handling.listener.EventListener
 *
 * @since 1.0.0
 */
public class Events {

    /**
     * Event triggered when the application is reloaded.
     *
     * @param message The reload message.
     * @since 1.0.0
     */
    public record Reload(String message) {
    }

    /**
     * Event triggered when the application is saving config data (on shutdown).
     *
     * @param message The save message.
     * @since 1.0.0
     */
    public record Save(String message) {
    }

    /**
     * Event triggered when the application starts up.
     *
     * @param message The startup message.
     * @since 1.0.0
     */
    public record Startup(String message) {
    }

    /**
     * Event triggered when the application has started.
     *
     * @param message The started message.
     * @since 1.0.0
     */
    public record Started(String message) {
    }

    /**
     * Event triggered when the application shuts down.
     *
     * @param message The shutdown message.
     * @since 1.0.0
     */
    public record Shutdown(String message) {
    }

    /**
     * Event triggered when data is sent out.
     *
     * @param yaml The YAML configuration being sent out.
     * @since 1.0.0
     */
    public record DataOut(YAMLConfiguration yaml) {
    }

    /**
     * Event triggered when data is received.
     *
     * @param yamlMessage The received YAML message.
     * @since 1.0.0
     */
    public record DataIn(YAMLMessage yamlMessage) {
    }

    /**
     * Event triggered when there is a status update.
     *
     * @param statusUpdate The status update.
     * @since 1.0.0
     */
    public record Status(StatusUpdate statusUpdate) {
    }

    /**
     * Event triggered when there is a server error.
     *
     * @param serverError The server error.
     * @since 1.0.0
     */
    public record Error(ServerError serverError) {
    }

    /**
     * Event triggered when the host changes.
     *
     * @param message The host change message.
     * @since 1.0.0
     */
    public record HostChanged(String message) {
    }

    /**
     * Event triggered when a specific setting changes.
     *
     * @param key   The key of the changed setting.
     * @param value The new value of the setting.
     * @since 1.0.0
     */
    public record SettingChanged(String key, Object value) {
    }

    /**
     * Event triggered when multiple settings change.
     *
     * @param changedSettings A map of the changed settings.
     * @since 1.0.0
     */
    public record SettingsChanged(HashMap<String, Object> changedSettings) {
    }
}
