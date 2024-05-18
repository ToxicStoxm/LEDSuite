package com.x_tornado10.lccp.Events;

import com.x_tornado10.lccp.Events.Events.Event;
import com.x_tornado10.lccp.Events.Events.ReloadEvent;
import com.x_tornado10.lccp.LCCP;
import com.x_tornado10.lccp.Settings.Settings;

import java.util.ArrayList;
import java.util.List;

public class EventManager {

    public EventManager() {
        addEventListener(new LCCP.main_listener());
    }
    // list of all listeners that need to be notified on events
    private final List<EventListener> listeners = new ArrayList<>();

    // adding a listener to the notify list
    public void addEventListener(EventListener listener) {
        listeners.add(listener);
    }

    // removing a listener from the notify list
    public void removeEventListener(EventListener listener) {
        listeners.remove(listener);
    }

    // notifying all listeners in the notify list if an event is fired
    public void notifyListeners(Event event) {
        for (EventListener listener : listeners) {
            if (listener != null) listener.onEvent(event);
        }
    }

    // sending a new reload event
    public void sendReload(Settings.Type menu) {
        LCCP.logger.info("Reloading...");
        newEvent(new ReloadEvent(menu));
        LCCP.logger.info("Successfully reloaded!");
    }
    public void newEvent(Event event) {
        notifyListeners(event);
    }
}
