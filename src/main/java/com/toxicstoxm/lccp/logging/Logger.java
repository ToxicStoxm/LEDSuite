package com.toxicstoxm.lccp.logging;

import com.toxicstoxm.lccp.LCCP;
import com.toxicstoxm.lccp.Constants;
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
    public void cInfo(String message) {
        writeLog(message);
        log(ansi().fg(DEFAULT).a(message).reset());
    }
    // formatting warn message
    public void warn(String message) {
        if (log_level.WARN.isEnabled()) cWarn("[WARN]:  [" + Constants.Application.NAME + "] " + message);
    }
    // sending a warning message to console and log file
    public void cWarn(String message) {
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
            // displaying toast in the user interface

        }
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
    public void cError(String message) {
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
    public void cFatal(String message) {
        writeLog(message);
        log(ansi().fgRgb(180, 0, 0).a(message).reset());
    }
    // formatting debug message
    public void debug(String message) {
        if (log_level.DEBUG.isEnabled()) cDebug("[DEBUG]: [" + Constants.Application.NAME + "] " + message);
    }
    // sending debug message to console and log file
    public void cDebug(String message) {
        writeLog(message);
        log(ansi().fgRgb(7, 94, 217).a(message).reset());
    }

    // attaching current time to the front of the message before sending it to the console
    private String attachTime(String message) {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        return "[" + df.format(new Date()) + "] " + message;
    }
    private String attachTime(Ansi message) {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        return "[" + df.format(new Date()) + "] " + message;
    }
    // final log function used to send the message to the console
    private void log(Ansi message) {
        System.out.println(attachTime(message));
    }

    // writing console log to log file
    private void writeLog(String message) {
        if (!new File(Constants.File_System.logFile).exists()) return;
        // new buffered writer is used to write logging information from console to the log file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(Constants.File_System.logFile, true))) {
            // attaching time stamp to message before writing it to the file
            writer.write(attachTime(message));
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error while trying to write log to log file!");
            System.out.println("If this message is displayed repeatedly:");
            System.out.println(Constants.Messages.WARN.OPEN_GITHUB_ISSUE);
            throw new RuntimeException(e);
        }
    }

    // display a toast containing a message, with standard 5s timeout
    private void visualFeedback(String message) {
       visualFeedback(message, 5);
    }
    // display a toast containing a message, with specific timeout
    private void visualFeedback(String message, int timeout) {
        // null check for toast overlay
        if (LCCP.mainWindow == null) return;
        ToastOverlay toastOverlay = LCCP.mainWindow.toastOverlay;
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
    public enum log_level implements LogLevel {
        INFO() {
            @Override
            public boolean isEnabled() {
                return currentLogLevel() >= 4;
            }
        },
        WARN() {
            @Override
            public boolean isEnabled() {
                return currentLogLevel() >= 3;
            }
        },
        ERROR() {
            @Override
            public boolean isEnabled() {
                //LCCP.sysBeep();
                return currentLogLevel() >= 2;
            }
        },
        FATAL() {
            @Override
            public boolean isEnabled() {
                //LCCP.sysBeep();
                return currentLogLevel() >= 1;
            }
        },
        DEBUG() {
            @Override
            public boolean isEnabled() {
                return currentLogLevel() >= 5;
            }
        };
        // function that retrieves current log level
        int currentLogLevel() {
            return LCCP.settings.getLogLevel();
        }
    }
}
