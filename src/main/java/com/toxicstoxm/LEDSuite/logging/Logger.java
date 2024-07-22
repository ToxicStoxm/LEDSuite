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
import java.util.Date;

import static org.fusesource.jansi.Ansi.Color.DEFAULT;
import static org.fusesource.jansi.Ansi.ansi;

public class Logger {

    // activating ansi library to translate and display color codes in the console
    public Logger() {
        AnsiConsole.systemInstall();
    }
    // formatting info message
    public void info(String message) {
        if (log_level.INFO.isEnabled()) cInfo("[INFO]:  [" + Constants.Application.NAME + "] " + message);
    }
    // sending and info message to console and log file
    private void cInfo(String message) {
        writeLog(message);
        log(ansi().fg(DEFAULT).a(message).reset());
    }
    // formatting warn message
    public void warn(String message) {
        if (log_level.WARN.isEnabled()) cWarn("[WARN]:  [" + Constants.Application.NAME + "] " + message);
    }
    // sending a warning message to console and log file
    private void cWarn(String message) {
        writeLog(message);
        log(ansi().fgRgb(227, 163, 0).a(message).reset());
    }
    public void error(String message) {
        error(message, false);
    }
    // formatting error message
    public void error(String message, boolean visualFeedback) {
        if (log_level.ERROR.isEnabled()) {
            cError( "[ERROR]: [" + Constants.Application.NAME + "] " + message);
        }
        // displaying toast in the user interface
        if (visualFeedback) visualFeedback(message, 0);
    }
    public void error(Exception e) {
        StackTraceElement[] stackTrace = e.getStackTrace();
        Throwable t = e.getCause();

        if (log_level.ERROR.isEnabled()) {
            debug("Error message: " + e);
            debug("Stack trace:");
            for (StackTraceElement s : stackTrace) {
                debug(s.toString());
            }
            if (t != null) {
                debug("Cause:");
                debug(t.toString());
            }
        }
    }
    // sending an error message to console and log file
    private void cError(String message) {
        writeLog(message);
        log(ansi().fgRgb(255, 0, 0).a(message).reset());
    }
    // formatting fatal message
    public void fatal(String message) {
        fatal(message, false);
    }
    public void fatal(String message, boolean visualFeedback) {
        if (log_level.FATAL.isEnabled()) cFatal("[FATAL]: [" + Constants.Application.NAME + "] " + message);
        // displaying toast in the user interface
        if (visualFeedback) visualFeedback(message, 0);
    }
    // sending a fatal error message to console and log file
    private void cFatal(String message) {
        writeLog(message);
        log(ansi().fgRgb(180, 0, 0).a(message).reset());
    }
    // formatting debug message
    public void debug(String message) {
        if (log_level.DEBUG.isEnabled()) cDebug("[DEBUG]: [" + Constants.Application.NAME + "] " + message);
    }
    // sending debug message to console and log file
    private void cDebug(String message) {
        writeLog(message);
        log(ansi().fgRgb(7, 94, 217).a(message).reset());
    }

    public void verbose(String message) {
        if (log_level.VERBOSE.isEnabled()) cVerbose("[VERBOSE]: [" + Constants.Application.NAME + "] " + message);
    }
    // sending debug message to console and log file
    private void cVerbose(String message) {
        writeLog(message);
        log(ansi().fgRgb(160, 22, 244).a(message).reset());
    }

    // attaching current time to the front of the message before sending it to the console
    private String attachMetadata(String message) {
        String s = Thread.currentThread().getStackTrace()[5].toString();
        String trace = s.substring(s.indexOf("(") + 1, s.lastIndexOf(")"));
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        return "[" + df.format(new Date()) + "] " +  "[" + trace + "] " + message;
    }
    private String attachMetadata(Ansi message) {
        String s = Thread.currentThread().getStackTrace()[5].toString();
        String trace = s.substring(s.indexOf("(") + 1, s.lastIndexOf(")"));
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        return "[" + df.format(new Date()) + "] " + ansi().fgRgb(0, 148, 50).a( "[" + trace + "] ").reset() + message;
    }

    public void log(String message) {
        log(message, true);
    }

    public void log(String message, boolean newLine) {
        String finalMessage = attachMetadata(message);
        if (newLine) System.out.println(finalMessage);
        else System.out.print(finalMessage);
    }

    // final log function used to send the message to the console
    private void log(Ansi message) {
        System.out.println(attachMetadata(message));
    }

    // writing console log to log file
    private void writeLog(String message) {
        String temp = attachMetadata(message);
        new LEDSuiteRunnable() {
            @Override
            public void run() {
                if (!new File(Constants.File_System.logFile).exists()) return;
                // new buffered writer is used to write logging information from console to the log file
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(Constants.File_System.logFile, true))) {
                    // attaching time stamp to message before writing it to the file
                    writer.write(temp);
                    writer.newLine();
                } catch (IOException e) {
                    System.out.println("Error while trying to write log to log file!");
                    System.out.println("If this message is displayed repeatedly:");
                    System.out.println(Constants.Messages.WARN.OPEN_GITHUB_ISSUE);
                    throw new RuntimeException(e);
                }
            }
        }.runTaskAsynchronously();
    }

    // display a toast containing a message, with standard 5s timeout
    private void visualFeedback(String message) {
        visualFeedback(message, 5);
    }
    // display a toast containing a message, with specific timeout
    private void visualFeedback(String message, int timeout) {
        // null check for toast overlay
        if (LEDSuite.mainWindow == null) return;
        ToastOverlay toastOverlay = LEDSuite.mainWindow.toastOverlay;
        if (toastOverlay != null) {
            // create new toast containing the message and specific timeout
            toastOverlay
                    .addToast(
                            Toast.builder()
                                    .setTimeout(timeout)
                                    .setTitle(message)
                                    .build()
                    );
        }
    }

    // log level checker
    // used to determine what messages should be logged / send to console and vice versa
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
                //LCCP.sysBeep();
                return currentLogLevel() >= 1;
            }
        },

        ERROR(2) {
            @Override
            public boolean isEnabled() {
                //LCCP.sysBeep();
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

        final int value;

        log_level(int value) {
            this.value = value;
        }
        // function that retrieves current log level
        int currentLogLevel() {
            return LEDSuite.argumentsSettings.getLogLevel();
        }

        public static log_level valueOf(int logLevel) {
            for (log_level l : log_level.values()) {
                if (l.value == logLevel) return l;
            }
            throw new IllegalArgumentException("No enum value for '" + logLevel + "' found!");
        }

        public static log_level interpret(int logLevel) {
            try {
                return valueOf(logLevel);
            } catch (IllegalArgumentException e) {
                int max = Logger.log_level.values().length;
                int min = 0;
                try {
                    return valueOf(Math.max(
                                    Math.min(
                                            logLevel,
                                            max
                                    ),
                                    min
                            )
                    );
                } catch (IllegalArgumentException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
}