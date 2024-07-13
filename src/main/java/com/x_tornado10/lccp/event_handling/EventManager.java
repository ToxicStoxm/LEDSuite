package com.x_tornado10.lccp.event_handling;

import com.x_tornado10.lccp.LCCP;
import com.x_tornado10.lccp.event_handling.listener.EventListener;
import com.x_tornado10.lccp.Constants;

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
                LCCP.logger.debug("Registering listener method: " +
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

        LCCP.logger.debug(id + "Firing event: " + event);
        List<RegisteredListener> registeredListeners = listeners.get(event.getClass());
        if (registeredListeners != null) {
            // loops through all registeredListeners
            for (RegisteredListener registeredListener : registeredListeners) {
                try {
                    // calls all methods from the current listener that listen for this specific event
                    registeredListener.method.invoke(registeredListener.eventListener, event);
                } catch (Exception e) {
                    try {
                        LCCP.logger.warn(id + "Error while trying to fire event: " + event);
                        LCCP.logger.warn(id + "This warning can be ignored!");
                        LCCP.logger.debug(id + "Stack trace: ");
                        for (StackTraceElement s : e.getStackTrace()) {
                            LCCP.logger.debug(id + s.toString());
                            registeredListeners.remove(registeredListener);
                        }
                    } catch (Exception ex) {
                        LCCP.logger.warn("Error getting error message!");
                    }
                }
            }
        }
    }

    // trying to brute force event type and get id if specific event has one, else return empty string
    private String tryToGetNetworkID(Object event) {
        String id = "";
        try {
            if (event instanceof Events.DataIn) {
                id = String.valueOf(((Events.DataIn) event).yamlMessage().getNetworkID());
            } else if (event instanceof Events.DataOut) {
                id = String.valueOf(((Events.DataOut) event).yaml().getProperty(Constants.NETWORK.YAML.INTERNAL_NETWORK_EVENT_ID));
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
