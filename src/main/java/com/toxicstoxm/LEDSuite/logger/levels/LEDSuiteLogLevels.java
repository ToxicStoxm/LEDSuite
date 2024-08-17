package com.toxicstoxm.LEDSuite.logger.levels;

import java.awt.*;

public class LEDSuiteLogLevels {

    public static class Fatal extends LEDSuiteLogLevel {
        public Fatal(boolean enabled, String text, Color color) {
            super(enabled, text, color);
        }
    }

    public static class Error extends LEDSuiteLogLevel {
        public Error(boolean enabled, String text, Color color) {
            super(enabled, text, color);
        }
    }

    public static class Warn extends LEDSuiteLogLevel {
        public Warn(boolean enabled, String text, Color color) {
            super(enabled, text, color);
        }
    }

    public static class Info extends LEDSuiteLogLevel {
        public Info(boolean enabled, String text, Color color) {
            super(enabled, text, color);
        }
    }

    public static class Debug extends LEDSuiteLogLevel {
        public Debug(boolean enabled, String text, Color color) {
            super(enabled, text, color);
        }
    }

    public static class Verbose extends LEDSuiteLogLevel {
        public Verbose(boolean enabled, String text, Color color) {
            super(enabled, text, color);
        }
    }

    public static class Stacktrace extends LEDSuiteLogLevel {
        public Stacktrace(boolean enabled, String text, Color color) {
            super(enabled, text, color);
        }
    }
}
