package com.x_tornado10.lccp.yaml_factory;

import lombok.Getter;
import org.apache.commons.configuration2.YAMLConfiguration;

import java.util.HashMap;
import java.util.UUID;

@Getter
public class YAMLMessage implements YAMLFactoryMessage {

    private UUID uuid;

    private PACKET_TYPE packetType = PACKET_TYPE.request;
    private ERROR_SOURCE errorSource = ERROR_SOURCE.other;
    private int errorCode = 0;
    private String errorName = "";
    private ERROR_SEVERITY errorSeverity = ERROR_SEVERITY.ONE;
    private REQUEST_TYPE requestType = REQUEST_TYPE.status;
    private String requestFile = "";
    private String objectPath = "";
    private String objectNewValue = "";
    private REPLY_TYPE replyType = REPLY_TYPE.status;
    private boolean isFileLoaded = false;
    private FILE_STATE fileState = FILE_STATE.paused;
    private String fileSelected = "";
    private double currentDraw = 0;
    private double voltage = 0;
    private boolean lidState = false;
    private HashMap<String, String> availableAnimations = new HashMap<>();
    private YAMLConfiguration menuYaml = new YAMLConfiguration();

    public YAMLMessage(UUID uuid) {
        this.uuid = uuid;
    }
    public YAMLMessage() {
        this.uuid = UUID.randomUUID();
    }
    public static YAMLMessage defaultStatusRequest() {
        return new YAMLMessage().setPacketType(PACKET_TYPE.request).setRequestType(REQUEST_TYPE.status);
    }

    public static YAMLMessage builder() {
        return new YAMLMessage();
    }

    public static YAMLMessage errorMsg(
            ERROR_SOURCE source,
            int errorCode,
            String name,
            ERROR_SEVERITY severity
    ) {
        return builder()
                .setPacketType(PACKET_TYPE.error)
                .setErrorSource(source)
                .setErrorCode(errorCode)
                .setErrorName(name)
                .setErrorSeverity(severity);
    }
    public static YAMLMessage requestMsg(
            REQUEST_TYPE type,
            String file,
            String path,
            String newVal
    ) {
        return builder()
                .setPacketType(PACKET_TYPE.request)
                .setRequestType(type)
                .setRequestFile(file)
                .setObjectPath(path)
                .setObjectNewValue(newVal);
    }

    public static YAMLMessage replyStatusMsg(
            boolean fileLoaded,
            FILE_STATE state,
            String fileSelected,
            double currentDraw,
            double voltage,
            boolean lidState,
            HashMap<String, String> availableAnimations
    ) {
        return builder()
                .setPacketType(PACKET_TYPE.reply)
                .setReplyType(REPLY_TYPE.status)
                .setFileLoaded(fileLoaded)
                .setFileState(state)
                .setFileSelected(fileSelected)
                .setCurrentDraw(currentDraw)
                .setVoltage(voltage)
                .setLidState(lidState)
                .setAvailableAnimations(availableAnimations);
    }

    public static YAMLMessage replyMenuMsg(
            YAMLConfiguration menuYaml
    ) {
        return builder()
                .setPacketType(PACKET_TYPE.reply)
                .setReplyType(REPLY_TYPE.menu)
                .setMenuYaml(menuYaml);
    }

    protected YAMLMessage setUUID(UUID uuid) {
        this.uuid = uuid;
        return this;
    }
    public UUID getNetworkEventID() {
        return uuid;
    }

    public enum PACKET_TYPE {
        error("error"),
        request("request"),
        reply("reply");

        private final String value;

        PACKET_TYPE(String value) {
            this.value = value;
        }
    }

    public enum ERROR_SOURCE {
        power("power"),
        invalid_file("invalid_file"),
        invalid_request("invalid_request"),
        other("other");

        private final String value;

        ERROR_SOURCE(String value) {
            this.value = value;
        }
    }

    @Getter
    public enum ERROR_SEVERITY {
        ONE(1),
        TWO(2),
        THREE(3),
        FOUR(4);

        private final int value;

        ERROR_SEVERITY(int value) {
            this.value = value;
        }

        public static ERROR_SEVERITY valueOf(int value) {
            for (ERROR_SEVERITY severity : values()) {
                if (severity.value == value) {
                    return severity;
                }
            }
            throw new IllegalArgumentException("No enum constant with value " + value);
        }
    }

    public enum REQUEST_TYPE {
        status("status"),
        play("play"),
        pause(""),
        stop("stop"),
        menu("menu"),
        menu_change("menu_change");

        private final String value;

        REQUEST_TYPE(String value) {
            this.value = value;
        }
    }

    public enum REPLY_TYPE {
        status("status"),
        menu("menu");

        private final String value;

        REPLY_TYPE(String value) {
            this.value = value;
        }
    }

