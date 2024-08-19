package com.toxicstoxm.LEDSuite.logger;

import com.toxicstoxm.LEDSuite.logger.areas.LogArea;
import com.toxicstoxm.LEDSuite.logger.levels.LEDSuiteLogLevels;
import com.toxicstoxm.LEDSuite.logger.levels.LogLevel;
import lombok.NonNull;

import java.awt.*;
import java.io.PrintStream;

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
                                true,
                                "FATAL",
                                new Color(115, 0, 0)
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
                                true,
                                "ERROR",
                                new Color(255, 0, 0)
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
                                true,
                                "WARN",
                                new Color(255, 220, 21)
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
                        new LEDSuiteLogLevels.Error(
                                true,
                                "INFO",
                                new Color(228, 228, 228)
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
                        new LEDSuiteLogLevels.Error(
                                true,
                                "DEBUG",
                                new Color(0, 140, 255)
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
                        new LEDSuiteLogLevels.Error(
                                true,
                                "VERBOSE",
                                new Color(131, 0, 255)
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
                        new LEDSuiteLogLevels.Error(
                                true,
                                "STACKTRACE",
                                new Color(71, 71, 71)
                        ),
                        area,
                        message
                )
        );
    }

    private void assembleLogMessage(LogMessageBluePrint logMessageBluePrint) {

    }

    @Override
    public void log(String message) {
       out.println(message);
    }
}
