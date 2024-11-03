package com.toxicstoxm.LEDSuite.formatting;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Wrapper class for helpful string formatting methods.
 * Like extracting a file name from a path or formating date / time.
 * @since 1.0.0
 */
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

    public static @NotNull String formatDuration(long millis) {
        Duration duration = Duration.ofMillis(millis);

        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        StringBuilder formatted = new StringBuilder();

        // Only append non-zero units
        if (hours > 0) {
            formatted.append(hours).append("h ");
        }
        if (minutes > 0) {
            formatted.append(minutes).append("min ");
        }
        if (seconds > 0 || formatted.isEmpty()) { // Include seconds even if zero if no other units
            formatted.append(seconds).append("s");
        }

        return formatted.toString().trim(); // Remove trailing space if any
    }

    @Contract(pure = true)
    public static @NotNull String formatSpeed(long bytesPerSecond) {
        final String[] units = {"Bps", "KBps", "MBps", "GBps", "TBps"};
        int unitIndex = 0;
        double speed = bytesPerSecond;

        // Keep dividing by 1024 to move to the next unit
        while (speed >= 1024 && unitIndex < units.length - 1) {
            speed /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", speed, units[unitIndex]);
    }
}
