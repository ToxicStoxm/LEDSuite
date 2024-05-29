package com.x_tornado10.lccp.util.logging;

import com.x_tornado10.lccp.LCCP;
import com.x_tornado10.lccp.util.Paths;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.fusesource.jansi.Ansi.*;
import static org.fusesource.jansi.Ansi.Color.*;

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
        if (log_level.DEBUG.isEnabled())  cDebug("[DEBUG]: [" + LCCP.settings.getWindowTitle() + "] " + message);
    }
    // sending debug message to console and log file
    public void cDebug(String message) {
        writeLog(message);
        log(ansi().fgRgb(7, 94, 217).a(message).reset());
    }

    // attaching current time to the front of the message before sending to console
    private void log(Ansi message) {
        System.out.println(attachTime(message));
    }
    private String attachTime(String message) {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        return "[" + df.format(new Date()) + "] " + message;
    }
    private String attachTime(Ansi message) {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        return "[" + df.format(new Date()) + "] " + message;
    }

    // write log to log file
    private void writeLog(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.logFile, true))) {
            writer.write(attachTime(message));
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // log level checker
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
                LCCP.sysBeep();
                return currentLogLevel() >= 2;
            }
        },
        FATAL() {
            @Override
            public boolean isEnabled() {
                LCCP.sysBeep();
                return currentLogLevel() >= 1;
            }
        },
        DEBUG() {
            @Override
            public boolean isEnabled() {
                return currentLogLevel() >= 5;
            }
        };
        int currentLogLevel() {
            return LCCP.settings.getLogLevel();
        }
    }
}
