package com.toxicstoxm.LEDSuite.event_handling;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.LEDSuite;
import com.toxicstoxm.LEDSuite.event_handling.listener.EventListener;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventManager {
    private final Map<Class<?>, List<RegisteredListener>> listeners = new HashMap<>();

    public void registerEvents(EventListener eventListener) {
        for (Method method : eventListener.getClass().getMethods()) {
            if (method.isAnnotationPresent(EventHandler.class) && method.getParameterCount() == 1) {

                Class<?> eventType = method.getParameterTypes()[0];
                listeners.computeIfAbsent(eventType, _ -> new ArrayList<>()).add(new RegisteredListener(eventListener, method));
                LEDSuite.logger.debug("Registering listener method: " +
                        eventListener.toString().split("@")[0] +
                        "." +
                        method.getName() +
                        "(" + eventType.getName().split("event_handling.")[1].replace("$", ".") +  ")");
            }
        }
    }

    public void unregisterEvents(EventListener eventListener) {
        for (Map.Entry<Class<?>, List<RegisteredListener>> entry : listeners.entrySet()) {
            List<RegisteredListener> registeredListeners = entry.getValue();
            registeredListeners.removeIf(registeredListener -> registeredListener.eventListener.equals(eventListener));
            // Clean up the list if it becomes empty
            if (registeredListeners.isEmpty()) {
                listeners.remove(entry.getKey());
            }
        }
    }

    // fires a new specified event
    // notifies all RegisteredListeners that listen for this specific event
    public void fireEvent(Object event) {
        String id = tryToGetNetworkID(event);

        LEDSuite.logger.debug(id + "Firing event: " + event);
        List<RegisteredListener> registeredListeners = listeners.get(event.getClass());
        List<RegisteredListener> toRemove = new ArrayList<>();
        if (registeredListeners != null) {
            // loops through all registeredListeners
            for (RegisteredListener registeredListener : registeredListeners) {
                try {
                    // calls all methods from the current listener that listen for this specific event
                    Class<?> eventType = registeredListener.method.getParameterTypes()[0];
                    LEDSuite.logger.debug("Calling listener method: " +
                            registeredListener.eventListener.toString().split("@")[0] +
                            "." +
                            registeredListener.method.getName() +
                            "(" + eventType.getName().split("event_handling.")[1].replace("$", ".") +  ")");
                    registeredListener.method.invoke(registeredListener.eventListener, event);
                } catch (Exception e) {
                    try {
                        LEDSuite.logger.warn(id + "Error while trying to fire event: " + event);
                        LEDSuite.logger.warn(id + "This warning can be ignored!");
                        LEDSuite.logger.debug(id + "Stack trace: ");
                        LEDSuite.logger.error(e);
                        toRemove.add(registeredListener);
                        //registeredListeners.remove(registeredListener);
                    } catch (Exception ex) {
                        LEDSuite.logger.warn("Error getting error message!");
                    }
                }
            }
            registeredListeners.removeAll(toRemove);
        }
    }

    // trying to a brute force event type and get id if a specific event has one, else return empty string
    private String tryToGetNetworkID(Object event) {
        String id = "";
        try {
            if (event instanceof Events.DataIn) {
                id = String.valueOf(((Events.DataIn) event).yamlMessage().getNetworkID());
            } else if (event instanceof Events.DataOut) {
                id = String.valueOf(((Events.DataOut) event).yaml().getProperty(Constants.Network.YAML.INTERNAL_NETWORK_ID));
            } else if (event instanceof Events.Status) {
                id = String.valueOf(((Events.Status) event).statusUpdate().getNetworkEventID());
            }
        } catch (Exception e) {
            return id;
        }
        return id.isBlank() ? "" : "[" + id + "] ";
    }


    // RegisteredListener Object to keep track of listener classes and their specific listeners
    private record RegisteredListener(EventListener eventListener, Method method) {

    }
}
