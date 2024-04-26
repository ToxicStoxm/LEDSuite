package com.x_tornado10.Events.Events;

import com.x_tornado10.Settings.Settings;

public class ReloadEvent extends Event {

    private final Settings.Type menu;
    public ReloadEvent(Settings.Type menu) {
        this.menu = menu;
    }

    public boolean hasRebootDest() {
        return menu != Settings.Type.UNDEFINED;
    }

    public Settings.Type getRebootDest() {
        if (!hasRebootDest()) return null;
        return menu;
    }

    @Override
    public TYPE getType() {
        return TYPE.RELOAD;
    }
}
