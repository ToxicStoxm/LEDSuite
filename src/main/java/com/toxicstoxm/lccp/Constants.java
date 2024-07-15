package com.toxicstoxm.lccp;

import com.toxicstoxm.lccp.logging.Logger;
import com.toxicstoxm.lccp.task_scheduler.LCCPArgumentProcessor;

import java.util.HashMap;

// static utility class for storing paths
public final class Constants {
    // file paths
    public static final class File_System {
        private static String getAppDir() {
            String confHome = java.lang.System.getenv("XDG_CONFIG_HOME");
            return confHome == null ?  // Check if the config home (mainly for flatpak) contains anything
                    java.lang.System.getProperty("user.home") + "/.config/LED-Cube-Control-Panel/" : // If not it uses the java home with '.config/LED-Cube-Control-Panel/' appended as path
                    confHome + "/"; // else it gets the environment variable and appends / because if it's missing it will error but when there are two it will still work
        }

        private static String getTmpDir() {
            String cacheHome =  java.lang.System.getenv("XDG_CACHE_HOME");
            return cacheHome == null ? // Check if the cache home or just temp directory (mainly for flatpak) contains anything
                    java.lang.System.getProperty("java.io.tmpdir") + "/LED-Cube-Control-Panel/" : // If not it uses the java tmpdir with 'LED-Cube-Control-Panel/' appended as path
                    cacheHome + "/"; // If yes it gets the environment variable and appends / because if it is missing it will error but when there are two it will still work
        }

        private static String getDataDir() {
            String dataHome =  java.lang.System.getenv("XDG_DATA_HOME");
            return dataHome == null ? // Check if the data home directory (mainly for flatpak) contains anything
                    java.lang.System.getProperty("user.home") + "/.config/LED-Cube-Control-Panel/" : // If not it uses the java home with '.config/LED-Cube-Control-Panel/' appended as path
                    dataHome + "/"; // If yes it gets the environment variable and appends / because if it is missing it will error but when there are two it will still work
        }

        public static final String logFile = getTmpDir() + "latest.log";
        public static final String server_config = getAppDir() + "server_config.yaml";
        public static final String config = getAppDir() + "config.yaml";
    }
    // yaml paths for config
    public static final class Config {
        // standard separator used by the config
        public static final String SEPARATOR = ".";
        // config sections
        public static final String LOCAL_SETTINGS_SECTION = "Local-Settings";
        public static final String WINDOW_SECTION = LOCAL_SETTINGS_SECTION + SEPARATOR + "Window";
        public static final String LOGGING_SECTION = LOCAL_SETTINGS_SECTION + SEPARATOR + "Logging";
        public static final String NETWORK_SECTION = LOCAL_SETTINGS_SECTION + SEPARATOR + "Network";
        public static final String USER_PREFERENCES_SECTION = LOCAL_SETTINGS_SECTION + SEPARATOR + "User-Preferences";
        // settings
        public static final String WINDOW_TITLE = WINDOW_SECTION + SEPARATOR + "Window-Title";
        public static final String WINDOW_RESIZABLE = WINDOW_SECTION + SEPARATOR + "Window-Resizable";
        public static final String WINDOW_DEFAULT_WIDTH = WINDOW_SECTION + SEPARATOR + "Window-Default-Width";
        public static final String WINDOW_DEFAULT_HEIGHT = WINDOW_SECTION + SEPARATOR + "Window-Default-Height";
        public static final String LOG_LEVEL = LOGGING_SECTION + SEPARATOR + "Log-Level";
        public static final String SELECTION_DIR = USER_PREFERENCES_SECTION + SEPARATOR + "Default-Selection-Dir";
        public static final String DISPLAY_STATUS_BAR = USER_PREFERENCES_SECTION + SEPARATOR + "Display-Status-Bar";
        public static final String CHECK_IPV4 = NETWORK_SECTION + SEPARATOR + "Check-IPv4";
        public static final String NETWORK_COMMUNICATION_CLOCK_SPEED = NETWORK_SECTION + SEPARATOR + "Network-Communication-Clock-Speed";
        public static final String AUTO_PLAY_AFTER_UPLOAD = USER_PREFERENCES_SECTION + SEPARATOR + "Auto-Play-After-Upload";
    }
    // yaml paths for server_config
    public static final class Server_Config {
        public static final String BRIGHTNESS = "LED-Brightness";
        public static final String IPV4 = "Server-IP";
        public static final String PORT = "Server-Port";
    }
    // links used by the program
    public static final class Links {
        public static final String PROJECT_GITHUB = "https://github.com/ToxicStoxm/LED-Cube-Control-Panel/";
    }
    // placeholders used by the program
    public static final class Placeholders {
        public static final String VERSION = "%VERSION%";
    }
    public static final class Patterns {
        public static final String PORT = "^([1-9][0-9]{0,3}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$";
        public static final String IPV4 = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
    }
    public static final class Network {
        public static final class YAML {
            public static final String PACKET_TYPE = "packet_type";
            public static final String REQUEST_TYPE = "request_type";
            public static final String REQUEST_FILE = "request_file";
            public static final String KEEPALIVE = "keepalive";
            public static final String OBJECT_PATH = "object_path";
            public static final String OBJECT_NEW_VALUE = "object_new_value";
            public static final String REPLY_TYPE = "reply_type";
            public static final String FILE_IS_LOADED = "file_is_loaded";
            public static final String FILE_STATE = "file_state";
            public static final String FILE_SELECTED = "file_selected";
            public static final String CURRENT_DRAW = "current_draw";
            public static final String VOLTAGE = "voltage";
            public static final String LID_STATE = "lid_state";
            public static final String ERROR_SOURCE = "error_source";
            public static final String ERROR_CODE = "error_code";
            public static final String ERROR_NAME = "error_name";
            public static final String ERROR_SEVERITY = "error_severity";
            public static final String INTERNAL_NETWORK_ID = "internal_network_event_id";
            public static final String AVAILABLE_ANIMATIONS = "available_animations";
            public static final class MENU {
                public static final String GROUP_PREFIX = "group";
                public static final String GROUP_SUFFIX_WIDGET = "suffix";

