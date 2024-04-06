package com.x_tornado10.Util;

// static utility class for storing paths
public class Paths {
    public static final String config = System.getProperty("user.home") + "/.config/LED-Cube-Control-Panel/config.yaml";
    public static class Config {
        public static final String DARK_MODE_ENABLED = "Dark-Mode-Enabled";
        public static final String DARK_MODE_COLOR_PRIMARY = "Dark-Mode-Color-Primary";
        public static final String DARK_MODE_COLOR_SECONDARY = "Dark-Mode-Color-Secondary";
        public static final String LIGHT_MODE_COLOR_PRIMARY = "Light-Mode-Color-Primary";
        public static final String LIGHT_MODE_COLOR_SECONDARY = "Light-Mode-Color-Secondary";
        public static final String WINDOW_TITLE = "Window-Title";
        public static final String WINDOW_RESIZABLE = "Window-Resizable";
        public static final String WINDOW_INITIAL_WIDTH = "Window-Initial-Width";
        public static final String WINDOW_INITIAL_HEIGHT = "Window-Initial-Height";
        public static final String WINDOW_SPAWN_CENTER = "Window-Spawn-Center";
        public static final String WINDOW_SPAWN_X = "Window-Spawn-X";
        public static final String WINDOW_SPAWN_Y = "Window-Spawn-Y";
        public static final String STARTUP_FAKE_LOADING_BAR = "Startup-Fake-Loading-Bar";
        public static final String WINDOW_FULL_SCREEN = "Window-Full-screen";
        public static final String WINDOWED_FULL_SCREEN = "Windowed-Full-screen";
        public static final String WINDOW_INITIAL_SCREEN = "Window-Initial-Screen";
        public static final String IPv4 = "Server-IP";
        public static final String Port = "Server-Port";
    }
    public static class Links {
        public static final String Project_GitHub = "https://github.com/ToxicStoxm/LED-Cube-Control-Panel";
    }
}
