package com.toxicstoxm.lccp.yaml_factory.wrappers.message_wrappers;

import com.toxicstoxm.lccp.yaml_factory.YAMLMessage;
import lombok.Getter;

import java.util.UUID;

public class ServerError {
    private final UUID uuid;
    @Getter
    private final YAMLMessage.ERROR_SOURCE errorSource;
    @Getter
    private final int errorCode;
    @Getter
    private final String errorName;
    @Getter
    private final YAMLMessage.ERROR_SEVERITY errorSeverity;

    public UUID getNetworkEventID() {
        return uuid;
    }

    public ServerError(UUID uuid, YAMLMessage.ERROR_SOURCE errorSource, int errorCode, String errorName, YAMLMessage.ERROR_SEVERITY errorSeverity) {
        this.uuid = uuid;
        this.errorSource = errorSource;
        this.errorCode = errorCode;
        this.errorName = errorName;
        this.errorSeverity = errorSeverity;
    }

    public static ServerError fromYAMLMessage(YAMLMessage yamlMessage) {
        return new ServerError(
                yamlMessage.getNetworkID(),
                yamlMessage.getErrorSource(),
                yamlMessage.getErrorCode(),
                yamlMessage.getErrorName(),
                yamlMessage.getErrorSeverity()
        );
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Name: '").append(getErrorName()).append("' ");
        sb.append("Source / Cause: '").append(getErrorSource()).append("' ");
        sb.append("Code: '").append(getErrorCode()).append("' ");
        sb.append("Severity: ").append(getErrorSeverity().getValue()).append("'");

        return sb.toString();
    }
    public String humanReadable() {
        StringBuilder sb = new StringBuilder();
        switch (getErrorSource()) {
            case invalid_request -> sb.append("Invalid request");
            case power -> sb.append("Power");
            case invalid_file -> sb.append("Invalid file");
            case other -> sb.append("Other / Unknown");
            case null, default -> sb.append("N/A");
        }
        sb.append(" error");
        if (!getErrorName().isEmpty()) sb.append(": ").append(getErrorName());
        return sb.toString();
    }
}
