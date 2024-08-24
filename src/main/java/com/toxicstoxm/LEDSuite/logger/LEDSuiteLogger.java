package com.toxicstoxm.LEDSuite.logger;

import com.toxicstoxm.LEDSuite.logger.areas.LogArea;
import com.toxicstoxm.LEDSuite.logger.colors.ColorConverter;
import com.toxicstoxm.LEDSuite.logger.colors.LEDSuiteMessage;
import com.toxicstoxm.LEDSuite.logger.levels.LEDSuiteLogLevels;
import com.toxicstoxm.LEDSuite.logger.levels.LogLevel;
import com.toxicstoxm.LEDSuite.logger.placeholders.LEDSuitePlaceholderManager;
import com.toxicstoxm.LEDSuite.logger.placeholders.Placeholder;
import com.toxicstoxm.LEDSuite.logger.placeholders.PlaceholderManager;
import com.toxicstoxm.LEDSuite.logger.placeholders.PlaceholderReplacer;
import com.toxicstoxm.LEDSuite.settings.config.LEDSuiteSettingsBundle;
import lombok.NonNull;

import java.awt.*;
import java.io.PrintStream;
import java.net.Socket;
import java.net.http.WebSocket;
import java.util.List;

public class LEDSuiteLogger implements Logger {

    private record LogMessageBluePrint(LogLevel logLevel, LogArea logArea, String message) {}

    private LogArea defaultLogArea;
    private final PrintStream out;

    public LEDSuiteLogger(PrintStream out, LogArea defaultLogArea) {
        this.out = out;
        this.defaultLogArea = defaultLogArea;
    }

    @Override
    public void setDefaultLogArea(@NonNull LogArea logArea) {
        defaultLogArea = logArea;
    }

    @Override
    public LogArea getLogArea() {
        return defaultLogArea;
    }

    private boolean hasDefaultLogArea() {
        return defaultLogArea != null;
    }

    @Override
    public void fatal(String message) {
        if (hasDefaultLogArea()) fatal(message, defaultLogArea);

    }

    @Override
    public void error(String message) {
        if (hasDefaultLogArea()) error(message, defaultLogArea);

    }

    @Override
    public void warn(String message) {
        if (hasDefaultLogArea()) warn(message, defaultLogArea);

    }

    @Override
    public void info(String message) {
        if (hasDefaultLogArea()) info(message, defaultLogArea);

    }

    @Override
    public void debug(String message) {
        if (hasDefaultLogArea()) debug(message, defaultLogArea);

    }

    @Override
    public void verbose(String message) {
        if (hasDefaultLogArea()) verbose(message, defaultLogArea);

    }

    @Override
    public void stacktrace(String message) {
        if (hasDefaultLogArea()) stacktrace(message, defaultLogArea);

    }

    @Override
    public void fatal(String message, LogArea area) {
        assembleLogMessage(
                new LogMessageBluePrint(
                        new LEDSuiteLogLevels.Fatal(
                                LEDSuiteSettingsBundle.EnableFatalLevel.getInstance().get(),
                                LEDSuiteSettingsBundle.FatalText.getInstance().get(),
                                ColorConverter.getColorFromHex(LEDSuiteSettingsBundle.FatalColor.getInstance().get())
                        ),
                        area,
                        message
                )
        );
    }

    @Override
    public void error(String message, LogArea area) {
        assembleLogMessage(
                new LogMessageBluePrint(
                        new LEDSuiteLogLevels.Error(
                                LEDSuiteSettingsBundle.EnableErrorLevel.getInstance().get(),
                                LEDSuiteSettingsBundle.ErrorText.getInstance().get(),
                                ColorConverter.getColorFromHex(LEDSuiteSettingsBundle.ErrorColor.getInstance().get())
                        ),
                        area,
                        message
                )
        );
    }

    @Override
    public void warn(String message, LogArea area) {
        assembleLogMessage(
                new LogMessageBluePrint(
                        new LEDSuiteLogLevels.Warn(
                                LEDSuiteSettingsBundle.EnableWarnLevel.getInstance().get(),
                                LEDSuiteSettingsBundle.WarnText.getInstance().get(),
                                ColorConverter.getColorFromHex(LEDSuiteSettingsBundle.WarnColor.getInstance().get())
                        ),
                        area,
                        message
                )
        );
    }

    @Override
    public void info(String message, LogArea area) {
        assembleLogMessage(
                new LogMessageBluePrint(
                        new LEDSuiteLogLevels.Info(
                                LEDSuiteSettingsBundle.EnableInfoLevel.getInstance().get(),
                                LEDSuiteSettingsBundle.InfoText.getInstance().get(),
                                ColorConverter.getColorFromHex(LEDSuiteSettingsBundle.InfoColor.getInstance().get())
                        ),
                        area,
                        message
                )
        );
    }

