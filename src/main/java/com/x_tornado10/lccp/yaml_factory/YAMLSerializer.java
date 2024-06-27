package com.x_tornado10.lccp.yaml_factory;

import com.x_tornado10.lccp.LCCP;
import com.x_tornado10.lccp.util.Paths;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.YAMLConfiguration;

import java.util.*;

public class YAMLSerializer {
    public static YAMLConfiguration serializeYAML(YAMLMessage yamlMessage) throws InvalidPacketTypeException, InvalidReplyTypeException, TODOException {
        YAMLMessage.PACKET_TYPE packetType = null;
        YAMLConfiguration result = new YAMLConfiguration();
        if (yamlMessage.getNetworkEventID() == null) yamlMessage.setUUID(UUID.randomUUID());
        result.setProperty(Paths.NETWORK.YAML.INTERNAL_NETWORK_EVENT_ID, String.valueOf(yamlMessage.getNetworkEventID()));
        try {
            packetType = YAMLMessage.PACKET_TYPE.valueOf(yamlMessage.getPacketTypeV());
        } catch (IllegalArgumentException e) {
            throw new InvalidPacketTypeException("Invalid packet type: " + packetType);
        }
        switch (packetType) {
            case reply -> result = serializeReplyYAML(yamlMessage);
            case error -> result = serializeErrorYAML(yamlMessage);
            case request -> result = serializeRequestYAML(yamlMessage);
            case null, default -> throw new InvalidPacketTypeException("Invalid packet type: " + packetType);
        }
        return result;
    }

    protected static YAMLConfiguration serializeReplyYAML(YAMLMessage yamlMessage) throws InvalidReplyTypeException, TODOException {
        YAMLMessage.REPLY_TYPE replyType = null;
        try {
            replyType = YAMLMessage.REPLY_TYPE.valueOf(yamlMessage.getReplyTypeV());
        } catch (IllegalArgumentException e) {
            throw new InvalidReplyTypeException("Invalid reply type: " + replyType);
        }
        switch (replyType) {
            case menu -> {
                return serializeMenuReplyYAML(yamlMessage);
            }
            case status -> {
                return serializeStatusReplyYAML(yamlMessage);
            }
            case null, default -> throw new InvalidReplyTypeException("Invalid reply type: " + replyType);
        }
    }
    protected static YAMLConfiguration serializeMenuReplyYAML(YAMLMessage yamlMessage) {
        YAMLConfiguration menuYaml = yamlMessage.getMenuYaml();
        if (yamlMessage.getPacketTypeV() != null) menuYaml.setProperty(Paths.NETWORK.YAML.PACKET_TYPE, yamlMessage.getPacketTypeV());
        if (yamlMessage.getReplyType() != null) menuYaml.setProperty(Paths.NETWORK.YAML.REPLY_TYPE, yamlMessage.getReplyTypeV());
        return menuYaml;
    }

    protected static YAMLConfiguration serializeStatusReplyYAML(YAMLMessage yamlMessage) {
        YAMLConfiguration yaml = new YAMLConfiguration();

        if (yamlMessage.getPacketTypeV() != null) yaml.setProperty(Paths.NETWORK.YAML.PACKET_TYPE, yamlMessage.getPacketTypeV());
        if (yamlMessage.getRequestTypeV() != null) yaml.setProperty(Paths.NETWORK.YAML.REPLY_TYPE, yamlMessage.getRequestTypeV());
        yaml.setProperty(Paths.NETWORK.YAML.FILE_IS_LOADED, yamlMessage.isFileLoaded());
        if (yamlMessage.getFileStateV() != null) yaml.setProperty(Paths.NETWORK.YAML.FILE_STATE, yamlMessage.getFileStateV());
        if (yamlMessage.getFileSelected() != null && !yamlMessage.getFileSelected().isBlank()) yaml.setProperty(Paths.NETWORK.YAML.FILE_SELECTED, yamlMessage.getFileSelected());
        yaml.setProperty(Paths.NETWORK.YAML.CURRENT_DRAW, yamlMessage.getCurrentDraw());
        yaml.setProperty(Paths.NETWORK.YAML.VOLTAGE, yamlMessage.getVoltage());
        if (yamlMessage.getFileStateV() != null) yaml.setProperty(Paths.NETWORK.YAML.LID_STATE, yamlMessage.isLidState());
        if (yamlMessage.getAvailableAnimations() != null && !yamlMessage.getAvailableAnimations().isEmpty()) {
            for (Map.Entry<String, String> entry : yamlMessage.getAvailableAnimations().entrySet()) {
                yaml.setProperty(Paths.NETWORK.YAML.AVAILABLE_ANIMATIONS + Paths.Config.SEPARATOR + entry.getKey(), entry.getValue());
            }
        }

        return yaml;
    }

