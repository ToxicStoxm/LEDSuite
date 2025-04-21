package com.toxicstoxm.LEDSuite.communication.packet_management;

import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.ErrorCode;
import lombok.Getter;

/**
 * <strong>Meaning:</strong><br>
 * {@code DeserializationException} is an unchecked exception used to indicate errors that occur during the deserialization
 * of communication packets.
 * This exception provides additional context in the form of an {@link ErrorCode} to help with
 * debugging deserialization failures.
 * <p>
 * <strong>Purpose:</strong><br>
 * This exception is thrown when the system encounters an issue while trying to convert a serialized packet (typically in YAML format)
 * back into an object.
 * It can be used to indicate various types of issues such as invalid YAML structure, missing required fields, or
 * other deserialization-related problems.
 * <p>
 * <strong>Note:</strong><br>
 * This exception is a runtime (unchecked) exception,
 * which means it doesn't need to be explicitly declared or caught by calling methods.
 *
 * @since 1.0.0
 */
@Getter
public class DeserializationException extends RuntimeException {

    /**
     * The error code associated with this exception, indicating the type of deserialization failure.
     */
    private ErrorCode errorCode = null;

    /**
     * Default constructor for the exception.
     */
    public DeserializationException() {
        super();
    }

    /**
     * Constructor to create the exception with a specific error code.
     *
     * @param errorCode the error code to associate with the exception
     */
    public DeserializationException(ErrorCode errorCode) {
        super();
        this.errorCode = errorCode;
    }

    /**
     * Constructor to create the exception with a specific message.
     *
     * @param message the detailed error message
     */
    public DeserializationException(String message) {
        super(message);
    }

    /**
     * Constructor to create the exception with a specific message and error code.
     *
     * @param message the detailed error message
     * @param errorCode the error code to associate with the exception
     */
    public DeserializationException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Constructor to create the exception with a specific message and cause.
     *
     * @param message the detailed error message
     * @param cause the cause of the exception
     */
    public DeserializationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor to create the exception with a specific message, cause, and error code.
     *
     * @param message the detailed error message
     * @param cause the cause of the exception
     * @param errorCode the error code to associate with the exception
     */
    public DeserializationException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Constructor to create the exception with a specific cause.
     *
     * @param cause the cause of the exception
     */
    public DeserializationException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor to create the exception with a specific cause and error code.
     *
     * @param cause the cause of the exception
     * @param errorCode the error code to associate with the exception
     */
    public DeserializationException(Throwable cause, ErrorCode errorCode) {
        super(cause);
        this.errorCode = errorCode;
    }

    /**
     * Protected constructor allowing additional control over suppression and stack trace.
     *
     * @param message the detailed error message
     * @param cause the cause of the exception
     * @param enableSuppression whether suppression is enabled or not
     * @param writableStackTrace whether the stack trace is writable
     */
    protected DeserializationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Protected constructor allowing additional control over suppression, stack trace, and error code.
     *
     * @param message the detailed error message
     * @param cause the cause of the exception
     * @param enableSuppression whether suppression is enabled or not
     * @param writableStackTrace whether the stack trace is writable
     * @param errorCode the error code to associate with the exception
     */
    protected DeserializationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, ErrorCode errorCode) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.errorCode = errorCode;
    }
}
