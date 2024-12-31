package com.toxicstoxm.LEDSuite.tools;

import com.toxicstoxm.YAJSI.api.logging.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * A utility class providing methods for handling and logging exceptions.
 * <p>
 * This class includes a method for printing the stack trace and error message
 * of an exception to a provided logger. Null checks are performed on the
 * exception's message and stack trace.
 * </p>
 *
 * @since 1.0.0
 */
public class ExceptionTools {

    /**
     * Prints the stack trace and the message of the provided exception to the
     * specified logger. If the exception's message is null, a default message
     * indicating the absence of a message is logged. If the stack trace is null,
     * a warning is logged instead.
     * <p>
     * This method iterates over each element in the stack trace and logs it using
     * the provided logger.
     * </p>
     *
     * @param throwable the exception whose message and stack trace will be logged
     * @param logger the logger interface used for logging the exception details
     * @throws NullPointerException if either the exception or logger is null
     */
    public static void printStackTrace(@NotNull Throwable throwable, @NotNull Logger logger) {
        // Log the exception message, with a null check
        String message = throwable.getMessage();
        logger.log("Error message: " + (message == null ? "No message was provided" : message));

        // Log the stack trace elements, with a null check for stack trace
        StackTraceElement[] stackTraceElements = throwable.getStackTrace();
        if (stackTraceElements != null && stackTraceElements.length > 0) {
            for (StackTraceElement e : stackTraceElements) {
                logger.log(e.toString());
            }
        } else {
            logger.log("Stacktrace is empty or null!");
        }
    }
}
