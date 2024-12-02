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
        public static final String ID = "com.toxicstoxm." + NAME;
        public static final String WEBSITE = "https://github.com/ToxicStoxm/LEDSuite";
        public static final String ISSUES = WEBSITE + "/issues";
    }

    public static class UI {
        public static final class CSS {
            public static final String[] DISCONNECTED_CSS = new String[]{"error"};
            public static final String[] CONNECTED_CSS = new String[]{"success"};
            public static final String[] CHANGING_CSS = new String[]{"warning"};
        }
        public static final class Intervals {
            public static final long MINIMUM_DELAY = 500;
            public static final long RETRY_DELAY = 3000;
            public static final long CONNECTION_TIMEOUT = 10000;
        }
    }

    public static final class Communication {
        public static final class YAML {
            public static final class Keys {

                public static final class General {
                    public static final String PACKET_TYPE = "packet_type";
                    public static final String SUB_TYPE = "subtype";
                    public static final String FILE_NAME = "filename";
                }

                public static final class Error {
                    public static final class ServerError {
                        public static final String SOURCE = "error_source";
                        public static final String CODE = "error_code";
                        public static final String MESSAGE = "error_name";
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

                    public static final class StatusRequest {}

                    public static final class RenameRequest {
                        public static final String NEW_NAME = "new_name";
                    }

                    public static final class MenuChangeRequest{
                        public static final String OBJECT_ID = "object_id";
                        public static final String OBJECT_VALUE = "object_value";
                    }

                    public static final class FileUploadRequest {
                        public static final String FORCE_OVERWRITE = "force_overwrite";
                        public static final String UPLOAD_SESSION_ID = "upload_session_id";
                        public static final String SHA256 = "sha256";
                    }

                    public static final class SettingsRequest {}

                    public static final class SettingsResetRequest {}

                    public static final class SettingsChangeRequest {
                        public static final String BRIGHTNESS = "brightness";
                        public static final String SELECTED_COLOR_MODE = "selected_color_mode";
                        public static final String RESTORE_PREVIOUS_STATE_ON_BOOT = "restore_previous_state";
                    }

                    public static final class AuthenticationRequest {
                        public static final String USERNAME = "username";
                        public static final String PASSWORD_HASH = "password_hash";
                    }
                }

                public static final class Reply {
                    public static final class StatusReply {
                        public static final String FILE_STATE = "file_state";
                        public static final String SELECTED_FILE = "file_selected";
                        public static final String CURRENT_DRAW = "current_draw";
                        public static final String VOLTAGE = "voltage";
                        public static final String LID_STATE = "lid_state";
                        public static final String ANIMATIONS = "available_animations";
                        public static final String USERNAME = "username";

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
                        public static final String RESTORE_PREVIOUS_STATE_ON_BOOT = "restore_previous_state";
                    }

                    public static final class UploadFileCollisionReply {

                    }

                    public static final class UploadSuccessReply {

                    }

                    public static final class UploadReply {
                        public static final String UPLOAD_PERMITTED = "upload_permitted";
                    }

                    public static final class MenuReply {
                        public static final String CONTENT = "content";
                        public static final String LABEL = "label";
                        public static final String TOOLTIP = "tooltip";
                        public static final String SUBTITLE = "subtitle";
                        public static final String FILENAME = "filename";
                        public static final String TYPE = "type";
                        public static final String VALUE = "value";

                        public static final class Groups {
                            public static final String SUFFIX = "suffix";
                            public static final class Suffix {
                                public static final String ICON_NAME = "icon_name";
                            }
                        }

                        public static final class EntryRow {
                            public static final String EDITABLE = "editable";
                            public static final String APPLY_BUTTON = "apply_button";
                            public static final String ATTRIBUTE_STRING = "attribute_string";
                            public static final String UPDATE_COOLDOWN = "update_cooldown";
                        }

                        public static final class ButtonRow {
                            public static final String START_ICON_NAME = "start_icon_name";
                            public static final String END_ICON_NAME = "end_icon_name";
                        }

                        public static final class SpinRow {
                            public static final String DIGITS = "digits";
                            public static final String MINIMUM = "minimum";
                            public static final String MAXIMUM = "maximum";
                            public static final String INCREMENT = "increment";
                            public static final String CLIMB_RATE = "climb_rate";
                            public static final String PAGE_INCREMENT = "page_increment";
                            public static final String WRAP = "wrap";
                            public static final String SNAP = "snap";
                            public static final String UPDATE_COOLDOWN = "update_cooldown";
                        }

                        public static final class ComboRow {
                            public static final String ENABLE_SEARCH = "enable_search";
                        }

                        public static final class ExpanderRow {
                            public static final String WITH_SWITCH = "with_switch";
                            public static final String ENABLE_EXPANSION = "enable_expansion";
                        }

                        public static final class Button {
                            public static final String SPIN_ON_CLICKED = "spin_on_clicked";
                            public static final String BLOCK_AFTER_CLICKED = "block_after_clicked";
                            public static final String BLOCKING = "blocking";
                            public static final String SPINNING = "spinning";
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
                            public static final String UPLOAD_ERROR = "upload_error";
                            public static final String NOT_IMPLEMENTED_ERROR = "not_implemented_error";
                            public static final String SERIALIZATION_ERROR = "serialization_error";
                            public static final String HANDLING_ERROR = "handling_error";
                            public static final String SENDING_ERROR = "sending_error";
                            public static final String RECEIVING_ERROR = "receiving_error";
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
                        public static final String SETTINGS_RESET = "settings_reset";
                        public static final String ANIMATION_DELETE = "animation_delete";
                        public static final String AUTHENTICATE = "authenticate";
                        public static final String PASSWORD_CHANGE = "password_change";
                        public static final String CREATE_ACCOUNT = "create_account";
                        public static final String DELETE_ACCOUNT = "delete_account";
                    }
                }

                public static final class Reply {

                    public static final class Types {
                        public static final String STATUS = "status";
                        public static final String MENU = "menu";
                        public static final String UPLOAD = "upload";
                        public static final String UPLOAD_SUCCESS = "upload_success";
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
