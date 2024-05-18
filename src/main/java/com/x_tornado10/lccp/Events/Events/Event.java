package com.x_tornado10.lccp.Events.Events;

public class Event {
    public TYPE getType() {return TYPE.UNDEFINED;}
    public enum TYPE{
        SAVE,
        RELOAD,
        STARTUP,
        UNDEFINED
    }
}
