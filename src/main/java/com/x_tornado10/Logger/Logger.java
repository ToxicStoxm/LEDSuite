package com.x_tornado10.Logger;

import com.x_tornado10.Main;
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
        log( ansi().fg(DEFAULT).a("[INFO]: [" + Main.settings.getWindowTitle() + "] " + message).reset() );
    }
    // sending a warning message to console
    public void warn(String message) {
        log( ansi().fgRgb(227, 163, 0).a("[WARN]: [" + Main.settings.getWindowTitle() + "] " + message).reset() );
    }
    // sending an error message to console
    public void error(String message) {
        log( ansi().fgRgb(255, 0, 0).a("[ERROR]: [" + Main.settings.getWindowTitle() + "] " + message).reset() );
    }
    // sending a fatal error message to console
    public void fatal(String message) {
        log( ansi().fgRgb(180, 0, 0).a("[FATAL]: [" + Main.settings.getWindowTitle() + "] " + message).reset() );
    }

    // attaching current time to the front of the message before sending to console
    private void log(Ansi message) {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        System.out.println("[" + df.format(new Date()) + "] " + message);
    }
}
