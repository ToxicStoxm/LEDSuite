package com.toxicstoxm.LEDSuite.settings.config;

import lombok.Getter;

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

    @YAMLSetting(path = "Logger.Color-Coding.Priority.Log-Level.Modes.Self")
    public static class LogLevelSelfMode extends LEDSuiteSetting<String> {
        @Getter
        private static LogLevelSelfMode instance;

        public LogLevelSelfMode(Setting<Object> setting) {
            super(setting, String.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Color-Coding.Priority.Log-Level.Modes.All")
    public static class LogLevelAllMode extends LEDSuiteSetting<String> {
        @Getter
        private static LogLevelAllMode instance;

        public LogLevelAllMode(Setting<Object> setting) {
            super(setting, String.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Color-Coding.Priority.Log-Area.Modes.Self")
    public static class LogAreaSelfMode extends LEDSuiteSetting<String> {
        @Getter
        private static LogAreaSelfMode instance;

        public LogAreaSelfMode(Setting<Object> setting) {
            super(setting, String.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Color-Coding.Priority.Log-Area.Modes.All")
    public static class LogAreaAllMode extends LEDSuiteSetting<String> {
        @Getter
        private static LogAreaAllMode instance;

        public LogAreaAllMode(Setting<Object> setting) {
            super(setting, String.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Color-Coding.Priority.Trace.Modes.Self")
    public static class TraceSelfMode extends LEDSuiteSetting<String> {
        @Getter
        private static TraceSelfMode instance;

        public TraceSelfMode(Setting<Object> setting) {
            super(setting, String.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Color-Coding.Priority.Trace.Modes.All")
    public static class TraceAllMode extends LEDSuiteSetting<String> {
        @Getter
        private static TraceAllMode instance;

        public TraceAllMode(Setting<Object> setting) {
            super(setting, String.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Style")
    public static class LoggerStyle extends LEDSuiteSetting<String> {
        @Getter
        private static LoggerStyle instance;

        public LoggerStyle(Setting<Object> setting) {
            super(setting, String.class);
            instance = this;
        }
    }

    @YAMLSetting(path = "Logger.Trace.Style")
    public static class TraceStyle extends LEDSuiteSetting<String> {
        @Getter
        private static TraceStyle instance;

        public TraceStyle(Setting<Object> setting) {
            super(setting, String.class);
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

        public ShownAreas(Setting<Object> setting) {
            super(setting, (Class<List<String>>) (Class<?>) List.class);
            instance = this;
        }
    }
}
