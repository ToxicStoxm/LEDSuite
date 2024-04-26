package com.x_tornado10.Events.Events;

public class StartupEvent extends Event{
    @Override
    public TYPE getType() {
        return TYPE.STARTUP;
    }
}
