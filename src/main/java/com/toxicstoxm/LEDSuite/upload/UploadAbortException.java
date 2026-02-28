package com.toxicstoxm.LEDSuite.upload;

import com.toxicstoxm.LEDSuite.time.Action;

/**
 * Custom exception thrown to indicate that an upload process has been aborted.
 * <p>
 * This exception can optionally hold a message or an {@link Action} that should
 * be executed when the exception is thrown, typically to print an error message
 * or take other actions related to the abort.
 * </p>
 *
 * @since 1.0.0
 */
public class UploadAbortException extends RuntimeException {
    /**
     * An optional action to print an error message or perform other operations when the exception is thrown.
     * If provided, this action will be executed when {@link #printErrorMessage()} is called.
     */
    private Action printMessage;

    /**
     * Constructs a new {@code UploadAbortException} with the specified {@link Action}.
     * The action will be executed when {@link #printErrorMessage()} is invoked.
     *
     * @param printMessage The action to execute when the exception is thrown, such as printing an error message.
     */
    public UploadAbortException(Action printMessage) {
        this.printMessage = printMessage;
    }

    /**
     * Executes the {@link Action} provided during the creation of this exception, if available.
     * This method can be used to perform additional tasks, such as printing an error message,
     * related to the upload abort.
     * <p>
     * If the {@link Action} is {@code null}, no operation is performed.
     * </p>
     */
    public void printErrorMessage() {
        if (printMessage != null) {
            printMessage.run();
        }
    }

    /**
     * Constructs a new {@code UploadAbortException} with the specified detail message.
     *
     * @param message The detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method).
     */
    public UploadAbortException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code UploadAbortException} with the specified detail message and cause.
     *
     * @param message The detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method).
     * @param cause The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     *              (A {@code null} value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public UploadAbortException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@code UploadAbortException} with the specified cause.
     *
     * @param cause The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     *              (A {@code null} value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public UploadAbortException(Throwable cause) {
        super(cause);
    }
}
