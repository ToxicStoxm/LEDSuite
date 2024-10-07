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
            String cacheHome =  java.lang.System.getenv("XDG_CACHE_HOME");
            return cacheHome == null ? // Check if the cache home or just temp directory (mainly for flatpak) contains anything
                    java.lang.System.getProperty("java.io.tmpdir") + "/" + Application.NAME + "/" : // If not, it uses the java tmpdir with 'LED-Cube-Control-Panel/' appended as a path
                    cacheHome + "/"; // If yes, it gets the environment variable and appends / because, if it is missing, it will error, but when there are two it will still work
        }

        public static String getDataDir() {
            String dataHome =  java.lang.System.getenv("XDG_DATA_HOME");
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
}
