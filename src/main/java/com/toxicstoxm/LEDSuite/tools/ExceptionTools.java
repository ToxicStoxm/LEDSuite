package com.toxicstoxm.LEDSuite.tools;

import com.toxicstoxm.YAJSI.api.logging.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Wrapper class for exception related methods.
 * Includes stack trace printer, etc.
 * @since 1.0.0
 */
public class ExceptionTools {
    /**
     * Prints the provided exceptions stacktrace and message using the provided logger interface.
     * Includes null checks for the stacktrace and error message.
     * @param exception the exception to print the stacktrace / message for
     * @param logger the logger interface to use for printing.
     */
    public static void printStackTrace(@NotNull Exception exception, @NotNull Logger logger) {
        String message = exception.getMessage();
        logger.log("Error message: " + (message == null ? "no message was found" : message));
        StackTraceElement[] stackTraceElements = exception.getStackTrace();
        if (stackTraceElements != null) {
            for (StackTraceElement e : stackTraceElements) {
                logger.log(e.toString());
            }
        } else logger.log("Stacktrace is null!");
    }
}
