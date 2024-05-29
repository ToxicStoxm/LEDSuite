package com.x_tornado10.lccp.util;

// static utility class for storing paths
public final class Paths {
    public static final String appDir = System.getProperty("user.home") + "/.config/LED-Cube-Control-Panel/";
    public static final String tmpDir = System.getProperty("java.io.tmpdir") + "/LED-Cube-Control-Panel/";
    public static final String config = appDir + "/config.yaml";
    public static final String server_config = appDir + "/server_config.yaml";
    public static final String logFile = appDir + "/latest.log";
    public static final class Config {
        public static final String SEPARATOR = ".";
        public static final String LOCAL_SETTINGS_SECTION = "Local-Settings";
        public static final String WINDOW_SECTION = LOCAL_SETTINGS_SECTION + SEPARATOR + "Window";
        public static final String LOGGING_SECTION = LOCAL_SETTINGS_SECTION + SEPARATOR + "Logging";
        public static final String NETWORK_SECTION = LOCAL_SETTINGS_SECTION + SEPARATOR + "Network";
        public static final String USER_PREFERENCES_SECTION = LOCAL_SETTINGS_SECTION + SEPARATOR + "User-Preferences";
        public static final String WINDOW_TITLE = WINDOW_SECTION + SEPARATOR + "Window-Title";
        public static final String WINDOW_RESIZABLE = WINDOW_SECTION + SEPARATOR + "Window-Resizable";
        public static final String WINDOW_DEFAULT_WIDTH = WINDOW_SECTION + SEPARATOR + "Window-Default-Width";
        public static final String WINDOW_DEFAULT_HEIGHT = WINDOW_SECTION + SEPARATOR + "Window-Default-Height";
        public static final String LOG_LEVEL = LOGGING_SECTION + SEPARATOR + "Log-Level";
        public static final String SELECTION_DIR = USER_PREFERENCES_SECTION + SEPARATOR + "Default-Selection-Dir";
        public static final String AUTO_UPDATE_REMOTE = NETWORK_SECTION + SEPARATOR + "Auto-Update-Remote";
        public static final String DISPLAY_STATUS_BAR = USER_PREFERENCES_SECTION + SEPARATOR + "Display-Status-Bar";
        public static final String AUTO_UPDATE_REMOTE_TICK = NETWORK_SECTION + SEPARATOR + "Auto-Update-Remote-Tick";
        public static final String CHECK_IPV4 = NETWORK_SECTION + SEPARATOR + "Check-IPv4";
    }
    public static final class Server_Config {
        public static final String BRIGHTNESS = "LED-Brightness";
        public static final String IPV4 = "Server-IP";
        public static final String PORT = "Server-Port";
    }

    public static final class Links {
        public static final String Project_GitHub = "https://github.com/ToxicStoxm/LED-Cube-Control-Panel";
    }
    public static final class Placeholders {
        public static final String VERSION = "%VERSION%";
    }
}
