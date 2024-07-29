package com.toxicstoxm.LEDSuite.logging;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.LEDSuite;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import lombok.Getter;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.gnome.adw.Toast;
import org.gnome.adw.ToastOverlay;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.fusesource.jansi.Ansi.Color.DEFAULT;
import static org.fusesource.jansi.Ansi.ansi;

/**
 * The `Logger` class is responsible for logging messages of various levels (INFO, WARN, ERROR, FATAL, DEBUG, VERBOSE)
 * to the console and optionally to a log file.
 * It supports color formatting for console output and provides
 * visual feedback through UI toasts.
 *
 * <p>This class uses ANSI escape codes for console color formatting and handles writing logs asynchronously.
 * It also integrates with a UI to display toast notifications for error and fatal messages.
 *
 * @since 1.0.0
 */
public class Logger {

    /**
     * A sorted map of log messages, used for caching before writing to the log file.
     *
     * @since 1.0.0
     */
    private final TreeMap<Long, String> cache;

    /**
     * Maximum length of the log message trace for alignment purposes.
     *
     * @since 1.0.0
     */
    private int maxLength;

    /**
     * Constructs a new `Logger` instance.
     * Initializes the ANSI console system and the cache for storing log messages.
     *
     * @since 1.0.0
     */
    public Logger() {
        // Activates ANSI support for console color codes
        AnsiConsole.systemInstall();
        // Initializes the cache to store log messages before writing them to the log file
        cache = new TreeMap<>();
        // Sets initial maximum length for log message trace
        maxLength = 0;
    }

    /**
     * Logs an informational message to the console and log file.
     *
     * @param message The message to log.
     * @since 1.0.0
     */
    public void info(String message) {
        // Check if INFO level logging is enabled
        if (log_level.INFO.isEnabled()) {
            // Format and log the message with INFO level
            cInfo("[INFO]:  [" + Constants.Application.NAME + "] " + message);
        }
    }

    /**
     * Internal method for logging an informational message with ANSI color formatting.
     *
     * @param message The formatted message to log.
     * @since 1.0.0
     */
    private void cInfo(String message) {
        // Write the message to the log file
        writeLog(message);
        // Log to console with default color
        log(ansi().fg(DEFAULT).a(message).reset());
    }

    /**
     * Logs a warning message to the console and log file.
     *
     * @param message The message to log.
     * @since 1.0.0
     */
    public void warn(String message) {
        // Check if WARN level logging is enabled
        if (log_level.WARN.isEnabled()) {
            // Format and log the message with WARN level
            cWarn("[WARN]:  [" + Constants.Application.NAME + "] " + message);
        }
    }

    /**
     * Internal method for logging a warning message with ANSI color formatting.
     *
     * @param message The formatted message to log.
     * @since 1.0.0
     */
    private void cWarn(String message) {
        // Write the message to the log file
        writeLog(message);
        // Log to console with warning color
        log(ansi().fgRgb(227, 163, 0).a(message).reset());
    }

    /**
     * Logs an error message to the console and log file.
     *
     * @param message The message to log.
     * @since 1.0.0
     */
    public void error(String message) {
        error(message, false); // Default to no visual feedback
    }

    /**
     * Logs an error message to the console and log file, with optional visual feedback.
     *
     * @param message The message to log.
     * @param visualFeedback Whether to display a visual toast notification.
     * @since 1.0.0
     */
    public void error(String message, boolean visualFeedback) {
        // Check if ERROR level logging is enabled
        if (log_level.ERROR.isEnabled()) {
            // Format and log the message with ERROR level
            cError("[ERROR]: [" + Constants.Application.NAME + "] " + message);
        }
        // Display visual feedback if requested
        if (visualFeedback) {
            visualFeedback(message, 0); // Default timeout of 0 seconds
        }
    }

    /**
     * Logs an error message and its associated exception to the console and log file.
     *
     * @param message The error message to log.
     * @param e The exception to log.
     * @since 1.0.0
     */
    public void error(String message, Exception e) {
        // Log the error message
        error(message);
        // Log the stack trace of the exception
        error(e);
    }

    /**
     * Logs the stack trace of an exception to the console and log file.
     *
     * @param exception The exception to log.
     * @since 1.0.0
     */
    public void error(Exception exception) {
        StackTraceElement[] stackTrace = exception.getStackTrace();
        Throwable t = exception.getCause();

        // Check if ERROR level logging is enabled and stack trace is not null
        if (log_level.ERROR.isEnabled() && stackTrace != null) {
            // Log the error message and stack trace
            debug("Error message: " + exception);
            debug("Stack trace:");
            for (StackTraceElement s : stackTrace) {
                debug(s.toString());
            }
            // Log the cause of the exception if present
            if (t != null) {
                debug("Cause:");
                debug(t.toString());
            }
        }
    }

    /**
     * Internal method for logging an error message with ANSI color formatting.
     *
     * @param message The formatted message to log.
     * @since 1.0.0
     */
    private void cError(String message) {
        // Write the message to the log file
        writeLog(message);
        // Log to console with error color
        log(ansi().fgRgb(255, 0, 0).a(message).reset());
    }

