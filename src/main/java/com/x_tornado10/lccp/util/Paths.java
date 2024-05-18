package com.x_tornado10.lccp.util;

// static utility class for storing paths
public class Paths {
    public static final String appDir = System.getProperty("user.home") + "/.config/LED-Cube-Control-Panel/";
    public static final String config = appDir + "/config.yaml";
    public static final String server_config = appDir + "/server_config.yaml";
    public static class Config {
        public static final String WINDOW_TITLE = "Window-Title";
        public static final String WINDOW_RESIZABLE = "Window-Resizable";
        public static final String WINDOW_INITIAL_WIDTH = "Window-Initial-Width";
        public static final String WINDOW_INITIAL_HEIGHT = "Window-Initial-Height";
        public static final String LOG_LEVEL = "Log-Level";
        public static final String SELECTION_DIR = "Default-Selection-Dir";
    }
    public static class Server_Config {
        public static final String BRIGHTNESS = "LED-Brightness";
        public static final String IPV4 = "Server-IP";
        public static final String PORT = "Server-Port";
    }
    public static class Links {
        public static final String Project_GitHub = "https://github.com/ToxicStoxm/LED-Cube-Control-Panel";
    }
    public static class Placeholders {
        public static final String VERSION = "%VERSION%";
    }
}
