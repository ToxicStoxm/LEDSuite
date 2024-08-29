package com.toxicstoxm.LEDSuite.settings.config;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class LEDSuiteSettingsBundle implements SettingsBundle {

    @YAMLSetting(path = "Logger.Enable")
    public static class EnableLogger extends LEDSuiteSetting<Boolean> {
        @Getter
        private static EnableLogger instance;

        public EnableLogger(Setting<Object> setting) {
            super(setting, Boolean.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.On-Demand-Trace.Enable")
    public static class EnableOnDemandLogger extends LEDSuiteSetting<Boolean> {
        @Getter
        private static EnableOnDemandLogger instance;

        public EnableOnDemandLogger(Setting<Object> setting) {
            super(setting, Boolean.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.On-Demand-Trace.Trace-Buffer-Limit")
    public static class TraceBufferLimit extends LEDSuiteSetting<Integer> {
        @Getter
        private static TraceBufferLimit instance;

        public TraceBufferLimit(Setting<Object> setting) {
            super(setting, Integer.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Color-Coding.Enable")
    public static class EnableColorCoding extends LEDSuiteSetting<Boolean> {
        @Getter
        private static EnableColorCoding instance;

        public EnableColorCoding(Setting<Object> setting) {
            super(setting, Boolean.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Color-Coding.Mode")
    public static class ColorCodingMode extends LEDSuiteSetting<String> {
        @Getter
        private static ColorCodingMode instance;

        public ColorCodingMode(Setting<Object> setting) {
            super(setting, String.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Color-Coding.Static-Color")
    public static class ColorCodingStaticColor extends LEDSuiteSetting<String> {
        @Getter
        private static ColorCodingStaticColor instance;

        public ColorCodingStaticColor(Setting<Object> setting) {
            super(setting, String.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Trace.Enable")
    public static class EnableTrace extends LEDSuiteSetting<Boolean> {
        @Getter
        private static EnableTrace instance;

        public EnableTrace(Setting<Object> setting) {
            super(setting, Boolean.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Trace.Color")
    public static class TraceColor extends LEDSuiteSetting<String> {
        @Getter
        private static TraceColor instance;

        public TraceColor(Setting<Object> setting) {
            super(setting, String.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Spacing.Enable-Auto-Spacing")
    public static class EnableAutoSpacing extends LEDSuiteSetting<Boolean> {
        @Getter
        private static EnableAutoSpacing instance;

        public EnableAutoSpacing(Setting<Object> setting) {
            super(setting, Boolean.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Spacing.Base-Spacing")
    public static class BaseSpacing extends LEDSuiteSetting<String> {
        @Getter
        private static BaseSpacing instance;

        public BaseSpacing(Setting<Object> setting) {
            super(setting, String.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Spacing.Enable-Auto-Reset")
    public static class EnableAutoReset extends LEDSuiteSetting<Boolean> {
        @Getter
        private static EnableAutoReset instance;

        public EnableAutoReset(Setting<Object> setting) {
            super(setting, Boolean.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Separator")
    public static class Separator extends LEDSuiteSetting<String> {
        @Getter
        private static Separator instance;

        public Separator(Setting<Object> setting) {
            super(setting, String.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Levels.Enable")
    public static class EnableLevels extends LEDSuiteSetting<Boolean> {
        @Getter
        private static EnableLevels instance;

        public EnableLevels(Setting<Object> setting) {
            super(setting, Boolean.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Levels.Fatal.Enable")
    public static class EnableFatalLevel extends LEDSuiteSetting<Boolean> {
        @Getter
        private static EnableFatalLevel instance;

        public EnableFatalLevel(Setting<Object> setting) {
            super(setting, Boolean.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Levels.Fatal.Text")
    public static class FatalText extends LEDSuiteSetting<String> {
        @Getter
        private static FatalText instance;

        public FatalText(Setting<Object> setting) {
            super(setting, String.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Levels.Fatal.Color")
    public static class FatalColor extends LEDSuiteSetting<String> {
        @Getter
        private static FatalColor instance;

        public FatalColor(Setting<Object> setting) {
            super(setting, String.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Levels.Error.Enable")
    public static class EnableErrorLevel extends LEDSuiteSetting<Boolean> {
        @Getter
        private static EnableErrorLevel instance;

        public EnableErrorLevel(Setting<Object> setting) {
            super(setting, Boolean.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Levels.Error.Text")
    public static class ErrorText extends LEDSuiteSetting<String> {
        @Getter
        private static ErrorText instance;

        public ErrorText(Setting<Object> setting) {
            super(setting, String.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Levels.Error.Color")
    public static class ErrorColor extends LEDSuiteSetting<String> {
        @Getter
        private static ErrorColor instance;

        public ErrorColor(Setting<Object> setting) {
            super(setting, String.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Levels.Warn.Enable")
    public static class EnableWarnLevel extends LEDSuiteSetting<Boolean> {
        @Getter
        private static EnableWarnLevel instance;

        public EnableWarnLevel(Setting<Object> setting) {
            super(setting, Boolean.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Levels.Warn.Text")
    public static class WarnText extends LEDSuiteSetting<String> {
        @Getter
        private static WarnText instance;

        public WarnText(Setting<Object> setting) {
            super(setting, String.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Levels.Warn.Color")
    public static class WarnColor extends LEDSuiteSetting<String> {
        @Getter
        private static WarnColor instance;

        public WarnColor(Setting<Object> setting) {
            super(setting, String.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Levels.Info.Enable")
    public static class EnableInfoLevel extends LEDSuiteSetting<Boolean> {
        @Getter
        private static EnableInfoLevel instance;

        public EnableInfoLevel(Setting<Object> setting) {
            super(setting, Boolean.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Levels.Info.Text")
    public static class InfoText extends LEDSuiteSetting<String> {
        @Getter
        private static InfoText instance;

        public InfoText(Setting<Object> setting) {
            super(setting, String.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Levels.Info.Color")
    public static class InfoColor extends LEDSuiteSetting<String> {
        @Getter
        private static InfoColor instance;

        public InfoColor(Setting<Object> setting) {
            super(setting, String.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Levels.Debug.Enable")
    public static class EnableDebugLevel extends LEDSuiteSetting<Boolean> {
        @Getter
        private static EnableDebugLevel instance;

        public EnableDebugLevel(Setting<Object> setting) {
            super(setting, Boolean.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Levels.Debug.Text")
    public static class DebugText extends LEDSuiteSetting<String> {
        @Getter
        private static DebugText instance;

        public DebugText(Setting<Object> setting) {
            super(setting, String.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Levels.Debug.Color")
    public static class DebugColor extends LEDSuiteSetting<String> {
        @Getter
        private static DebugColor instance;

        public DebugColor(Setting<Object> setting) {
            super(setting, String.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Levels.Verbose.Enable")
    public static class EnableVerboseLevel extends LEDSuiteSetting<Boolean> {
        @Getter
        private static EnableVerboseLevel instance;

        public EnableVerboseLevel(Setting<Object> setting) {
            super(setting, Boolean.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Levels.Verbose.Text")
    public static class VerboseText extends LEDSuiteSetting<String> {
        @Getter
        private static VerboseText instance;

        public VerboseText(Setting<Object> setting) {
            super(setting, String.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Levels.Verbose.Color")
    public static class VerboseColor extends LEDSuiteSetting<String> {
        @Getter
        private static VerboseColor instance;

        public VerboseColor(Setting<Object> setting) {
            super(setting, String.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Levels.Stacktrace.Enable")
    public static class EnableStacktraceLevel extends LEDSuiteSetting<Boolean> {
        @Getter
        private static EnableStacktraceLevel instance;

        public EnableStacktraceLevel(Setting<Object> setting) {
            super(setting, Boolean.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Levels.Stacktrace.Text")
    public static class StacktraceText extends LEDSuiteSetting<String> {
        @Getter
        private static StacktraceText instance;

        public StacktraceText(Setting<Object> setting) {
            super(setting, String.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Levels.Stacktrace.Color")
    public static class StacktraceColor extends LEDSuiteSetting<String> {
        @Getter
        private static StacktraceColor instance;

        public StacktraceColor(Setting<Object> setting) {
            super(setting, String.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Areas.Enabled")
    public static class EnableAreas extends LEDSuiteSetting<Boolean> {
        @Getter
        private static EnableAreas instance;

        public EnableAreas(Setting<Object> setting) {
            super(setting, Boolean.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Areas.Shown-Areas")
    public static class ShownAreas extends LEDSuiteSetting<List<String>> {
        @Getter
        private static ShownAreas instance;

        static {
            instance = new ShownAreas(new LEDSuiteSetting<>(new ArrayList<>()));
        }

        public ShownAreas(Setting<Object> setting) {
            super(setting, (Class<List<String>>) (Class<?>) List.class);
            instance = this;
        }
    }
}