    protected static YAMLConfiguration serializeErrorYAML(YAMLMessage yamlMessage) {
        YAMLConfiguration yaml = new YAMLConfiguration();

        if (yamlMessage.getPacketTypeV() != null) yaml.setProperty(Paths.NETWORK.YAML.PACKET_TYPE, yamlMessage.getPacketTypeV());
        if (yamlMessage.getErrorSourceV() != null) yaml.setProperty(Paths.NETWORK.YAML.ERROR_SOURCE, yamlMessage.getErrorSourceV());
        yaml.setProperty(Paths.NETWORK.YAML.ERROR_CODE, yamlMessage.getErrorCode());
        if (yamlMessage.getErrorName() != null && !yamlMessage.getErrorName().isBlank()) yaml.setProperty(Paths.NETWORK.YAML.ERROR_NAME, yamlMessage.getErrorName());
        yaml.setProperty(Paths.NETWORK.YAML.ERROR_SEVERITY, yamlMessage.getErrorSeverityV());

        return yaml;
    }
    protected static YAMLConfiguration serializeRequestYAML(YAMLMessage yamlMessage) {
        YAMLConfiguration yaml = new YAMLConfiguration();

        if (yamlMessage.getPacketTypeV() != null) yaml.setProperty(Paths.NETWORK.YAML.PACKET_TYPE, yamlMessage.getPacketTypeV());
        if (yamlMessage.getRequestTypeV() != null) yaml.setProperty(Paths.NETWORK.YAML.REQUEST_TYPE, yamlMessage.getRequestTypeV());
        if (yamlMessage.getRequestFile() != null && !yamlMessage.getRequestFile().isBlank()) yaml.setProperty(Paths.NETWORK.YAML.REQUEST_FILE, yamlMessage.getRequestFile());
        if (yamlMessage.getObjectPath() != null && !yamlMessage.getObjectPath().isBlank()) yaml.setProperty(Paths.NETWORK.YAML.OBJECT_PATH, yamlMessage.getObjectPath());
        if (yamlMessage.getObjectNewValue() != null && !yamlMessage.getObjectNewValue().isBlank()) yaml.setProperty(Paths.NETWORK.YAML.OBJECT_NEW_VALUE, yamlMessage.getObjectNewValue());
        return yaml;
    }

    public static class YAMLException extends Exception {
        public YAMLException(String message) {
            super(message);
        }
    }
    public static class InvalidPacketTypeException extends YAMLException {
        public InvalidPacketTypeException(String message) {
            super(message);
        }
    }
    public static class InvalidReplyTypeException extends YAMLException {
        public InvalidReplyTypeException(String message) {
            super(message);
        }
    }
    public static class InvalidRequestTypeException extends YAMLException {
        public InvalidRequestTypeException(String message) {
            super(message);
        }
    }
    public static class InvalidErrorSourceException extends YAMLException {
        public InvalidErrorSourceException(String message) {
            super(message);
        }
    }
    public static class InvalidErrorCodeException extends YAMLException {
        public InvalidErrorCodeException(String message) {
            super(message);
        }
    }
    public static class InvalidErrorSeverityException extends YAMLException {
        public InvalidErrorSeverityException(String message) {
            super(message);
        }
    }
    public static class InvalidRequestFileException extends YAMLException {
        public InvalidRequestFileException(String message) {
            super(message);
        }
    }
    public static class InvalidObjectPathException extends YAMLException {
        public InvalidObjectPathException(String message) {
            super(message);
        }
    }
    public static class InvalidObjectNewValueException extends YAMLException {
        public InvalidObjectNewValueException(String message) {
            super(message);
        }
    }
    public static class InvalidFileStateException extends YAMLException {
        public InvalidFileStateException(String message) {
            super(message);
        }
    }
    public static class InvalidFileSelectedException extends YAMLException {
        public InvalidFileSelectedException(String message) {
            super(message);
        }
    }
    public static class InvalidCurrentDrawException extends YAMLException {
        public InvalidCurrentDrawException(String message) {
            super(message);
        }
    }
    public static class InvalidVoltageException extends YAMLException {
        public InvalidVoltageException(String message) {
            super(message);
        }
    }
    public static class InvalidMenuReplySyntaxException extends YAMLException {
        public InvalidMenuReplySyntaxException(String message) {
            super(message);
        }
    }
    public static class TODOException extends YAMLException {
        public TODOException(String message) {
            super(message);
        }
    }

