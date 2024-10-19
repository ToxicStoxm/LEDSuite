package com.toxicstoxm.LEDSuite;

public class Constants {

    public static class FileSystem {

        public static String getAppDir() {
            String confHome = java.lang.System.getenv("XDG_CONFIG_HOME");
            return confHome == null ?  // Check if the config home (mainly for flatpak) contains anything
                    java.lang.System.getProperty("user.home") + "/.config/" + Application.NAME + "/" : // If not, it uses the java home with '.config/LED-Cube-Control-Panel/' appended as a path
                    confHome + "/"; // else it gets the environment variable and appends / because, if it's missing, it will error, but when there are two it will still work
        }

        public static String getTmpDir() {
            String cacheHome = java.lang.System.getenv("XDG_CACHE_HOME");
            return cacheHome == null ? // Check if the cache home or just temp directory (mainly for flatpak) contains anything
                    java.lang.System.getProperty("java.io.tmpdir") + "/" + Application.NAME + "/" : // If not, it uses the java tmpdir with 'LED-Cube-Control-Panel/' appended as a path
                    cacheHome + "/"; // If yes, it gets the environment variable and appends / because, if it is missing, it will error, but when there are two it will still work
        }

        public static String getDataDir() {
            String dataHome = java.lang.System.getenv("XDG_DATA_HOME");
            return dataHome == null ? // Check if the data home directory (mainly for flatpak) contains anything
                    java.lang.System.getProperty("user.home") + "/" + Application.NAME + "/" : // If not, it uses the java home with '.config/LED-Cube-Control-Panel/' appended as a path
                    dataHome + "/"; // If yes, it gets the environment variable and appends / because, if it is missing, it will error, but when there are two it will still work
        }

        public static final String configFileName = "config.yaml";
        public static final String configFilePath = getAppDir() + "/" + configFileName;

    }

    public static class Application {
        public static final String NAME = "LEDSuite";
        public static final String WEBSITE = "https://github.com/ToxicStoxm/LEDSuite";
        public static final String ISSUES = WEBSITE + "/issues";
    }

    public static final class Communication {
        public static final class YAML {
            public static final class Keys {
                public static final class General {
                    public static final String InternalNetworkId = "internal_network_event_id";
                    public static final String PacketType = "packet_type";
                    public static final String ReplyType = "reply_type";
                }

                public static final class Error {
                    public static final String Source = "error_source";
                    public static final String Code = "error_code";
                    public static final String Name = "error_name";
                    public static final String Severity = "error_severity";
                }

                public static final class Status {
                    public static final String IsFileLoaded = "file_is_loaded";
                    public static final String FileState = "file_state";
                    public static final String SelectedFile = "file_selected";
                    public static final String CurrentDraw = "current_draw";
                    public static final String Voltage = "voltage";
                    public static final String LidState = "lid_state";
                    public static final String Animations = "available_animations";
                }

                public static final class Request {
                    public static final String Type = "type";
                    public static final String File = "file";
                    public static final String ObjectPath = "object_path";
                    public static final String ObjectValue = "object_new_value";
                    public static final String FileSize = ObjectValue;
                }

                public static final class Menu {

                }
            }

            public static final class Values {
                public static final class PacketTypes {
                    public static final String Error = "error";
                    public static final String Reply = "reply";
                    public static final String Request = "request";
                }

                public static final class ReplyTypes {
                    public static final String Status = "status";
                    public static final String Menu = "menu";
                }

                public static final class Error {
                    public static final class Sources {
                        public static final String Power = "power";
                        public static final String InvalidFile = "invalid_file";
                        public static final String ParsingError = "parsing_error";
                        public static final String Other = "other";
                    }
                }

                public static final class Status {
                    public static final class FileState {
                        public static final String Playing = "playing";
                        public static final String Paused = "paused";
                    }
                }

                public static final class RequestTypes {
                    public static final String Status = "status";
                    public static final String Play = "play";
                    public static final String Pause = "pause";
                    public static final String Stop = "stop";
                    public static final String Menu = "menu";
                    public static final String MenuChange = "menu_change";
                    public static final String FileUpload = "file_upload";
                    public static final String KeepAlive = "keepalive";
                }
            }
        }

        public static final class ErrorCodes {
            public static final int FailedToParseRequestType = 1;
        }
    }
}
