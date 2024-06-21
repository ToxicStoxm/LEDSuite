package com.x_tornado10.lccp.util.logging;

import com.x_tornado10.lccp.LCCP;
import com.x_tornado10.lccp.util.Paths;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

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
        if (log_level.INFO.isEnabled()) cInfo("[INFO]:  [" + LCCP.settings.getWindowTitle() + "] " + message);
    }
    // sending and info message to console and log file
    public void cInfo(String message) {
        writeLog(message);
        log(ansi().fg(DEFAULT).a(message).reset());
    }
    // formatting warn message
    public void warn(String message) {
        if (log_level.WARN.isEnabled()) cWarn("[WARN]:  [" + LCCP.settings.getWindowTitle() + "] " + message);
    }
    // sending a warning message to console and log file
    public void cWarn(String message) {
        writeLog(message);
        log(ansi().fgRgb(227, 163, 0).a(message).reset());
    }
    // formatting error message
    public void error(String message) {
        if (log_level.ERROR.isEnabled()) cError( "[ERROR]: [" + LCCP.settings.getWindowTitle() + "] " + message);
    }
    public void error(Exception e) {
        StackTraceElement[] stackTrace = e.getStackTrace();
        Throwable t = e.getCause();

        if (log_level.ERROR.isEnabled()) {

            error("Stack trace:");
            for (StackTraceElement s : stackTrace) {
                error(e.toString());
            }
            if (t != null) {
                error("Cause:");
                error(t.toString());
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
        if (log_level.FATAL.isEnabled()) cFatal("[FATAL]: [" + LCCP.settings.getWindowTitle() + "] " + message);
    }
    // sending a fatal error message to console and log file
    public void cFatal(String message) {
        writeLog(message);
        log(ansi().fgRgb(180, 0, 0).a(message).reset());
    }
    // formatting debug message
    public void debug(String message) {
        if (log_level.DEBUG.isEnabled()) cDebug("[DEBUG]: [" + LCCP.settings.getWindowTitle() + "] " + message);
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
        if (!new File(Paths.File_System.logFile).exists()) return;
        // new buffered writer is used to write logging information from console to the log file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.File_System.logFile, true))) {
            // attaching time stamp to message before writing it to the file
            writer.write(attachTime(message));
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error while trying to write log to log file!");
            System.out.println("If this message is displayed repeatedly:");
            System.out.println(Messages.WARN.OPEN_GITHUB_ISSUE);
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