    public enum FILE_STATE {
        playing("playing"),
        paused("paused");

        private final String value;

        FILE_STATE(String value) {
            this.value = value;
        }
    }

    public String getPacketTypeV() {
        return packetType.value;
    }

    public String getErrorSourceV() {
        return errorSource.value;
    }

    public int getErrorSeverityV() {
        return errorSeverity.value;
    }

    public String getRequestTypeV() {
        return requestType.value;
    }

    public String getReplyTypeV() {
        return replyType.value;
    }

    public String getFileStateV() {
        return fileState.value;
    }

    public YAMLMessage setPacketType(PACKET_TYPE packetType) {
        this.packetType = packetType;
        return this;
    }

    public YAMLMessage setErrorSource(ERROR_SOURCE errorSource) {
        this.errorSource = errorSource;
        return this;
    }

    public YAMLMessage setErrorCode(int errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public YAMLMessage setErrorName(String errorName) {
        this.errorName = errorName;
        return this;
    }

    public YAMLMessage setErrorSeverity(ERROR_SEVERITY errorSeverity) {
        this.errorSeverity = errorSeverity;
        return this;
    }

    public YAMLMessage setRequestType(REQUEST_TYPE requestType) {
        this.requestType = requestType;
        return this;
    }

    public YAMLMessage setRequestFile(String requestFile) {
        this.requestFile = requestFile;
        return this;
    }

    public YAMLMessage setObjectPath(String objectPath) {
        this.objectPath = objectPath;
        return this;
    }

    public YAMLMessage setObjectNewValue(String objectNewValue) {
        this.objectNewValue = objectNewValue;
        return this;
    }

    public YAMLMessage setReplyType(REPLY_TYPE replyType) {
        this.replyType = replyType;
        return this;
    }

    public YAMLMessage setFileLoaded(boolean isFileLoaded) {
        this.isFileLoaded = isFileLoaded;
        return this;
    }

    public YAMLMessage setFileState(FILE_STATE fileState) {
        this.fileState = fileState;
        return this;
    }
    public YAMLMessage setFileSelected(String fileSelected) {
        this.fileSelected = fileSelected;
        return this;
    }
    public YAMLMessage setCurrentDraw(double currentDraw) {
        this.currentDraw = currentDraw;
        return this;
    }
    public YAMLMessage setVoltage(double voltage) {
        this.voltage = voltage;
        return this;
    }
    public YAMLMessage setLidState(boolean lidState) {
        this.lidState = lidState;
        return this;
    }
    public YAMLMessage setAvailableAnimations(HashMap<String, String> availableAnimations) {
        this.availableAnimations = availableAnimations;
        return this;
    }
    public YAMLMessage setMenuYaml(YAMLConfiguration menuYaml) {
        this.menuYaml = menuYaml;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("YAMLMessage {");

        // Include fields based on packet type
        switch (packetType) {
            case error -> {
                sb.append("packetType=").append(packetType.value).append(", ");
                sb.append("errorSource=").append(errorSource.value).append(", ");
                if (errorCode != 0) sb.append("errorCode=").append(errorCode).append(", ");
                if (errorName != null && !errorName.isBlank()) sb.append("errorName='").append(errorName).append("', ");
                sb.append("errorSeverity=").append(errorSeverity.value).append(", ");
            }

            case request -> {
                sb.append("packetType=").append(packetType.value).append(", ");
                sb.append("requestType=").append(requestType.value).append(", ");
                switch (requestType) {
                    case play, pause, stop, menu, menu_change -> {
                        sb.append("requestFile='").append(requestFile).append("', ");
                        if (requestType == REQUEST_TYPE.menu_change) {
                            sb.append("objectPath='").append(objectPath).append("', ");
                            sb.append("objectNewValue='").append(objectNewValue).append("', ");
                        }
                    }
                }
            }

            case reply -> {
                sb.append("packetType=").append(packetType.value).append(", ");
                sb.append("replyType=").append(replyType.value).append(", ");
                sb.append("isFileLoaded=").append(isFileLoaded).append(", ");
                sb.append("fileState=").append(fileState.value).append(", ");
                if (fileSelected != null && !fileSelected.isBlank())
                    sb.append("fileSelected='").append(fileSelected).append("', ");
                if (currentDraw != 0) sb.append("currentDraw=").append(currentDraw).append(", ");
                if (voltage != 0) sb.append("voltage=").append(voltage).append(", ");
                sb.append("lidState=").append(lidState).append(", ");
                sb.append("availableAnimations=").append(availableAnimations).append(", ");
            }
        }

        // Remove trailing comma and space
        if (sb.length() > 12) sb.setLength(sb.length() - 2);

        sb.append('}');
        return sb.toString();
    }
}
