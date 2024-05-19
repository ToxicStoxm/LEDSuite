package com.x_tornado10.lccp.event_handling;

public class Events {
    public record Reload(String message) {
    }
    public record Save(String message) {
    }
    public record Startup(String message) {
    }
}