    /**
     * Logs a fatal error message to the console and log file.
     *
     * @param message The message to log.
     * @since 1.0.0
     */
    public void fatal(String message) {
        fatal(message, false); // Default to no visual feedback
    }

    /**
     * Logs a fatal error message to the console and log file, with optional visual feedback.
     *
     * @param message The message to log.
     * @param visualFeedback Whether to display a visual toast notification.
     * @since 1.0.0
     */
    public void fatal(String message, boolean visualFeedback) {
        // Check if FATAL level logging is enabled
        if (log_level.FATAL.isEnabled()) {
            // Format and log the message with FATAL level
            cFatal("[FATAL]: [" + Constants.Application.NAME + "] " + message);
        }
        // Display visual feedback if requested
        if (visualFeedback) {
            visualFeedback(message, 0); // Default timeout of 0 seconds
        }
    }

    /**
     * Internal method for logging a fatal error message with ANSI color formatting.
     *
     * @param message The formatted message to log.
     * @since 1.0.0
     */
    private void cFatal(String message) {
        // Write the message to the log file
        writeLog(message);
        // Log to console with fatal color
        log(ansi().fgRgb(180, 0, 0).a(message).reset());
    }

    /**
     * Logs a debug message to the console and log file.
     *
     * @param message The message to log.
     * @since 1.0.0
     */
    public void debug(String message) {
        // Check if DEBUG level logging is enabled
        if (log_level.DEBUG.isEnabled()) {
            // Format and log the message with DEBUG level
            cDebug("[DEBUG]: [" + Constants.Application.NAME + "] " + message);
        }
    }

    /**
     * Internal method for logging a debug message with ANSI color formatting.
     *
     * @param message The formatted message to log.
     * @since 1.0.0
     */
    private void cDebug(String message) {
        // Write the message to the log file
        writeLog(message);
        // Log to console with debug color
        log(ansi().fgRgb(7, 94, 217).a(message).reset());
    }

    /**
     * Logs a verbose message to the console and log file.
     *
     * @param message The message to log.
     * @since 1.0.0
     */
    public void verbose(String message) {
        // Check if VERBOSE level logging is enabled
        if (log_level.VERBOSE.isEnabled()) {
            // Format and log the message with VERBOSE level
            cVerbose("[VERBOSE]: [" + Constants.Application.NAME + "] " + message);
        }
    }

    /**
     * Internal method for logging a verbose message with ANSI color formatting.
     *
     * @param message The formatted message to log.
     * @since 1.0.0
     */
    private void cVerbose(String message) {
        // Write the message to the log file
        writeLog(message);
        // Log to console with verbose color
        log(ansi().fgRgb(160, 22, 244).a(message).reset());
    }

    /**
     * Attaches metadata (timestamp and stack trace) to the log message.
     *
     * @param message The message to attach metadata to.
     * @return The message with attached metadata.
     * @since 1.0.0
     */
    private String attachMetadata(String message) {
        // Create a date formatter for the timestamp
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        // Attach the current timestamp and stack trace to the message
        return "[" + df.format(new Date()) + "] " + getTrace() + message;
    }

    /**
     * Attaches metadata (timestamp and stack trace) to an ANSI formatted log message.
     *
     * @param message The ANSI formatted message to attach metadata to.
     * @return The ANSI message with attached metadata.
     * @since 1.0.0
     */
    private String attachMetadata(Ansi message) {
        // Create a date formatter for the timestamp
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        // Attach the current timestamp and stack trace to the ANSI message
        return "[" + df.format(new Date()) + "] " + ansi().fgRgb(0, 148, 50).a(getTrace()).reset() + message;
    }

    /**
     * Retrieves the current stack trace information (filename and row-number) for logging.
     *
     * @return The formatted stack trace information.
     * @since 1.0.0
     */
    private String getTrace() {
        // Retrieve the stack trace of the current thread
        String s = Thread.currentThread().getStackTrace()[6].toString();
        // Extract the relevant portion of the stack trace
        StringBuilder trace = new StringBuilder("[" + s.substring(s.indexOf("(") + 1, s.lastIndexOf(")")) + "] ");
        // Adjust the trace length to align with other log messages
        int length = trace.length();
        int difference = maxLength - trace.length();
        if (difference < 0) maxLength = length;
        trace.append(" ".repeat(Math.max(0, difference)));
        return trace.toString();
    }

    /**
     * Logs a message to the console with metadata attached.
     *
     * @param message The message to log.
     * @since 1.0.0
     */
    public void log(String message) {
        log(message, true); // Default to appending a newline after the message
    }

    /**
     * Logs a message to the console with metadata attached and optional newline.
     *
     * @param message The message to log.
     * @param newLine Whether to append a newline after the message.
     * @since 1.0.0
     */
    public void log(String message, boolean newLine) {
        // Attach metadata to the message
        String finalMessage = attachMetadata(message);
        // Output the message to the console
        if (newLine) System.out.println(finalMessage);
        else System.out.print(finalMessage);
    }

    /**
     * Logs an ANSI formatted message to the console with metadata attached.
     *
     * @param message The ANSI formatted message to log.
     * @since 1.0.0
     */
    private void log(Ansi message) {
        // Output the ANSI message to the console with metadata
        System.out.println(attachMetadata(message));
    }

