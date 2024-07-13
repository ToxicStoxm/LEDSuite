package com.toxicstoxm.lccp.yaml_factory;

import com.toxicstoxm.lccp.LCCP;
import com.toxicstoxm.lccp.Constants;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.YAMLConfiguration;

import java.util.*;

public class YAMLSerializer {
    public static YAMLConfiguration serializeYAML(YAMLMessage yamlMessage) throws InvalidPacketTypeException, InvalidReplyTypeException, TODOException {
        YAMLMessage.PACKET_TYPE packetType = null;
        YAMLConfiguration result = new YAMLConfiguration();
        if (yamlMessage.getNetworkID() == null) yamlMessage.setUUID(UUID.randomUUID());
        result.setProperty(Constants.Network.YAML.INTERNAL_NETWORK_EVENT_ID, String.valueOf(yamlMessage.getNetworkID()));
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
        YAMLConfiguration menuYaml = null;
        try {
            menuYaml = yamlMessage.getAnimationMenu().serialize();
        } catch (TODOException e) {
            LCCP.logger.fatal("Unsupported operation: Tried to serialize menu yaml request!");
            LCCP.logger.warn("This wont be implemented since the front-end never sends menus back to the back-end!");
            LCCP.logger.warn("If you want to implement it you can do so by contributing to the project on GitHub!");
        }
        if (menuYaml == null) return null;
        if (yamlMessage.getPacketTypeV() != null) menuYaml.setProperty(Constants.Network.YAML.PACKET_TYPE, yamlMessage.getPacketTypeV());
        if (yamlMessage.getReplyType() != null) menuYaml.setProperty(Constants.Network.YAML.REPLY_TYPE, yamlMessage.getReplyTypeV());
        return menuYaml;
    }

    protected static YAMLConfiguration serializeStatusReplyYAML(YAMLMessage yamlMessage) {
        YAMLConfiguration yaml = new YAMLConfiguration();

        if (yamlMessage.getPacketTypeV() != null) yaml.setProperty(Constants.Network.YAML.PACKET_TYPE, yamlMessage.getPacketTypeV());
        if (yamlMessage.getRequestTypeV() != null) yaml.setProperty(Constants.Network.YAML.REPLY_TYPE, yamlMessage.getRequestTypeV());
        yaml.setProperty(Constants.Network.YAML.FILE_IS_LOADED, yamlMessage.isFileLoaded());
        if (yamlMessage.getFileStateV() != null) yaml.setProperty(Constants.Network.YAML.FILE_STATE, yamlMessage.getFileStateV());
        if (yamlMessage.getFileSelected() != null && !yamlMessage.getFileSelected().isBlank()) yaml.setProperty(Constants.Network.YAML.FILE_SELECTED, yamlMessage.getFileSelected());
        yaml.setProperty(Constants.Network.YAML.CURRENT_DRAW, yamlMessage.getCurrentDraw());
        yaml.setProperty(Constants.Network.YAML.VOLTAGE, yamlMessage.getVoltage());
        if (yamlMessage.getFileStateV() != null) yaml.setProperty(Constants.Network.YAML.LID_STATE, yamlMessage.isLidState());
        if (yamlMessage.getAvailableAnimations() != null && !yamlMessage.getAvailableAnimations().isEmpty()) {
            for (Map.Entry<String, String> entry : yamlMessage.getAvailableAnimations().entrySet()) {
                yaml.setProperty(Constants.Network.YAML.AVAILABLE_ANIMATIONS + Constants.Config.SEPARATOR + entry.getKey(), entry.getValue());
            }
        }

        return yaml;
    }

