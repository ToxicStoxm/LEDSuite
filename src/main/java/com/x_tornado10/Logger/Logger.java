package com.x_tornado10.Logger;

import com.x_tornado10.Main;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.fusesource.jansi.Ansi.*;
import static org.fusesource.jansi.Ansi.Color.*;

public class Logger {
    public Logger() {
        AnsiConsole.systemInstall();
    }
    public void info(String message) {
        log( ansi().fg(DEFAULT).a("[INFO]: [" + Main.settings.getWindowTitle() + "] " + message).reset() );
    }
    public void warn(String message) {
        log( ansi().fgRgb(227, 163, 0).a("[WARN]: [" + Main.settings.getWindowTitle() + "] " + message).reset() );
    }
    public void error(String message) {
        log( ansi().fgRgb(255, 0, 0).a("[ERROR]: [" + Main.settings.getWindowTitle() + "] " + message).reset() );
    }
    public void fatal(String message) {
        log( ansi().fgRgb(180, 0, 0).a("[FATAL]: [" + Main.settings.getWindowTitle() + "] " + message).reset() );
    }

    private void log(Ansi message) {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        System.out.println("[" + df.format(new Date()) + "] " + message);
    }
}