    /**
     * Writes a log message to the log file asynchronously.
     * If the log file does not exist, it caches messages until the file is created.
     *
     * @param message The message to write to the log file.
     * @since 1.0.0
     */
    private void writeLog(String message) {
        // Attach metadata to the message for file writing
        String temp = attachMetadata(message);
        new LEDSuiteRunnable() {
            @Override
            public void run() {
                // Check if the log file exists
                if (!new File(Constants.File_System.logFile).exists()) {
                    // Cache the message if the file does not exist
                    if (cache.size() >= 200) cache.pollLastEntry();
                    cache.put(System.currentTimeMillis(), message);
                } else {
                    // Write cached messages if the log file exists and logging is enabled
                    if (LEDSuite.settings.isLogFileEnabled()) {
                        if (!cache.isEmpty()) {
                            TreeMap<Long, String> temp = new TreeMap<>(cache);
                            for (SortedMap.Entry<Long, String> entry : temp.entrySet()) {
                                writeLog(entry.getValue());
                            }
                        }
                    }
                }
                // Write the log message to the log file
                if (LEDSuite.settings.isLogFileEnabled()) {
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(Constants.File_System.logFile, true))) {
                        writer.write(temp);
                        writer.newLine();
                    } catch (IOException e) {
                        System.out.println("Error while trying to write log to log file!");
                        System.out.println("If this message is displayed repeatedly:");
                        System.out.println(Constants.Messages.WARN.OPEN_GITHUB_ISSUE);
                        throw new RuntimeException(e);
                    }
                }
            }
        }.runTaskAsynchronously(); // Run the file writing operation asynchronously
    }

    /**
     * Displays a toast notification with a message and a default timeout of 5 seconds.
     *
     * @param message The message to display in the toast.
     * @since 1.0.0
     */
    private void visualFeedback(String message) {
        visualFeedback(message, 5); // Default timeout of 5 seconds
    }

    /**
     * Displays a toast notification with a message and a specific timeout.
     *
     * @param message The message to display in the toast.
     * @param timeout The timeout duration for the toast in seconds.
     * @since 1.0.0
     */
    private void visualFeedback(String message, int timeout) {
        // Check if the main window and toast overlay are available
        if (LEDSuite.mainWindow == null) return;
        ToastOverlay toastOverlay = LEDSuite.mainWindow.toastOverlay;
        if (toastOverlay != null) {
            // Create and display new toast with the message and timeout
            toastOverlay
                    .addToast(
                            Toast.builder()
                                    .setTimeout(timeout)
                                    .setTitle(message)
                                    .build()
                    );
        }
    }

    /**
     * Enum representing different log levels.
     * Determines which log messages are enabled based on the current log level setting.
     *
     * @since 1.0.0
     */
    @Getter
    public enum log_level implements LogLevel {
        OFF(0) {
            @Override
            public boolean isEnabled() {
                return currentLogLevel() == 0;
            }
        },

        FATAL(1) {
            @Override
            public boolean isEnabled() {
                return currentLogLevel() >= 1;
            }
        },

        ERROR(2) {
            @Override
            public boolean isEnabled() {
                return currentLogLevel() >= 2;
            }
        },

        WARN(3) {
            @Override
            public boolean isEnabled() {
                return currentLogLevel() >= 3;
            }
        },

        INFO(4) {
            @Override
            public boolean isEnabled() {
                return currentLogLevel() >= 4;
            }
        },

        DEBUG(5) {
            @Override
            public boolean isEnabled() {
                return currentLogLevel() >= 5;
            }
        },

        VERBOSE(6) {
            @Override
            public boolean isEnabled() {
                return currentLogLevel() >= 6;
            }
        };

        /**
         * The integer value representing the log level.
         *
         * @since 1.0.0
         */
        final int value;

        log_level(int value) {
            this.value = value;
        }

        /**
         * Retrieves the current log level setting.
         *
         * @return The current log level.
         * @since 1.0.0
         */
        int currentLogLevel() {
            return LEDSuite.argumentsSettings.getLogLevel();
        }

        /**
         * Retrieves the log level enum corresponding to the specified value.
         *
         * @param logLevel The integer value representing the log level.
         * @return The corresponding log level enum.
         * @throws IllegalArgumentException If no corresponding log level is found.
         * @since 1.0.0
         */
        public static log_level valueOf(int logLevel) {
            for (log_level l : log_level.values()) {
                if (l.value == logLevel) return l;
            }
            throw new IllegalArgumentException("No enum value for '" + logLevel + "' found!");
        }

        /**
         * Interprets and retrieves the log level enum corresponding to the specified value.
         * Adjusts the value if it's out of range.
         *
         * @param logLevel The integer value representing the log level.
         * @return The corresponding log level enum.
         * @since 1.0.0
         */
        public static log_level interpret(int logLevel) {
            try {
                return valueOf(logLevel);
            } catch (IllegalArgumentException e) {
                return log_level.INFO; // Default to INFO level if out of range
            }
        }
    }
}
