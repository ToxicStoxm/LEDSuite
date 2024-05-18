package com.x_tornado10.lccp.Logger;

import com.x_tornado10.lccp.LCCP;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.fusesource.jansi.Ansi.*;
import static org.fusesource.jansi.Ansi.Color.*;

public class Logger {

    // activating ansi library to translate and display color codes in the console
    public Logger() {
        AnsiConsole.systemInstall();
    }
    // sending and info message to console
    public void info(String message) {
        if (log_level.INFO.isEnabled()) log( ansi().fg(DEFAULT).a("[INFO]: [" + LCCP.settings.getWindowTitle() + "] " + message).reset() );
    }
    // sending a warning message to console
    public void warn(String message) {
        if (log_level.WARN.isEnabled()) log( ansi().fgRgb(227, 163, 0).a("[WARN]: [" + LCCP.settings.getWindowTitle() + "] " + message).reset() );
    }
    // sending an error message to console
    public void error(String message) {
        if (log_level.ERROR.isEnabled()) log( ansi().fgRgb(255, 0, 0).a("[ERROR]: [" + LCCP.settings.getWindowTitle() + "] " + message).reset() );
    }
    // sending a fatal error message to console
    public void fatal(String message) {
        if (log_level.FATAL.isEnabled()) log( ansi().fgRgb(180, 0, 0).a("[FATAL]: [" + LCCP.settings.getWindowTitle() + "] " + message).reset() );
    }

    // sending debug message to console
    public void debug(String message) {
        if (log_level.DEBUG.isEnabled()) log( ansi().fgRgb(7, 94, 217).a("[DEBUG]: [" + LCCP.settings.getWindowTitle() + "] " + message).reset() );
    }

    // attaching current time to the front of the message before sending to console
    private void log(Ansi message) {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        System.out.println("[" + df.format(new Date()) + "] " + message);
    }

    // log level checker
    public enum log_level implements Log_Level {
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
                return currentLogLevel() >= 2;
            }
        },
        FATAL() {
            @Override
            public boolean isEnabled() {
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
            return 5;
        }
    }
}
