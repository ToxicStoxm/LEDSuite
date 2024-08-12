package com.toxicstoxm.LEDSuite.settings;

import com.toxicstoxm.LEDSuite.LEDSuite;
import com.toxicstoxm.LEDSuite.event_handling.EventHandler;
import com.toxicstoxm.LEDSuite.event_handling.Events;
import com.toxicstoxm.LEDSuite.event_handling.listener.EventListener;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.configuration2.YAMLConfiguration;

import java.io.IOException;

/**
 * The `Settings` class provides a template for configuration settings.
 * It implements the `EventListener` interface to handle specific events.
 *
 * @since 1.0.0
 */
@Setter
@Getter
public class Settings implements EventListener {
    // Type of settings (LOCAL, SERVER, or UNDEFINED)
    private Type type = Type.UNDEFINED;
    // Name of the configuration file
    private String name = "Universal-Config-File";

    /**
     * Enum representing the type of settings.
     *
     * @since 1.0.0
     */
    public enum Type {
        LOCAL,
        SERVER,
        UNDEFINED
    }

    /**
     * Saves the default configuration. To be implemented by subclasses.
     *
     * @throws IOException if an I/O error occurs.
     * @throws NullPointerException if a required value is missing.
     * @since 1.0.0
     */
    public void saveDefaultConfig() throws IOException, NullPointerException {}

    /**
     * Loads the configuration from a given `YAMLConfiguration` object.
     *
     * @param config The `YAMLConfiguration` object containing the configuration settings.
     * @since 1.0.0
     */
    public void load(YAMLConfiguration config) {
        LEDSuite.logger.verbose("[" + name + "] Loading settings for '" + type.name() + "' config.");
    }

    /**
     * Saves the current settings. To be implemented by subclasses.
     *
     * @since 1.0.0
     */
    public void save() {}

    /**
     * Copies settings from another `Settings` instance.
     *
     * @param settings The `Settings` instance to copy from.
     * @since 1.0.0
     */
    public void copy(Settings settings) {}

    /**
     * Reloads the configuration and fires a reload event.
     *
     * @param changes The description of changes that triggered the reload.
     * @since 1.0.0
     */
    public void reload(String changes) {
        // Fire an event indicating that the settings have been reloaded
        LEDSuite.eventManager.fireEvent(new Events.Reload(getName() + " changed! Changes: " + changes));
    }

    /**
     * Creates a clone of the current settings.
     *
     * @return A new `Settings` instance with copied values.
     * @since 1.0.0
     */
    public Settings cloneS() {
        // Create a new Settings instance
        Settings settings1 = new Settings();
        // Copy the current settings to the new instance
        settings1.copy(this);
        return settings1;
    }

    /**
     * Method to be called on startup. To be implemented by subclasses.
     *
     * @since 1.0.0
     */
    public void startup() {}

    /**
     * Event handler for the `Save` event.
     *
     * @param e The `Events.Save` event instance.
     * @since 1.0.0
     */
    @EventHandler
    public void onSave(Events.Save e) {
        // Call the save method when a save event is fired
        this.save();
    }

    /**
     * Event handler for the `Startup` event.
     *
     * @param e The `Events.Startup` event instance.
     * @since 1.0.0
     */
    @EventHandler
    public void onStartup(Events.Startup e) {
        // Call the startup method when a startup event is fired
        this.startup();
    }
}
