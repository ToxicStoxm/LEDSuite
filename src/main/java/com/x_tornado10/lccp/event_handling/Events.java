package com.x_tornado10.lccp.event_handling;

import com.x_tornado10.lccp.yaml_factory.StatusUpdate;
import com.x_tornado10.lccp.yaml_factory.YAMLMessage;
import org.apache.commons.configuration2.YAMLConfiguration;

import java.util.UUID;

public class Events {
    public record Reload(String message) {
    }
    public record Save(String message) {
    }
    public record Startup(String message) {
    }
    public record Shutdown(String message) {
    }
    public record DataOut(YAMLConfiguration yaml) {
    }
    public record DataIn(YAMLMessage yamlMessage) {
    }
    public record Status(StatusUpdate statusUpdate) {
    }
}