    @Override
    public void debug(String message, LogArea area) {
        assembleLogMessage(
                new LogMessageBluePrint(
                        new LEDSuiteLogLevels.Debug(
                                LEDSuiteSettingsBundle.EnableDebugLevel.getInstance().get(),
                                LEDSuiteSettingsBundle.DebugText.getInstance().get(),
                                ColorConverter.getColorFromHex(LEDSuiteSettingsBundle.DebugColor.getInstance().get())
                        ),
                        area,
                        message
                )
        );
    }

    @Override
    public void verbose(String message, LogArea area) {

        assembleLogMessage(
                new LogMessageBluePrint(
                        new LEDSuiteLogLevels.Verbose(
                                LEDSuiteSettingsBundle.EnableVerboseLevel.getInstance().get(),
                                LEDSuiteSettingsBundle.VerboseText.getInstance().get(),
                                ColorConverter.getColorFromHex(LEDSuiteSettingsBundle.VerboseColor.getInstance().get())
                        ),
                        area,
                        message
                )
        );
    }

    @Override
    public void stacktrace(String message, LogArea area) {
        assembleLogMessage(
                new LogMessageBluePrint(
                        new LEDSuiteLogLevels.Stacktrace(
                                LEDSuiteSettingsBundle.EnableStacktraceLevel.getInstance().get(),
                                LEDSuiteSettingsBundle.StacktraceText.getInstance().get(),
                                ColorConverter.getColorFromHex(LEDSuiteSettingsBundle.StacktraceColor.getInstance().get())
                        ),
                        area,
                        message
                )
        );
    }

    private void assembleLogMessage(LogMessageBluePrint logMessageBluePrint) {
        if (!LEDSuiteSettingsBundle.EnableLogger.getInstance().get()) return;
        if (!check(logMessageBluePrint)) return;

        String result = LEDSuiteSettingsBundle.LoggerStyle.getInstance().get();
        PlaceholderManager placeholderManager = LEDSuitePlaceholderManager.withDefault();

        LogLevel logLevel = logMessageBluePrint.logLevel;
        LogArea logArea = logMessageBluePrint.logArea;
        String message = logMessageBluePrint.message;

        if (LEDSuiteSettingsBundle.EnableLevels.getInstance().get()) {
            if (!logLevel.isEnabled()) {
                System.out.println("Log level " + logLevel +  " is disabled!");
                return;
            }
            placeholderManager.registerPlaceholder(
                    new Placeholder() {
                        @Override
                        public String getText() {
                            return "LOG_LEVEL";
                        }

                        @Override
                        public char getRegex() {
                            return '%';
                        }
                    },
                    _ -> logLevel.getText()
            );
        }
        placeholderManager.registerPlaceholder(
                new Placeholder() {
                    @Override
                    public String getText() {
                        return "TRACE";
                    }

                    @Override
                    public char getRegex() {
                        return '%';
                    }
                },
                _ -> getTrace()
        );


        //result = result.text(message);


        log(
                placeholderManager.processPlaceholders(
                        result
                )
        );
    }


    private String getTrace() {
        StackTraceElement currentTrace = Thread.currentThread().getStackTrace()[0];
        for (StackTraceElement traceElement : Thread.currentThread().getStackTrace()) {
            if (!traceElement.toString().contains(this.getClass().getName())) {
                currentTrace = traceElement;
                break;
            }
        }
        return formatTrace(currentTrace.toString()); /*LEDSuiteMessage.builder()
                .color(ColorConverter.getColorFromHex(LEDSuiteSettingsBundle.TraceColor.getInstance().get()))
                .text(formatTrace(currentTrace.toString()))
                .getMessage();*/

    }

    private String formatTrace(String trace) {
        String result = LEDSuiteSettingsBundle.TraceStyle.getInstance().get();

        LEDSuitePlaceholderManager placeholderManager = new LEDSuitePlaceholderManager();

        placeholderManager.registerPlaceholder(
                new Placeholder() {
                    @Override
                    public String getText() {
                        return "CLASS_PATH";
                    }

                    @Override
                    public char getRegex() {
                        return '%';
                    }
                },
                placeholder -> trace
        );

        return placeholderManager.processPlaceholders(
                result
        );
    }

    private boolean isLogAreaEnabled() {
        List<String> enabledAreas = LEDSuiteSettingsBundle.ShownAreas.getInstance().get();
        return true;
    }

    private boolean check(LogMessageBluePrint bP) {
        return bP != null && bP.logLevel != null && bP.logArea != null && bP.message != null;
    }

    @Override
    public void log(String message) {
       out.println(message);
       out.flush();
    }
}
