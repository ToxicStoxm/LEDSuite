package com.toxicstoxm.LEDSuite.communication.packet_management;

import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.ErrorCode;
import com.toxicstoxm.YAJSI.api.logging.Logger;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * {@code DeserializationException} is an unchecked exception and is used to express an error during deserialization of a communication packet.
 * @since 1.0.0
 */
@Getter
public class DeserializationException extends RuntimeException {

    private ErrorCode errorCode = null;

    public DeserializationException() {
        super();
    }

    public DeserializationException(ErrorCode errorCode) {
        super();
        this.errorCode = errorCode;
    }

    public DeserializationException(String message) {
        super(message);
    }

    public DeserializationException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public DeserializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeserializationException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public DeserializationException(Throwable cause) {
        super(cause);
    }

    public DeserializationException(Throwable cause, ErrorCode errorCode) {
        super(cause);
        this.errorCode = errorCode;
    }

    protected DeserializationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    protected DeserializationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, ErrorCode errorCode) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.errorCode = errorCode;
    }

    public void printStackTrace(@NotNull Logger s) {
        s.log("Error message: " + getMessage());
        s.log("Error code: " + getErrorCode().name() + "(" + getErrorCode().getCode() + ")");
        for (StackTraceElement e : getStackTrace()) {
            s.log(e.toString());
        }
    }
}
