package com.x_tornado10.lccp.event_handling;

import com.x_tornado10.lccp.LCCP;
import com.x_tornado10.lccp.event_handling.listener.EventListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventManager {
    private final Map<Class<?>, List<RegisteredListener>> listeners = new HashMap<>();

    public void registerEvents(EventListener eventListener) {
        LCCP.logger.debug(eventListener.toString());
        for (Method method : eventListener.getClass().getMethods()) {
            if (method.isAnnotationPresent(EventHandler.class) && method.getParameterCount() == 1) {
                LCCP.logger.debug(method.getName());
                Class<?> eventType = method.getParameterTypes()[0];
                listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(new RegisteredListener(eventListener, method));
            }
        }
    }

    public void fireEvent(Object event) {
        List<RegisteredListener> registeredListeners = listeners.get(event.getClass());
        if (registeredListeners != null) {
            for (RegisteredListener registeredListener : registeredListeners) {
                try {
                    registeredListener.getMethod().invoke(registeredListener.getEventListener(), event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private record RegisteredListener(EventListener eventListener, Method method) {
        public EventListener getEventListener() {
            return eventListener;
        }

        public Method getMethod() {
            return method;
        }
    }
}