    public static YAMLMessage deserializeYAML(YAMLConfiguration yaml, UUID uuid) throws YAMLException {
        return deserializeYAML(yaml).setUUID(uuid);
    }

    public static YAMLMessage deserializeYAML(YAMLConfiguration yaml) throws YAMLException {

        String s = yaml.getString(Paths.NETWORK.YAML.PACKET_TYPE);
        YAMLMessage.PACKET_TYPE pT;
        try {
            pT = YAMLMessage.PACKET_TYPE.valueOf(s);
        } catch (IllegalArgumentException e) {
            throw new InvalidPacketTypeException("Invalid packet type: '" + s + "'");
        }

        YAMLMessage yamlMessage = new YAMLMessage().setPacketType(pT);

        try {
            String networkId = yaml.getString(Paths.NETWORK.YAML.INTERNAL_NETWORK_EVENT_ID);
            if (networkId != null && networkId.isEmpty()) {
                yamlMessage.setUUID(UUID.fromString(networkId));
            }
        } catch (IllegalArgumentException | NoSuchElementException e) {
            LCCP.logger.warn("Packet didn't contain a network id or it was invalid! Replay can't be associated with corresponding request event by id!");
        }

        try {
            switch (pT) {
                case request -> deserializeRequestYAML(yaml, yamlMessage);
                case reply -> deserializeReplyYAML(yaml, yamlMessage);
                case error -> deserializeErrorYAML(yaml, yamlMessage);
            }
        } catch (NoSuchElementException e) {
            throw new YAMLException("Couldn't disassemble YAML! Invalid or missing values / keys!");
        }

        return yamlMessage;
    }

    private static void deserializeRequestYAML(YAMLConfiguration yaml, YAMLMessage yamlMessage) throws YAMLException {
        String s0 = yaml.getString(Paths.NETWORK.YAML.REQUEST_TYPE);
        YAMLMessage.REQUEST_TYPE rT;
        try {
            rT = YAMLMessage.REQUEST_TYPE.valueOf(s0);
            yamlMessage.setRequestType(rT);
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestTypeException("Invalid request type: '" + s0 + "'");
        }

        switch (rT) {
            case play, pause, stop, menu, menu_change, file_upload -> {
                String requestFile = yaml.getString(Paths.NETWORK.YAML.REQUEST_FILE);
                if (requestFile == null || requestFile.isBlank()) throw new InvalidRequestFileException("Invalid request file name: '" + requestFile + "'. Must be a valid, non empty String!");
                yamlMessage.setRequestFile(requestFile);
                if (rT == YAMLMessage.REQUEST_TYPE.menu_change) {
                    String objectPath = yaml.getString(Paths.NETWORK.YAML.OBJECT_PATH);
                    String objectNewValue = yaml.getString(Paths.NETWORK.YAML.OBJECT_NEW_VALUE);
                    if (objectPath == null || objectPath.isBlank()) throw new InvalidObjectPathException("Invalid object path: '" + objectPath + "'. Must be a valid, non empty String!");
                    yamlMessage.setObjectPath(objectPath);
                    if (objectNewValue == null || objectNewValue.isBlank()) throw new InvalidObjectNewValueException("Invalid object new value: '" + objectNewValue + "'. Must be a valid, non empty String!");
                    yamlMessage.setObjectNewValue(objectNewValue);
                }
            }
        }
    }

    private static void deserializeReplyYAML(YAMLConfiguration yaml, YAMLMessage yamlMessage) throws YAMLException {
        String s0 = yaml.getString(Paths.NETWORK.YAML.REPLY_TYPE);
        YAMLMessage.REPLY_TYPE rT;
        try {
            rT = YAMLMessage.REPLY_TYPE.valueOf(s0);
            yamlMessage.setReplyType(rT);
        } catch (IllegalArgumentException e) {
            throw new InvalidReplyTypeException("Invalid reply type: '" + s0 + "'");
        }

        switch (rT) {
            case status -> deserializeStatusReplyYAML(yaml, yamlMessage);
            case menu -> deserializeMenuReplyYAML(yaml, yamlMessage);
        }
    }

