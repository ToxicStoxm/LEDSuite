package com.x_tornado10.Utilities;

public final class Networking {
    // validate an IP4 address
    public static boolean isValidIP(final String ip) {
        String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";

        return ip.matches(PATTERN);
    }
    // validate Port number
    public static boolean isValidPORT(final String port) {
        String PATTERN = "^([1-9][0-9]{0,3}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$";

        return port.matches(PATTERN);
    }
}
