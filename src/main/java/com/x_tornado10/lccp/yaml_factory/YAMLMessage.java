package com.x_tornado10.lccp.yaml_factory;

import lombok.Getter;

@Getter
public class YAMLMessage implements YAMLFactoryMessage {

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

    public String getPacketType() {
        return packetType.value;
    }

    public String getErrorSource() {
        return errorSource.value;
    }

    public int getErrorSeverity() {
        return errorSeverity.value;
    }

    public String getRequestType() {
        return requestType.value;
    }

    public String getReplyType() {
        return replyType.value;
    }

    public String getFileState() {
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
}
