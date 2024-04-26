package com.x_tornado10.Logger;

import com.x_tornado10.Main;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import javax.swing.*;
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
        if (log_level.INFO.isEnabled()) log( ansi().fg(DEFAULT).a("[INFO]: [" + Main.settings.getWindowTitle() + "] " + message).reset() );
    }
    // sending a warning message to console
    public void warn(String message) {
        if (log_level.WARN.isEnabled()) log( ansi().fgRgb(227, 163, 0).a("[WARN]: [" + Main.settings.getWindowTitle() + "] " + message).reset() );
    }
    // sending an error message to console
    public void error(String message) {
        if (log_level.ERROR.isEnabled()) log( ansi().fgRgb(255, 0, 0).a("[ERROR]: [" + Main.settings.getWindowTitle() + "] " + message).reset() );
    }
    // sending a fatal error message to console
    public void fatal(String message) {
        if (log_level.FATAL.isEnabled()) log( ansi().fgRgb(180, 0, 0).a("[FATAL]: [" + Main.settings.getWindowTitle() + "] " + message).reset() );
    }

    // sending debug message to console
    public void debug(String message) {
        if (log_level.DEBUG.isEnabled()) log( ansi().fgRgb(7, 94, 217).a("[DEBUG]: [" + Main.settings.getWindowTitle() + "] " + message).reset() );
    }

    // attaching current time to the front of the message before sending to console
    private void log(Ansi message) {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        System.out.println("[" + df.format(new Date()) + "] " + message);
    }

    // displaying info popup on screen
    public void info_popup(String message) {
        popup(message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
    // displaying warning popup on screen
    public void warn_popup(String message) {
        popup(message, "Warning", JOptionPane.WARNING_MESSAGE);
    }
    // displaying error popup on screen
    public void error_popup(String message) {
        popup(message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    // displaying fatal error popup on screen
    public void fatal_popup(String message) {
        popup(message, "Fatal Error", JOptionPane.ERROR_MESSAGE);
    }
    // displaying debug popup on screen
    public void debug_popup(String message) {
        popup(message, "Debug", JOptionPane.INFORMATION_MESSAGE);
    }

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
            return Main.settings.getLogLevel();
        }

    }


    // displaying popup on screen
    private void popup(String message, String title, int i) {
        JOptionPane.showMessageDialog(null, message, title, i);
    }
}