    private static void deserializeStatusReplyYAML(YAMLConfiguration yaml, YAMLMessage yamlMessage) throws YAMLException {
        boolean fileIsLoaded = yaml.getBoolean(Paths.NETWORK.YAML.FILE_IS_LOADED);
        yamlMessage.setFileLoaded(fileIsLoaded);

        String s1 = yaml.getString(Paths.NETWORK.YAML.FILE_STATE);
        String s2 = yaml.getString(Paths.NETWORK.YAML.FILE_SELECTED);
        if (!s1.isBlank()) {
            YAMLMessage.FILE_STATE fS;
            try {
                fS = YAMLMessage.FILE_STATE.valueOf(s1);
                yamlMessage.setFileState(fS);
            } catch (IllegalArgumentException e) {
                throw new InvalidFileStateException("Invalid file state: '" + s1 + "'");
            }
            if (s2.isBlank()) throw new InvalidFileSelectedException("Selected file name mustn't be empty if file state is given!");
            else {
                yamlMessage.setFileSelected(s2);
            }
        }

        double currentDraw = yaml.getDouble(Paths.NETWORK.YAML.CURRENT_DRAW);
        double voltage = yaml.getDouble(Paths.NETWORK.YAML.VOLTAGE);
        boolean lidState = yaml.getBoolean(Paths.NETWORK.YAML.LID_STATE);

        if (currentDraw < 0) throw new InvalidCurrentDrawException("Invalid current draw value! Value mustn't be negative!");
        if (voltage < 0) throw new InvalidVoltageException("Invalid voltage value! Value mustn't be negative!");

        yamlMessage.setCurrentDraw(currentDraw);
        yamlMessage.setVoltage(voltage);
        yamlMessage.setLidState(lidState);

        HashMap<String, String> availableAnimations = new HashMap<>();

        Configuration availableAnimationsSection = yaml.subset(Paths.NETWORK.YAML.AVAILABLE_ANIMATIONS);

        for (Iterator<String> it = availableAnimationsSection.getKeys(); it.hasNext(); ) {
            String s = it.next();
            availableAnimations.put(s, availableAnimationsSection.getString(s));
        }
        yamlMessage.setAvailableAnimations(availableAnimations);
    }
    private static void deserializeMenuReplyYAML(YAMLConfiguration yaml, YAMLMessage yamlMessage) throws YAMLException {
        yaml.clearProperty(Paths.NETWORK.YAML.PACKET_TYPE);
        yaml.clearProperty(Paths.NETWORK.YAML.REPLY_TYPE);
        yamlMessage.setMenuYaml(yaml);

    }

    private static void deserializeErrorYAML(YAMLConfiguration yaml, YAMLMessage yamlMessage) throws YAMLException {
        String s0 = yaml.getString(Paths.NETWORK.YAML.ERROR_SOURCE);
        YAMLMessage.ERROR_SOURCE eS;
        try {
            eS = YAMLMessage.ERROR_SOURCE.valueOf(s0);
            yamlMessage.setErrorSource(eS);
        } catch (IllegalArgumentException e) {
            throw new InvalidErrorSourceException("Invalid error source: '" + s0 + "'");
        }

        int errorCode = yaml.getInt(Paths.NETWORK.YAML.ERROR_CODE);
        if (errorCode < 0) throw new InvalidErrorCodeException("Invalid error code: '" + errorCode + "'. Error code must be a valid, positive integer!");
        yamlMessage.setErrorCode(errorCode);

        yamlMessage.setErrorName(yaml.getString(Paths.NETWORK.YAML.ERROR_NAME));

        int s1 = -1;
        try {
            s1 = yaml.getInt(Paths.NETWORK.YAML.ERROR_SEVERITY);
            YAMLMessage.ERROR_SEVERITY eS0;
            eS0 = YAMLMessage.ERROR_SEVERITY.valueOf(s1);
            yamlMessage.setErrorSeverity(eS0);
        } catch (IllegalArgumentException e) {
            throw new InvalidErrorSeverityException("Invalid error severity: '" + s1 + "'");
        }
    }
}
