package com.toxicstoxm.lccp.yaml_factory.wrappers.message_wrappers;

import com.toxicstoxm.lccp.yaml_factory.YAMLMessage;
import lombok.Getter;

import java.util.*;


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
    private HashMap<String, String> availableAnimations;

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
                yamlMessage.getFileState(),
                yamlMessage.getFileSelected(),
                yamlMessage.getCurrentDraw(),
                yamlMessage.getVoltage(),
                yamlMessage.isLidState(),
                yamlMessage.getNetworkID(),
                yamlMessage.getAvailableAnimations()
        );
    }
    public static StatusUpdate notConnected() {
        return new StatusUpdate(
                false,
                null,
                null,
                0,
                0,
                false,
                blankUUID(),
                null
        );
    }
    private static final List<UUID> notConnected = new ArrayList<>();
    private static UUID blankUUID() {
        UUID uuid1 = UUID.randomUUID();
        notConnected.add(uuid1);
        return uuid1;
    }

    public boolean isNotConnected() {
        return notConnected.contains(uuid);
    }

    public String humanReadableLidState(boolean lidState) {
        return lidState ? "closed" : "open";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.isNotConnected()) return "Not connected!";
        sb.append("Current File: ");
        if (isFileLoaded) {
            sb.append("'").append(fileSelected).append("' ");
            sb.append("Current State: '").append(fileState.name()).append("' ");
        } else sb.append("no file selected ");
        sb.append("Current Draw: ").append(currentDraw).append(" ");
        sb.append("Voltage: ").append(voltage).append(" ");
        sb.append("Lid State: ").append(humanReadableLidState(lidState)).append(" ");
        if (!availableAnimations.isEmpty()) {
            sb.append("Available Animations: [");
            for (Map.Entry<String, String> entry : availableAnimations.entrySet()) {
                sb.append("Name: '").append(entry.getKey()).append("' GNOME Icon Name: '").append(entry.getValue()).append("', ");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.deleteCharAt(sb.length() - 1);

            sb.append("] ");
        } else sb.setLength(sb.length() - 1);

        return sb.toString();
    }

    @Override
    public int hashCode() {
        if (notConnected.contains(this.uuid)) return -1;
        return Objects.hash(isFileLoaded, fileState, fileSelected, currentDraw, voltage, lidState, availableAnimations);
    }

    public String minimal() {
        StringBuilder sb = new StringBuilder();
        sb.append("State: ");
        if (this.isNotConnected()) {
            sb.append("not connected");
        } else {
            if (isFileLoaded) {
                sb.append(fileState.name());
                sb.append("  |   Filename: ").append(fileSelected);
            } else sb.append("idle");
        }
        return sb.toString();
    }
}
