package com.x_tornado10.lccp.event_handling;

import com.x_tornado10.lccp.LCCP;
import com.x_tornado10.lccp.event_handling.listener.EventListener;
import com.x_tornado10.lccp.util.Paths;
import com.x_tornado10.lccp.util.logging.Messages;

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
                listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(new RegisteredListener(eventListener, method));
                LCCP.logger.debug("Registering listener method: " +
                        eventListener.toString().split("@")[0] +
                        "." +
                        method.getName() +
                        "(" + eventType.getName().split("event_handling.")[1].replace("$", ".") +  ")");
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
                    registeredListener.getMethod().invoke(registeredListener.getEventListener(), event);
                } catch (Exception e) {
                    LCCP.logger.error(id + "Error while trying to fire event: " + event);
                    LCCP.logger.error("Stack trace: ");
                    for (StackTraceElement s : e.getStackTrace()) {
                        LCCP.logger.error(s.toString());
                    }
                    LCCP.logger.warn(Messages.WARN.OPEN_GITHUB_ISSUE);
                }
            }
        }
    }

    // trying to brute force event type and get id if specific event has one, else return empty string
    private String tryToGetNetworkID(Object event) {
        String id = "";
        try {
            id = String.valueOf(((Events.DataIn) event).yamlMessage().getNetworkEventID());
        } catch (Exception e) {
            try {
                id = String.valueOf(((Events.DataOut) event).yaml().getProperty(Paths.NETWORK.YAML.INTERNAL_NETWORK_EVENT_ID));
            } catch (Exception ex) {
                try {
                    id = String.valueOf(((Events.Status) event).statusUpdate().getNetworkEventID());
                } catch (Exception exc) {
                    return id;
                }
            }
        }
        return id.isBlank() ? "" : "[" + id + "] ";
    }


    // RegisteredListener Object to keep track of listener classes and their specific listeners
    private record RegisteredListener(EventListener eventListener, Method method) {
        public EventListener getEventListener() {
            return eventListener;
        }

        public Method getMethod() {
            return method;
        }
    }
}
