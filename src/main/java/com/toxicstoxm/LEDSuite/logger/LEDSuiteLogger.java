package com.toxicstoxm.LEDSuite.logger;

import com.toxicstoxm.LEDSuite.logger.areas.*;
import com.toxicstoxm.LEDSuite.logger.colors.ColorConverter;
import com.toxicstoxm.LEDSuite.logger.colors.LEDSuiteMessage;
import com.toxicstoxm.LEDSuite.logger.levels.LEDSuiteLogLevels;
import com.toxicstoxm.LEDSuite.logger.levels.LogLevel;
import com.toxicstoxm.LEDSuite.settings.config.LEDSuiteSettingsBundle;
import lombok.NonNull;

import java.awt.*;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.toxicstoxm.LEDSuite.settings.config.LEDSuiteSettingsBundle.*;

public class LEDSuiteLogger implements Logger {

    public record LogMessageBluePrint(LogLevel logLevel, LogArea logArea, String message) {}

    public static LogAreaManager logAreaManager;
    public static Spacer elementSpacer;

    private LogArea defaultLogArea;
    private final PrintStream out;

    public LEDSuiteLogger(PrintStream out, LogArea defaultLogArea) {
        this.out = out;
        this.defaultLogArea = defaultLogArea;

        logAreaManager = new LEDSuiteLogAreaManger();
        logAreaManager.registerAreaBundle(new LEDSuiteLogAreas());

        elementSpacer = new LEDSuiteSpacer();
    }

    @Override
    public void setDefaultLogArea(@NonNull LogArea logArea) {
        defaultLogArea = logArea;
    }

    @Override
    public LogArea getDefaultLogArea() {
        return defaultLogArea;
    }

    private boolean hasDefaultLogArea() {
        return defaultLogArea != null;
    }

    @Override
    public void fatal(String message) {
        if (isLoggerDisabled()) return;
        if (hasDefaultLogArea()) fatal(message, defaultLogArea);

    }

    @Override
    public void error(String message) {
        if (isLoggerDisabled()) return;
        if (hasDefaultLogArea()) error(message, defaultLogArea);

    }

    @Override
    public void warn(String message) {
        if (isLoggerDisabled()) return;
        if (hasDefaultLogArea()) warn(message, defaultLogArea);

    }

    @Override
    public void info(String message) {
        if (isLoggerDisabled()) return;
        if (hasDefaultLogArea()) info(message, defaultLogArea);

    }

    @Override
    public void debug(String message) {
        if (isLoggerDisabled()) return;
        if (hasDefaultLogArea()) debug(message, defaultLogArea);

    }

    @Override
    public void verbose(String message) {
        if (isLoggerDisabled()) return;
        if (hasDefaultLogArea()) verbose(message, defaultLogArea);

    }

    @Override
    public void stacktrace(String message) {
        if (isLoggerDisabled()) return;
        if (hasDefaultLogArea()) stacktrace(message, defaultLogArea);

    }

    @Override
    public void fatal(String message, LogArea area) {
        if (isLoggerDisabled()) return;
        assembleLogMessage(
                new LogMessageBluePrint(
                        new LEDSuiteLogLevels.Fatal(
                                EnableFatalLevel.getInstance().get(),
                                FatalText.getInstance().get(),
                                ColorConverter.getColorFromHex(FatalColor.getInstance().get())
                        ),
                        area,
                        message
                )
        );
    }

    @Override
    public void error(String message, LogArea area) {
        if (isLoggerDisabled()) return;
        assembleLogMessage(
                new LogMessageBluePrint(
                        new LEDSuiteLogLevels.Error(
                                EnableErrorLevel.getInstance().get(),
                                ErrorText.getInstance().get(),
                                ColorConverter.getColorFromHex(ErrorColor.getInstance().get())
                        ),
                        area,
                        message
                )
        );
    }

    @Override
    public void warn(String message, LogArea area) {
        if (isLoggerDisabled()) return;
        assembleLogMessage(
                new LogMessageBluePrint(
                        new LEDSuiteLogLevels.Warn(
                                LEDSuiteSettingsBundle.EnableWarnLevel.getInstance().get(),
                                LEDSuiteSettingsBundle.WarnText.getInstance().get(),
                                ColorConverter.getColorFromHex(WarnColor.getInstance().get())
                        ),
                        area,
                        message
                )
        );
    }

    @Override
    public void info(String message, LogArea area) {
        if (isLoggerDisabled()) return;
        assembleLogMessage(
                new LogMessageBluePrint(
                        new LEDSuiteLogLevels.Info(
                                EnableInfoLevel.getInstance().get(),
                                InfoText.getInstance().get(),
                                ColorConverter.getColorFromHex(InfoColor.getInstance().get())
                        ),
                        area,
                        message
                )
        );
    }

    @Override
    public void debug(String message, LogArea area) {
        if (isLoggerDisabled()) return;
        assembleLogMessage(
                new LogMessageBluePrint(
                        new LEDSuiteLogLevels.Debug(
                                EnableDebugLevel.getInstance().get(),
                                DebugText.getInstance().get(),
                                ColorConverter.getColorFromHex(DebugColor.getInstance().get())
                        ),
                        area,
                        message
                )
        );
    }

