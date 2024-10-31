package com.toxicstoxm.LEDSuite;

import org.jetbrains.annotations.NotNull;

/**
 * Holds all constant values used by the application. E.g. YAML-Keys, paths, project-website,
 * @since 1.0.0
 */
public class Constants {

    public static class FileSystem {

        public static @NotNull String getAppDir() {
            String confHome = java.lang.System.getenv("XDG_CONFIG_HOME");
            return confHome == null ?  // Check if the config home (mainly for flatpak) contains anything
                    java.lang.System.getProperty("user.home") + "/.config/" + Application.NAME + "/" : // If not, it uses the java home with '.config/LED-Cube-Control-Panel/' appended as a path
                    confHome + "/"; // else it gets the environment variable and appends / because, if it's missing, it will error, but when there are two it will still work
        }

        public static @NotNull String getTmpDir() {
            String cacheHome = java.lang.System.getenv("XDG_CACHE_HOME");
            return cacheHome == null ? // Check if the cache home or just temp directory (mainly for flatpak) contains anything
                    java.lang.System.getProperty("java.io.tmpdir") + "/" + Application.NAME + "/" : // If not, it uses the java tmpdir with 'LED-Cube-Control-Panel/' appended as a path
                    cacheHome + "/"; // If yes, it gets the environment variable and appends / because, if it is missing, it will error, but when there are two it will still work
        }

        public static @NotNull String getDataDir() {
            String dataHome = java.lang.System.getenv("XDG_DATA_HOME");
            return dataHome == null ? // Check if the data home directory (mainly for flatpak) contains anything
                    java.lang.System.getProperty("user.home") + "/" + Application.NAME + "/" : // If not, it uses the java home with '.config/LED-Cube-Control-Panel/' appended as a path
                    dataHome + "/"; // If yes, it gets the environment variable and appends / because, if it is missing, it will error, but when there are two it will still work
        }

        public static final String CONFIG_FILE_NAME = "config.yaml";
        public static final String CONFIG_FILE_PATH = getAppDir() + "/" + CONFIG_FILE_NAME;

    }

    public static class Application {
        public static final String NAME = "LEDSuite";
        public static final String WEBSITE = "https://github.com/ToxicStoxm/LEDSuite";
        public static final String ISSUES = WEBSITE + "/issues";

    }

    public static class UI {
        public static final String NOT_AVAILABLE_VALUE = "N/A";
        public static final Double DEFAULT_OPACITY = 1.0;
        public static final Double REDUCED_OPACITY = 0.5;
        public static final class SettingsDialog {
            public static final String[] DISCONNECTED_CSS = new String[]{"error"};
            public static final String[] CONNECTED_CSS = new String[]{"success"};
            public static final String CONNECTED = "Connected";
            public static final String CONNECTED_TOOLTIP = "Click to disconnect";
            public static final String DISCONNECTED = "Disconnected";
            public static final String DISCONNECTED_TOOLTIP = "Click to connect";
            public static final String CONNECTING = "Connecting";
            public static final String DISCONNECTING = "Disconnecting";
            public static final String[] CHANGING_CSS = new String[]{"warning"};
            public static final long MINIMUM_DELAY = 500;
            public static final long CONNECTION_TIMEOUT = 10000;

        }
    }

    public static final class Communication {
        public static final class YAML {
            public static final class Keys {

                public static final class General {
                    public static final String PACKET_TYPE = "packet_type";
                    public static final String SUB_TYPE = "subtype";
                }

                public static final class Error {
                    public static final class ServerError {
                        public static final String SOURCE = "error_source";
                        public static final String CODE = "error_code";
                        public static final String NAME = "error_name";
                        public static final String SEVERITY = "error_severity";
                    }

                    public static final class MenuError {
                        public static final String FILE_NAME = "filename";
                        public static final String MESSAGE = "message";
                        public static final String SEVERITY = "severity";
                        public static final String CODE = "code";
                    }
                }

                public static final class Request {
                    public static final class General {
                        public static final String FILE_NAME = "filename";
                    }

                    public static final class StatusRequest {}

                    public static final class RenameRequest {
                        public static final String NEW_NAME = "new_name";
                    }

                    public static final class MenuChangeRequest{
                        public static final String FILE_NAME = "filename";
                        public static final String OBJECT_ID = "object_id";
                        public static final String OBJECT_VALUE = "object_value";
                    }

                    public static final class FileUploadRequest {
                        public static final String PACKET_COUNT = "packet_count";
                        public static final String UPLOAD_SESSION_ID = "upload_session_id";

                    }

                    public static final class SettingsRequest {}

