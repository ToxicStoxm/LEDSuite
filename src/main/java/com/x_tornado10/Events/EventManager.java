package com.x_tornado10.Events;

import com.x_tornado10.Main;

import java.util.ArrayList;
import java.util.List;

public class EventManager {
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
    public void notifyListeners() {
        for (EventListener listener : listeners) {
            listener.onReload();
        }
    }

    // sending a new reload event
    public void sendReload() {
        Main.logger.info("Reloading...");
        notifyListeners();
        Main.logger.info("Successfully reloaded!");
    }
}