                public static final String WIDGET_PREFIX = "widget";
                public static final String WIDGET_TYPE = "widget";
                public static final String WIDGET_STYLE = "style";
                public static final String WIDGET_LABEL = "label";
                public static final String WIDGET_TOOLTIP = "tooltip";
                public static final String WIDGET_CONTENT = "content";
                public static final String WIDGET_VALUE = "value";
                public static final String WIDGET_ICON = "icon";
                public static final String BUTTON_ROW = "row";

                public static final String SLIDER_MIN = "min";
                public static final String SLIDER_MAX = "max";
                public static final String SLIDER_STEP = "step";
                public static final String SLIDER_CLIMB_RATE = "climb_rate";
                public static final String SLIDER_DIGITS = "digits";
                public static final String SLIDER_NUMERIC = "numeric";
                public static final String SLIDER_SNAP = "snap";
                public static final String SLIDER_WRAPAROUND = "wraparound";

                public static final String ENTRY_APPLY_BUTTON = "apply_button";
                public static final String ENTRY_EDITABLE = "editable";

                public static final String EXPANDER_TOGGLEABLE = "toggleable";
                public static final String DROPDOWN_SEARCHABLE = "searchable";
                public static final String DROPDOWN_SELECTED = "selected";
                public static final String DROPDOWN = "dropdown";

                public static final String SPINNER_TIME = "time";
            }
        }
    }
    public static final class System {
        public static final String NAME = java.lang.System.getProperty("os.name");
        public static final String VERSION= java.lang.System.getProperty("os.version");
    }
    public static final class Application {
        public static final String NAME = "LED-Cube Control Panel";
        public static final String DOMAIN = "com.toxicstoxm.lccp";
        public static final String ICON = DOMAIN;
        public static final HashMap<String, LCCPArgumentProcessor> Arguments;
        public static final class _Arguments {
            public static final String LOG_LEVEL = "--log-level";
            public static final String SET_LOG_LEVEL = "--set-log-level";
        }


