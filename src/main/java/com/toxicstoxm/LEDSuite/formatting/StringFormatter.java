package com.toxicstoxm.LEDSuite.formatting;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public final class StringFormatter {

    private static final String defaultDateTimeFormat = "dd:MM:jj hh:mm:ss";

    public static @NotNull String getFileNameFromPath(String path) {
        return getFileNameFromPath(path, "/");
    }

    public static @NotNull String getFileNameFromPath(@NotNull String path, String delimiter) {
        String[] parts = path.strip().split(Pattern.quote(delimiter));
        if (parts.length == 0) return path;
        return parts[parts.length - 1].strip();
    }

    public static @NotNull String getClassName(@NotNull Class<?> clazz) {
        return getFileNameFromPath(clazz.getName(), ".");
    }

    public static @NotNull String formatDateTime(Date currentDate, String format) {
        return new SimpleDateFormat(format).format(currentDate);
    }

    public static @NotNull String formatDateTime(String format) {
        return new SimpleDateFormat(format).format(new Date());
    }

    public static @NotNull String formatDateTime(Date currentDate) {
        return formatDateTime(currentDate, defaultDateTimeFormat);
    }

    public static @NotNull String formatDateTime() {
        return formatDateTime(defaultDateTimeFormat);
    }
}