    @Override
    public void verbose(String message, LogArea area) {
        if (isLoggerDisabled()) return;
        assembleLogMessage(
                new LogMessageBluePrint(
                        new LEDSuiteLogLevels.Verbose(
                                EnableVerboseLevel.getInstance().get(),
                                VerboseText.getInstance().get(),
                                ColorConverter.getColorFromHex(VerboseColor.getInstance().get())
                        ),
                        area,
                        message
                )
        );
    }

    @Override
    public void stacktrace(String message, LogArea area) {
        if (isLoggerDisabled()) return;
        assembleLogMessage(
                new LogMessageBluePrint(
                        new LEDSuiteLogLevels.Stacktrace(
                                EnableStacktraceLevel.getInstance().get(),
                                StacktraceText.getInstance().get(),
                                ColorConverter.getColorFromHex(StacktraceColor.getInstance().get())
                        ),
                        area,
                        message
                )
        );
    }

    private void assembleLogMessage(LogMessageBluePrint bluePrint) {
        LogLevel logLevel = bluePrint.logLevel;
        LogArea logArea = bluePrint.logArea;
        String message = bluePrint.message;

        if (logArea == null || logArea.getColor() == null) logArea = defaultLogArea;

        //log("LEVEL: " + logLevel.isEnabled() + "                     AREA: " + logAreaManager.isAreaEnabled(logArea));
        if (logLevel.isEnabled() && logAreaManager.isAreaEnabled(logArea)) {
            log(
                    LEDSuiteMessage.builder()
                            .text(
                                    elementSpacer.getSpacingFor("timestamp","[" + getTimestamp() + "]")
                            )
                            .color(
                                    EnableTrace.getInstance().get() &&
                                            EnableColorCoding.getInstance().get(),
                                    TraceColor.getInstance().get()
                            )
                            .text(
                                    EnableTrace.getInstance().get(),
                                    elementSpacer.getSpacingFor("trace", "[" + getTrace() + "]")
                            )
                            .reset(EnableTrace.getInstance().get())
                            .color(
                                    EnableColorCoding.getInstance().get(),
                                    logLevel.getColor()
                            )

                            .text(
                                    elementSpacer.getSpacingFor("level", "[" + logLevel.getText() + "]")
                            )
                            .reset()
                            .color(
                                    EnableAreas.getInstance().get() &&
                                            EnableColorCoding.getInstance().get(),
                                    logArea.getColor()
                            )
                            .text(
                                    EnableAreas.getInstance().get(),
                                   elementSpacer.getSpacingFor("area", "[" + logArea.getName() + "]" + Separator.getInstance().get())
                            )
                            .reset(EnableAreas.getInstance().get())
                            .color(
                                    EnableColorCoding.getInstance().get(),
                                    computeColor(logLevel, logArea)
                            )
                            .text(message)
                            .reset()
                            .build()
            );
        }
    }

    private Color computeColor(LogLevel level, LogArea area) {
        String mode = ColorCodingMode.getInstance().get();
        try {
            return switch (mode) {
                case "MIX" -> ColorConverter.mixColors(level.getColor(), area.getColor());
                case "AREA" -> area.getColor();
                case "LEVEL" -> level.getColor();
                case "STATIC" -> ColorConverter.getColorFromHex(ColorCodingStaticColor.getInstance().get());
                default -> hasDefaultLogArea() ? defaultLogArea.getColor() : new Color(-1, -1, -1);
            };
        } catch (IllegalArgumentException e) {
            log("Invalid mode '" + mode + "' Supported modes: 'MIX', 'AREA', 'LEVEL', 'STATIC'");
        }
        return new Color(0, 0, 0, 0);
    }

    private String getTimestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        return dateFormat.format(new Date());
    }

    private boolean isLoggerDisabled() {
        return !LEDSuiteSettingsBundle.EnableLogger.getInstance().get();
    }

    private String getTrace() {
        StackTraceElement currentTrace = Thread.currentThread().getStackTrace()[0];
        int cnt = 0;
        for (StackTraceElement traceElement : Thread.currentThread().getStackTrace()) {
            //System.out.println(traceElement.toString());
            if (!traceElement.toString().contains(this.getClass().getName()) && cnt >= 14) {
                currentTrace = traceElement;
                break;
            }
            cnt++;
        }
        return formatTrace(currentTrace.toString());
    }

    private String formatTrace(String trace) {
        return Arrays.stream(trace.split("\\(")).toList().getLast().replace("(", "").replace(")", "").replace(".java", "").strip();
    }

    private boolean isLogAreaEnabled() {
        List<String> enabledAreas = LEDSuiteSettingsBundle.ShownAreas.getInstance().get();
        return true;
    }

    @Override
    public void log(String message) {
        if (isLoggerDisabled()) return;
        out.println(message);
        out.flush();
    }
}