        static {
            Arguments = new HashMap<>();
            Arguments.put(
                    _Arguments.LOG_LEVEL, new LCCPArgumentProcessor() {
                        @Override
                        public void run(String argument, ArgumentValidationCallback callback) {
                            boolean callb = callback != null;
                            boolean result = false;
                            boolean snap = false;
                            int logLevel = 0;
                            try {
                                int temp = Integer.parseInt(argument);
                                int max = Logger.log_level.values().length;
                                int min = 0;
                                if (temp < min || temp > max) snap = true;
                                logLevel = Math.max(
                                        Math.min(
                                                temp,
                                                max
                                        ),
                                        min
                                );
                                LCCP.argumentsSettings.setLogLevel(logLevel);
                                result = true;
                            } catch (NumberFormatException _) {
                            } finally {
                                if (callb) callback.onValidationComplete(result,
                                        (
                                                result ?
                                                        "[" + _Arguments.LOG_LEVEL + " " + argument + "] Successfully set log level to '" + logLevel + "' for current session!" + (snap ? " (Value was automatically snapped to bounds, since '"+ argument +"' is out of bounds)" : "") :
                                                        "[" + _Arguments.LOG_LEVEL + "] Invalid log level '" + argument + "'!"
                                        )
                                );
                            }
                        }
                    }
            );

            Arguments.put(
                    _Arguments.SET_LOG_LEVEL, new LCCPArgumentProcessor() {
                        @Override
                        public void run(String argument, ArgumentValidationCallback callback) {
                            boolean callb = callback != null;
                            boolean result = false;
                            boolean snap = false;
                            int logLevel = 0;
                            try {
                                int temp = Integer.parseInt(argument);
                                int max = Logger.log_level.values().length;
                                int min = 0;
                                if (temp < min || temp > max) snap = true;
                                logLevel = Math.max(
                                        Math.min(
                                                temp,
                                                max
                                        ),
                                        min
                                );
                                LCCP.argumentsSettings.setLogLevel(logLevel);
                                LCCP.settings.setLogLevel(logLevel);
                                result = true;
                            } catch (NumberFormatException _) {
                            } finally {
                                if (callb) {
                                    callback.onValidationComplete(result,
                                            (
                                                    result ?
                                                            "[" + _Arguments.SET_LOG_LEVEL + " " + argument + "] Successfully changed log level to '" + logLevel + "'!" + (snap ? " (Value was automatically snapped to bounds, since '"+ argument +"' is out of bounds)" : "") :
                                                            "[" + _Arguments.SET_LOG_LEVEL + "] Invalid log level '" + argument + "'!"
                                            )
                                    );
                                }
                            }
                        }
                    }
            );
        }

    }
    public static final class GTK {
        public static final class Shortcuts {
            public static final class Keys {
                public static final String CONTROL = "<Control>";
                public static final String ALT = "<Alt>";
            }
            public static final class Symbols {
                public static final String QUESTIONMARK = "question";
            }
            public static final String STATUS = Keys.ALT + "s";
            public static final String SETTINGS = Keys.CONTROL + "s";
            public static final String SHORTCUTS = Keys.CONTROL + Symbols.QUESTIONMARK;
            public static final String ABOUT = Keys.ALT + "a";

        }
        public static final class Actions {
            public static final String ACTION_PREFIX = "action(";
            public static final String ACTION_SEPARATOR = ".";
            public static final String ACTION_SUFFIX = ")";
            public static final class Groups {
                public static final String MENU = "menu";
            }
            public static final class _Actions {
                public static final String STATUS  = "status";
                public static final String SETTINGS = "settings";
                public static final String SHORTCUTS = "shortcuts";
                public static final String ABOUT = "about";

                public static final String _STATUS  = Groups.MENU + ACTION_SEPARATOR + "status";
                public static final String _SETTINGS = Groups.MENU + ACTION_SEPARATOR + "settings";
                public static final String _SHORTCUTS = Groups.MENU + ACTION_SEPARATOR + "shortcuts";
                public static final String _ABOUT = Groups.MENU + ACTION_SEPARATOR + "about";

            }
            public static final String SHORTCUT_STATUS = ACTION_PREFIX + _Actions._STATUS + ACTION_SUFFIX;
            public static final String SHORTCUT_SETTINGS = ACTION_PREFIX + _Actions._SETTINGS + ACTION_SUFFIX;
            public static final String SHORTCUT_SHORTCUTS = ACTION_PREFIX + _Actions._SHORTCUTS + ACTION_SUFFIX;
            public static final String SHORTCUT_ABOUT = ACTION_PREFIX + _Actions._ABOUT + ACTION_SUFFIX;
        }
        public static final class Icons {
            public static final class Symbolic {
                public static final String SYMBOLIC = "symbolic";
                public static final String SEPARATOR = "-";
                public static final String SEARCH = "system-search" + SEPARATOR + SYMBOLIC;
                public static final String MENU = "open-menu" + SEPARATOR + SYMBOLIC;
                public static final String PLAY = "media-playback-start" + SEPARATOR + SYMBOLIC;
                public static final String STOP = "media-playback-stop" + SEPARATOR + SYMBOLIC;
                public static final String PAUSE = "media-playback-pause" + SEPARATOR + SYMBOLIC;
                public static final String DOCUMENT_SEND = "document-send" + SEPARATOR + SYMBOLIC;
                public static final String SIDEBAR_SHOW = "sidebar-show" + SEPARATOR + SYMBOLIC;
            }
        }
    }

    public static final class Messages {
        public static final class WARN {
            public static final String OPEN_GITHUB_ISSUE = "Please open an issue on the projects GitHub: " + Links.PROJECT_GITHUB;
        }
    }
}
