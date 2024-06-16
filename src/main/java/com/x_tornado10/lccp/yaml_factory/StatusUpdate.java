package com.x_tornado10.lccp.yaml_factory;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class StatusUpdate {
    private final UUID uuid;
    @Getter
    private final boolean isFileLoaded;
    @Getter
    private final YAMLMessage.FILE_STATE fileState;
    @Getter
    private final String fileSelected;
    @Getter
    private final double currentDraw;
    @Getter
    private final double voltage;
    @Getter
    private final boolean lidState;
    @Getter
    private HashMap<String, String> availableAnimations = new HashMap<>();


    public UUID getNetworkEventID() {
        return uuid;
    }

    private StatusUpdate(boolean isFileLoaded, YAMLMessage.FILE_STATE fileState, String fileSelected, double currentDraw, double voltage, boolean lidState, UUID uuid, HashMap<String, String> availableAnimations) {
        this.isFileLoaded = isFileLoaded;
        this.fileState = fileState;
        this.fileSelected = fileSelected;
        this.currentDraw = currentDraw;
        this.voltage = voltage;
        this.lidState = lidState;
        this.uuid = uuid;
        this.availableAnimations = availableAnimations;
    }

    public static StatusUpdate fromYAMLMessage(YAMLMessage yamlMessage) {
        return new StatusUpdate(
                yamlMessage.isFileLoaded(),
                YAMLMessage.FILE_STATE.playing,
                yamlMessage.getFileSelected(),
                yamlMessage.getCurrentDraw(),
                yamlMessage.getVoltage(),
                yamlMessage.isLidState(),
                yamlMessage.getNetworkEventID(),
                yamlMessage.getAvailableAnimations()
        );
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Current File: ");
        if (isFileLoaded) {
            sb.append("'").append(fileSelected).append("' ");
            sb.append("Current State: '").append(fileState).append("' ");
        } else sb.append("no file selected ");
        sb.append("Current Draw: ").append(currentDraw).append(" ");
        sb.append("Voltage: ").append(voltage).append(" ");
        sb.append("Lid State: ").append(lidState ? "closed" : "open").append(" ");
        if (!availableAnimations.isEmpty()) {
            sb.append("Available Animations: [");
            for (Map.Entry<String, String> entry : availableAnimations.entrySet()) {
                sb.append("Name: '").append(entry.getKey()).append("' Gnome Icon Name: '").append(entry.getValue()).append("', ");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.deleteCharAt(sb.length() - 1);

            sb.append("] ");
        } else sb.setLength(sb.length() - 1);

        return sb.toString();
    }
}
