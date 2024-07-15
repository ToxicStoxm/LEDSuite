package com.toxicstoxm.lccp.event_handling;

import com.toxicstoxm.lccp.yaml_factory.YAMLMessage;
import com.toxicstoxm.lccp.yaml_factory.wrappers.message_wrappers.ServerError;
import com.toxicstoxm.lccp.yaml_factory.wrappers.message_wrappers.StatusUpdate;
import org.apache.commons.configuration2.YAMLConfiguration;

import java.util.HashMap;

public class Events {
    public record Reload(String message) {
    }
    public record Save(String message) {
    }
    public record Startup(String message) {
    }
    public record Started(String message) {
    }
    public record Shutdown(String message) {
    }
    public record DataOut(YAMLConfiguration yaml) {
    }
    public record DataIn(YAMLMessage yamlMessage) {
    }
    public record Status(StatusUpdate statusUpdate) {
    }
    public record Error(ServerError serverError) {
    }
    public record HostChanged(String message) {
    }
    public record SettingChanged(String key, Object value) {
    }
    public record SettingsChanged(HashMap<String, Object> changedSettings) {
    }
}
