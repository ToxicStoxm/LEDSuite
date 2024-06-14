package com.x_tornado10.lccp.yaml_factory;

import com.x_tornado10.lccp.util.Paths;
import org.apache.commons.configuration2.YAMLConfiguration;

public class YAMLAssembly {
    public static YAMLConfiguration assembleYAML(YAMLMessage yamlMessage) throws InvalidPacketTypeException, InvalidReplyTypeException, TODOException {
        YAMLMessage.PACKET_TYPE packetType = null;
        try {
            packetType = YAMLMessage.PACKET_TYPE.valueOf(yamlMessage.getPacketType());
        } catch (IllegalArgumentException e) {
            throw new InvalidPacketTypeException("Invalid packet type: " + packetType);
        }
        switch (packetType) {
            case reply -> {
                return assembleReplyYAML(yamlMessage);
            }
            case error -> {
                return assembleErrorYAML(yamlMessage);
            }
            case request -> {
                return assembleRequestYAML(yamlMessage);
            }
            case null, default -> throw new InvalidPacketTypeException("Invalid packet type: " + packetType);
        }
    }

    protected static YAMLConfiguration assembleReplyYAML(YAMLMessage yamlMessage) throws InvalidReplyTypeException, TODOException {
        YAMLMessage.REPLY_TYPE replyType = null;
        try {
            replyType = YAMLMessage.REPLY_TYPE.valueOf(yamlMessage.getReplyType());
        } catch (IllegalArgumentException e) {
            throw new InvalidReplyTypeException("Invalid reply type: " + replyType);
        }
        switch (replyType) {
            case menu -> {
                return assembleMenuReplyYAML(yamlMessage);
            }
            case status -> {
                return assembleStatusReplyYAML(yamlMessage);
            }
            case null, default -> throw new InvalidReplyTypeException("Invalid reply type: " + replyType);
        }
    }
    protected static YAMLConfiguration assembleMenuReplyYAML(YAMLMessage yamlMessage) throws TODOException {
        throw new TODOException("Implement it!");
    }

    protected static YAMLConfiguration assembleStatusReplyYAML(YAMLMessage yamlMessage) {
        YAMLConfiguration yaml = new YAMLConfiguration();

        yaml.setProperty(Paths.NETWORK.YAML.PACKET_TYPE, yamlMessage.getPacketType());
        yaml.setProperty(Paths.NETWORK.YAML.REPLY_TYPE, yamlMessage.getRequestType());
        yaml.setProperty(Paths.NETWORK.YAML.FILE_IS_LOADED, yamlMessage.isFileLoaded());
        yaml.setProperty(Paths.NETWORK.YAML.FILE_STATE, yamlMessage.getFileState());
        yaml.setProperty(Paths.NETWORK.YAML.FILE_SELECTED, yamlMessage.getFileSelected());
        yaml.setProperty(Paths.NETWORK.YAML.CURRENT_DRAW, yamlMessage.getCurrentDraw());
        yaml.setProperty(Paths.NETWORK.YAML.VOLTAGE, yamlMessage.getVoltage());
        yaml.setProperty(Paths.NETWORK.YAML.LID_STATE, yamlMessage.isLidState());

        return yaml;
    }

    protected static YAMLConfiguration assembleErrorYAML(YAMLMessage yamlMessage) {
        YAMLConfiguration yaml = new YAMLConfiguration();

        yaml.setProperty(Paths.NETWORK.YAML.PACKET_TYPE, yamlMessage.getPacketType());
        yaml.setProperty(Paths.NETWORK.YAML.ERROR_SOURCE, yamlMessage.getErrorSource());
        yaml.setProperty(Paths.NETWORK.YAML.ERROR_CODE, yamlMessage.getErrorCode());
        yaml.setProperty(Paths.NETWORK.YAML.ERROR_NAME, yamlMessage.getErrorName());
        yaml.setProperty(Paths.NETWORK.YAML.ERROR_SEVERITY, yamlMessage.getErrorSeverity());

        return yaml;
    }
    protected static YAMLConfiguration assembleRequestYAML(YAMLMessage yamlMessage) {
        YAMLConfiguration yaml = new YAMLConfiguration();

        yaml.setProperty(Paths.NETWORK.YAML.PACKET_TYPE, yamlMessage.getPacketType());
        yaml.setProperty(Paths.NETWORK.YAML.REQUEST_TYPE, yamlMessage.getRequestType());
        yaml.setProperty(Paths.NETWORK.YAML.REQUEST_FILE, yamlMessage.getRequestFile());
        yaml.setProperty(Paths.NETWORK.YAML.OBJECT_PATH, yamlMessage.getObjectPath());
        yaml.setProperty(Paths.NETWORK.YAML.OBJECT_NEW_VALUE, yamlMessage.getObjectNewValue());

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

    public static class TODOException extends YAMLException {
        public TODOException(String message) {
            super(message);
        }
    }

    public YAMLMessage disassembleYAML(YAMLConfiguration yaml) throws TODOException {
        throw new TODOException("");
    }
}