    protected static YAMLConfiguration serializeErrorYAML(YAMLMessage yamlMessage) {
        YAMLConfiguration yaml = new YAMLConfiguration();

        if (yamlMessage.getPacketTypeV() != null) yaml.setProperty(Constants.Network.YAML.PACKET_TYPE, yamlMessage.getPacketTypeV());
        if (yamlMessage.getErrorSourceV() != null) yaml.setProperty(Constants.Network.YAML.ERROR_SOURCE, yamlMessage.getErrorSourceV());
        yaml.setProperty(Constants.Network.YAML.ERROR_CODE, yamlMessage.getErrorCode());
        if (yamlMessage.getErrorName() != null && !yamlMessage.getErrorName().isBlank()) yaml.setProperty(Constants.Network.YAML.ERROR_NAME, yamlMessage.getErrorName());
        yaml.setProperty(Constants.Network.YAML.ERROR_SEVERITY, yamlMessage.getErrorSeverityV());

        return yaml;
    }
    protected static YAMLConfiguration serializeRequestYAML(YAMLMessage yamlMessage) {
        YAMLConfiguration yaml = new YAMLConfiguration();

        if (yamlMessage.getPacketTypeV() != null) yaml.setProperty(Constants.Network.YAML.PACKET_TYPE, yamlMessage.getPacketTypeV());
        if (yamlMessage.getRequestTypeV() != null) yaml.setProperty(Constants.Network.YAML.REQUEST_TYPE, yamlMessage.getRequestTypeV());
        if (yamlMessage.getRequestFile() != null && !yamlMessage.getRequestFile().isBlank()) yaml.setProperty(Constants.Network.YAML.REQUEST_FILE, yamlMessage.getRequestFile());
        if (yamlMessage.getObjectPath() != null && !yamlMessage.getObjectPath().isBlank()) yaml.setProperty(Constants.Network.YAML.OBJECT_PATH, yamlMessage.getObjectPath());
        if (yamlMessage.getObjectNewValue() != null && !yamlMessage.getObjectNewValue().isBlank()) yaml.setProperty(Constants.Network.YAML.OBJECT_NEW_VALUE, yamlMessage.getObjectNewValue());
        if (yamlMessage.getRequestType().equals(YAMLMessage.REQUEST_TYPE.settings_change)) {
            HashMap<String, Object> additionalEntries = yamlMessage.getAdditionalEntries();
            if (additionalEntries != null && !additionalEntries.isEmpty()) {
                for (Map.Entry<String, Object> entry : additionalEntries.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (key != null && !key.isEmpty() && !key.isBlank() && value != null) {
                        yaml.setProperty(key, value);
                    }
                }
            }
        }
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

    public static YAMLMessage deserializeYAML(YAMLConfiguration yaml) throws YAMLException {
        return deserializeYAML(yaml, null);
    }

    public static YAMLMessage deserializeYAML(YAMLConfiguration yaml, UUID uuid) throws YAMLException {

        String s = yaml.getString(Constants.Network.YAML.PACKET_TYPE);
        YAMLMessage.PACKET_TYPE pT;
        try {
            pT = YAMLMessage.PACKET_TYPE.valueOf(s);
        } catch (IllegalArgumentException e) {
            throw new InvalidPacketTypeException("Invalid packet type: '" + s + "'");
        }

        YAMLMessage yamlMessage;
        if (uuid != null) yamlMessage = new YAMLMessage(uuid).setPacketType(pT);
        else yamlMessage = new YAMLMessage().setPacketType(pT);

        try {
            switch (pT) {
                case request -> deserializeRequestYAML(yaml, yamlMessage);
                case reply -> deserializeReplyYAML(yaml, yamlMessage);
                case error -> deserializeErrorYAML(yaml, yamlMessage);
            }
        } catch (NoSuchElementException e) {
            LCCP.logger.error(e);
            throw new YAMLException("Couldn't disassemble YAML! Invalid or missing values / keys!");
        }

        return yamlMessage;
    }

    private static void deserializeRequestYAML(YAMLConfiguration yaml, YAMLMessage yamlMessage) throws YAMLException {
        String replayType = yaml.getString(Constants.Network.YAML.REQUEST_TYPE);
        YAMLMessage.REQUEST_TYPE rT;
        try {
            rT = YAMLMessage.REQUEST_TYPE.valueOf(replayType);
            yamlMessage.setRequestType(rT);
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestTypeException("Invalid request type: '" + replayType + "'");
        }

        switch (rT) {
            case play, pause, stop, menu, menu_change, file_upload -> {
                String requestFile = yaml.getString(Constants.Network.YAML.REQUEST_FILE);
                if (requestFile == null || requestFile.isBlank()) throw new InvalidRequestFileException("Invalid request file name: '" + requestFile + "'. Must be a valid, non empty String!");
                yamlMessage.setRequestFile(requestFile);
                if (rT == YAMLMessage.REQUEST_TYPE.menu_change) {
                    String objectPath = yaml.getString(Constants.Network.YAML.OBJECT_PATH);
                    String objectNewValue = yaml.getString(Constants.Network.YAML.OBJECT_NEW_VALUE);
                    if (objectPath == null || objectPath.isBlank()) throw new InvalidObjectPathException("Invalid object path: '" + objectPath + "'. Must be a valid, non empty String!");
                    yamlMessage.setObjectPath(objectPath);
                    if (objectNewValue == null || objectNewValue.isBlank()) throw new InvalidObjectNewValueException("Invalid object new value: '" + objectNewValue + "'. Must be a valid, non empty String!");
                    yamlMessage.setObjectNewValue(objectNewValue);
                }
            }
            case settings_change -> {
                yaml.clearProperty(Constants.Network.YAML.PACKET_TYPE);
                yaml.clearProperty(Constants.Network.YAML.REQUEST_TYPE);
                yaml.clearProperty(Constants.Network.YAML.INTERNAL_NETWORK_EVENT_ID);
                for (Iterator<String> it = yaml.getKeys(); it.hasNext(); ) {
                    String key = it.next();
                    yamlMessage.addAdditionalEntry(key, yaml.getProperty(key));
                }
                yaml.setProperty(Constants.Network.YAML.INTERNAL_NETWORK_EVENT_ID, yamlMessage.getNetworkID().toString());
                yaml.setProperty(Constants.Network.YAML.PACKET_TYPE, yamlMessage.getPacketTypeV());
                yaml.setProperty(Constants.Network.YAML.REQUEST_TYPE, yamlMessage.getRequestTypeV());
            }
        }
    }

    private static void deserializeReplyYAML(YAMLConfiguration yaml, YAMLMessage yamlMessage) throws YAMLException {
        String replayType = yaml.getString(Constants.Network.YAML.REPLY_TYPE);

        YAMLMessage.REPLY_TYPE rT;
        try {
            rT = YAMLMessage.REPLY_TYPE.valueOf(replayType);
            yamlMessage.setReplyType(rT);
        } catch (IllegalArgumentException e) {
            throw new InvalidReplyTypeException("Invalid reply type: '" + replayType + "'");
        }

        switch (rT) {
            case status -> deserializeStatusReplyYAML(yaml, yamlMessage);
            case menu -> deserializeMenuReplyYAML(yaml, yamlMessage);
        }
    }

    private static void deserializeStatusReplyYAML(YAMLConfiguration yaml, YAMLMessage yamlMessage) throws YAMLException {
        boolean fileIsLoaded = yaml.getBoolean(Constants.Network.YAML.FILE_IS_LOADED);
        yamlMessage.setFileLoaded(fileIsLoaded);

        String fileState = yaml.getString(Constants.Network.YAML.FILE_STATE);
        String fileSelected = yaml.getString(Constants.Network.YAML.FILE_SELECTED);
        if (fileIsLoaded && !fileState.isBlank()) {
            YAMLMessage.FILE_STATE fS;
            try {
                fS = YAMLMessage.FILE_STATE.valueOf(fileState);
                yamlMessage.setFileState(fS);
            } catch (IllegalArgumentException e) {
                throw new InvalidFileStateException("Invalid file state: '" + fileState + "'");
            }
            if (fileSelected.isBlank()) throw new InvalidFileSelectedException("Selected file name mustn't be empty if file state is given!");
            else {
                yamlMessage.setFileSelected(fileSelected);
            }
        }

        double currentDraw = yaml.getDouble(Constants.Network.YAML.CURRENT_DRAW);
        double voltage = yaml.getDouble(Constants.Network.YAML.VOLTAGE);
        boolean lidState = yaml.getBoolean(Constants.Network.YAML.LID_STATE);

        if (currentDraw < 0) throw new InvalidCurrentDrawException("Invalid current draw value! Value mustn't be negative!");
        if (voltage < 0) throw new InvalidVoltageException("Invalid voltage value! Value mustn't be negative!");

        yamlMessage.setCurrentDraw(currentDraw);
        yamlMessage.setVoltage(voltage);
        yamlMessage.setLidState(lidState);

        HashMap<String, String> availableAnimations = new HashMap<>();

        Configuration availableAnimationsSection = yaml.subset(Constants.Network.YAML.AVAILABLE_ANIMATIONS);

        String id = "[" + yamlMessage.getNetworkID() + "] ";
        LCCP.logger.debug(id + "Available animations update:");

        for (Iterator<String> it = availableAnimationsSection.getKeys(); it.hasNext(); ) {
            String s = it.next();
            LCCP.logger.debug(id + s + ": " + availableAnimationsSection.getString(s));
            availableAnimations.put(s, availableAnimationsSection.getString(s));
        }
        yamlMessage.setAvailableAnimations(availableAnimations);
        LCCP.logger.debug(id + "Available animations updated!");
    }
    private static void deserializeMenuReplyYAML(YAMLConfiguration yaml, YAMLMessage yamlMessage) {

        UUID networkID = yamlMessage.getNetworkID();
        String id = "[" + networkID + "] ";

        LCCP.logger.debug(id + "Received deserialize request!");

        /*for (Iterator<String> it = yaml.getKeys(); it.hasNext(); ) {
            String s = it.next();
            LCCP.logger.debug(s + ": " + yaml.getProperty(s));
        }*/

        LCCP.logger.debug(id + "Removed packet type and reply type!");

        yaml.clearProperty(Constants.Network.YAML.PACKET_TYPE);
        yaml.clearProperty(Constants.Network.YAML.REPLY_TYPE);

        /*for (Iterator<String> it = yaml.getKeys(); it.hasNext(); ) {
            String s = it.next();
            LCCP.logger.debug(s + ": " + yaml.getProperty(s));
        }*/

        LCCP.logger.debug(id + "Initializing new animation menu and loading context from yaml!");

        yamlMessage.setAnimationMenu(AnimationMenu.fromYAML(yaml));
        LCCP.logger.debug(id + "Finished deserialization!");
    }

    private static void deserializeErrorYAML(YAMLConfiguration yaml, YAMLMessage yamlMessage) throws YAMLException {
        String s0 = yaml.getString(Constants.Network.YAML.ERROR_SOURCE);
        YAMLMessage.ERROR_SOURCE eS;
        try {
            eS = YAMLMessage.ERROR_SOURCE.valueOf(s0);
            yamlMessage.setErrorSource(eS);
        } catch (IllegalArgumentException e) {
            throw new InvalidErrorSourceException("Invalid error source: '" + s0 + "'");
        }

        int errorCode = yaml.getInt(Constants.Network.YAML.ERROR_CODE);
        if (errorCode < 0) throw new InvalidErrorCodeException("Invalid error code: '" + errorCode + "'. Error code must be a valid, positive integer!");
        yamlMessage.setErrorCode(errorCode);

        yamlMessage.setErrorName(yaml.getString(Constants.Network.YAML.ERROR_NAME));

        int s1 = -1;
        try {
            s1 = yaml.getInt(Constants.Network.YAML.ERROR_SEVERITY);
            YAMLMessage.ERROR_SEVERITY eS0;
            eS0 = YAMLMessage.ERROR_SEVERITY.valueOf(s1);
            yamlMessage.setErrorSeverity(eS0);
        } catch (IllegalArgumentException e) {
            throw new InvalidErrorSeverityException("Invalid error severity: '" + s1 + "'");
        }
    }
}
