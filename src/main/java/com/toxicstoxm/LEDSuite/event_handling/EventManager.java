package com.toxicstoxm.LEDSuite.event_handling;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.LEDSuite;
import com.toxicstoxm.LEDSuite.event_handling.listener.EventListener;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the registration, un-registration, and firing of events.
 *
 * <p>This class handles event listeners, registers their event handler methods,
 * and triggers these methods when specific events occur.
 *
 * @see EventHandler
 * @see EventListener
 * @see Events
 *
 * @since 1.0.0
 */
public class EventManager {
    // A map to store event types and their associated listeners
    private final Map<Class<?>, List<RegisteredListener>> listeners = new HashMap<>();

    /**
     * Registers all event handler methods from the provided event listener.
     *
     * @param eventListener the event listener containing event handler methods
     * @since 1.0.0
     */
    public void registerEvents(EventListener eventListener) {
        // Iterate over all methods in the event listener class
        for (Method method : eventListener.getClass().getMethods()) {
            // Check if the method is annotated with @EventHandler and has exactly one parameter
            if (method.isAnnotationPresent(EventHandler.class) && method.getParameterCount() == 1) {
                // Get the type of event the method listens for
                Class<?> eventType = method.getParameterTypes()[0];
                // Add the method to the listeners map for the specific event type
                listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(new RegisteredListener(eventListener, method));
                // Log the registration of the listener method
                LEDSuite.logger.verbose("Registering listener method: " +
                        eventListener.toString().split("@")[0] +
                        "." +
                        method.getName() +
                        "(" + eventType.getName().split("event_handling.")[1].replace("$", ".") + ")");
            }
        }
    }

    /**
     * Unregisters all event handler methods from the provided event listener.
     *
     * @param eventListener the event listener to unregister
     * @since 1.0.0
     */
    public void unregisterEvents(EventListener eventListener) {
        // Iterate over all entries in the listener map
        for (Map.Entry<Class<?>, List<RegisteredListener>> entry : listeners.entrySet()) {
            // Get the list of registered listeners for the current event type
            List<RegisteredListener> registeredListeners = entry.getValue();
            // Remove all listeners that belong to the provided event listener
            registeredListeners.removeIf(registeredListener -> registeredListener.eventListener.equals(eventListener));
            // Clean up the list if it becomes empty
            if (registeredListeners.isEmpty()) {
                listeners.remove(entry.getKey());
            }
        }
    }

    /**
     * Fires the specified event, notifying all registered listeners for this event.
     *
     * @param event the event to fire
     * @since 1.0.0
     */
    public void fireEvent(Object event) {
        // Attempt to retrieve the network ID from the event
        String id = tryToGetNetworkID(event);

        // Log the firing of the event
        LEDSuite.logger.verbose(id + "Firing event: " + event);
        // Get the list of registered listeners for the event's class
        List<RegisteredListener> registeredListeners = listeners.get(event.getClass());
        // Temporary list to keep track of listeners to remove
        List<RegisteredListener> toRemove = new ArrayList<>();
        // Check if there are any listeners for this event
        if (registeredListeners != null) {
            // Iterate over all registered listeners
            for (RegisteredListener registeredListener : registeredListeners) {
                try {
                    // Invoke the listener method with the event
                    Class<?> eventType = registeredListener.method.getParameterTypes()[0];
                    // Log the calling of the listener method
                    LEDSuite.logger.verbose("Calling listener method: " +
                            registeredListener.eventListener.toString().split("@")[0] +
                            "." +
                            registeredListener.method.getName() +
                            "(" + eventType.getName().split("event_handling.")[1].replace("$", ".") + ")");
                    // Invoke the method on the event listener
                    registeredListener.method.invoke(registeredListener.eventListener, event);
                } catch (Exception e) {
                    // Log any exception that occurs during method invocation
                    LEDSuite.logger.warn(id + "Error while trying to fire event: " + event);
                    LEDSuite.logger.warn(id + "This warning can be ignored!");
                    LEDSuite.logger.displayError(e);
                    // Add the listener to the removal list
                    toRemove.add(registeredListener);
                }
            }
            // Remove all listeners that encountered errors
            registeredListeners.removeAll(toRemove);
        }
    }

    /**
     * Attempts to retrieve the network ID from the event.
     *
     * @param event the event to extract the network ID from
     * @return the network ID as a string, or an empty string if not found
     * @since 1.0.0
     */
    private String tryToGetNetworkID(Object event) {
        String id = "";
        try {
            // Check the event type and extract the network ID accordingly
            if (event instanceof Events.DataIn) {
                id = String.valueOf(((Events.DataIn) event).yamlMessage().getNetworkID());
            } else if (event instanceof Events.DataOut) {
                id = String.valueOf(((Events.DataOut) event).yaml().getProperty(Constants.Network.YAML.INTERNAL_NETWORK_ID));
            } else if (event instanceof Events.Status) {
                id = String.valueOf(((Events.Status) event).statusUpdate().getNetworkEventID());
            }
        } catch (Exception e) {
            // Log any exception that occurs during network ID extraction
            LEDSuite.logger.error("Error extracting network ID: ");
            LEDSuite.logger.displayError(e);
        }
        // Return the network ID if found, else return an empty string
        return id.isBlank() ? "" : "[" + id + "] ";
    }

    /**
     * Represents a registered event listener with its associated method.
     * @since 1.0.0
     */
    private record RegisteredListener(EventListener eventListener, Method method) {
        // Record to keep track of listener classes and their specific listeners
    }
}