                    public static final class SettingsChangeRequest {
                        public static final String BRIGHTNESS = "brightness";
                        public static final String SELECTED_COLOR_MODE = "selected_color_mode";
                    }
                }

                public static final class Reply {
                    public static final class StatusReply {
                        public static final String IS_FILE_LOADED = "file_is_loaded";
                        public static final String FILE_STATE = "file_state";
                        public static final String SELECTED_FILE = "file_selected";
                        public static final String CURRENT_DRAW = "current_draw";
                        public static final String VOLTAGE = "voltage";
                        public static final String LID_STATE = "lid_state";
                        public static final String ANIMATIONS = "available_animations";

                        public static final class AnimationList {
                            public static final String ICON = "icon";
                            public static final String LABEL = "label";
                            public static final String PAUSEABLE = "pause_able";
                        }
                    }

                    public static final class SettingsReply {
                        public static final String BRIGHTNESS = "brightness";
                        public static final String SELECTED_COLOR_MODE = "selected_color_mode";
                        public static final String AVAILABLE_COLOR_MODES = "available_color_modes";
                    }

                    public static final class UploadFileCollisionReply {
                        public static final String FILE_NAME = "filename";
                    }

                    public static final class UploadSuccessReply {
                        public static final String FILE_NAME = "file";
                    }

                    public static final class MenuReply {
                        public static final String CONTENT = "content";
                        public static final String LABEL = "label";
                        public static final String SUBTITLE = "subtitle";
                        public static final String FILENAME = "filename";
                        public static final String TYPE = "type";

                        public static final class Groups {
                            public static final String SUFFIX = "suffix";
                            public static final class Suffix {
                                public static final String ICON_NAME = "icon_name";
                            }
                        }

                        public static final class Property {
                            public static final String TEXT = "text";
                        }
                    }
                }
            }

            public static final class Values {
                public static final class General {
                    public static final class PacketTypes {
                        public static final String ERROR = "error";
                        public static final String REPLY = "reply";
                        public static final String REQUEST = "request";
                    }
                }

                public static final class Error {
                    public static class Types {
                        public static final String SERVER = "server";
                        public static final String MENU = "menu";
                    }

                    public static final class ServerError {
                        public static final class Sources {
                            public static final String POWER = "power";
                            public static final String INVALID_FILE = "invalid_file";
                            public static final String PARSING_ERROR = "parsing_error";
                            public static final String OTHER = "other";
                        }
                    }

                    public static final class MenuError {}

                    public static final String UNKNOWN_ERROR = "Unknown error";
                }

                public static final class Request {

                    public static final class Types {
                        public static final String PLAY = "play";
                        public static final String PAUSE = "pause";
                        public static final String STOP = "stop";
                        public static final String MENU = "menu";
                        public static final String MENU_CHANGE = "menu_change";
                        public static final String FILE_UPLOAD = "file_upload";
                        public static final String RENAME_REQUEST = "rename_request";
                        public static final String SETTINGS_CHANGE = "settings_change";
                        public static final String STATUS = "status";
                        public static final String SETTINGS = "settings";
                    }
                }

                public static final class Reply {

                    public static final class Types {
                        public static final String STATUS = "status";
                        public static final String MENU = "menu";
                        public static final String UPLOAD_SUCCESS = "upload_success";
                        public static final String UPLOAD_FILE_COLLISION_REPLY = "upload_file_collision_reply";
                        public static final String SETTINGS = "settings";
                    }

                    public static final class MenuReply {
                        public static final class WidgetTypes {
                            public static final String GROUP = "group";
                            public static final String BUTTON_ROW = "button_row";
                            public static final String BUTTON = "button";
                            public static final String ENTRY_ROW = "entry_row";
                            public static final String PROPERTY_ROW = "property_row";
                            public static final String COMBO_ROW = "combo_row";
                            public static final String SWITCH_ROW = "switch_row";
                            public static final String SPIN_ROW = "spin_row";
                            public static final String EXPANDER_ROW = "expander_row";
                        }

                        public static final class General {
                            public static final String LABEL = "label";
                            public static final String TEXT = "text";
                        }
                    }

                    public static final class StatusReply {
                        public static final class FileState {
                            public static final String PLAYING = "playing";
                            public static final String PAUSED = "paused";
                        }
                    }
                }
            }
        }

        public static final class ErrorCodes {
            public static final int FAILED_TO_PARSE_REQUEST_TYPE = 1;
        }

        public static final class WebSocketPaths {
            public static final String UPLOAD = "/upload";
            public static final String COMMUNICATION = "/communication";
            public static final String DEFAULT_SERVER_ROOT = "/";
        }

        public static final int DEFAULT_PORT = 80;
    }
}
