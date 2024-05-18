package com.x_tornado10.lccp.Events.Events;

public class StartupEvent extends Event{
    @Override
    public TYPE getType() {
        return TYPE.STARTUP;
    }
}
