package com.toxicstoxm.LEDSuite.formatting;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Utility class that provides helpful string formatting methods, such as:
 * <ul>
 *     <li>Extracting a file name from a file path.</li>
 *     <li>Formatting date and time in various formats.</li>
 *     <li>Formatting durations into a human-readable format.</li>
 *     <li>Converting bytes per second into a readable speed format with appropriate units (e.g., Bps, KBps, MBps).</li>
 * </ul>
 *
 * <p>These methods are designed to simplify common string formatting tasks related to dates, file paths, durations,
 * and data speeds.</p>
 *
 * @since 1.0.0
 */
public final class StringFormatter {

    // Default format used for date and time if no format is provided
    private static final String defaultDateTimeFormat = "dd:MM:jj hh:mm:ss";

    /**
     * Extracts the file name from the given path using the default delimiter ("/").
     *
     * @param path The full file path as a string.
     * @return The file name extracted from the path, or the original path if no file name is found.
     */
    public static @NotNull String getFileNameFromPath(String path) {
        return getFileNameFromPath(path, "/");
    }

    /**
     * Extracts the file name from the given path using a specified delimiter.
     *
     * @param path The full file path as a string.
     * @param delimiter The delimiter used to split the path (e.g., "/" or "\\").
     * @return The file name extracted from the path, or the original path if no file name is found.
     */
    public static @NotNull String getFileNameFromPath(@NotNull String path, String delimiter) {
        String[] parts = path.strip().split(Pattern.quote(delimiter));
        if (parts.length == 0) return path;
        return parts[parts.length - 1].strip();
    }

    /**
     * Extracts the simple class name from a given class, excluding the package name.
     *
     * @param clazz The class from which to extract the simple name.
     * @return The simple class name (e.g., "StringFormatter" from "com.toxicstoxm.LEDSuite.formatting.StringFormatter").
     */
    public static @NotNull String getClassName(@NotNull Class<?> clazz) {
        return getFileNameFromPath(clazz.getName(), ".");
    }

    /**
     * Formats a {@link Date} object into a string using a custom date-time format.
     *
     * @param currentDate The {@link Date} object to be formatted.
     * @param format The date-time format to use (e.g., "dd/MM/yyyy HH:mm:ss").
     * @return The formatted date-time string.
     */
    public static @NotNull String formatDateTime(Date currentDate, String format) {
        return new SimpleDateFormat(format).format(currentDate);
    }

    /**
     * Formats the current date and time into a string using a custom date-time format.
     *
     * @param format The date-time format to use (e.g., "dd/MM/yyyy HH:mm:ss").
     * @return The formatted date-time string.
     */
    public static @NotNull String formatDateTime(String format) {
        return new SimpleDateFormat(format).format(new Date());
    }

    /**
     * Formats a {@link Date} object into a string using the default date-time format ("dd:MM:jj hh:mm:ss").
     *
     * @param currentDate The {@link Date} object to be formatted.
     * @return The formatted date-time string using the default format.
     */
    public static @NotNull String formatDateTime(Date currentDate) {
        return formatDateTime(currentDate, defaultDateTimeFormat);
    }

    /**
     * Formats the current date and time into a string using the default date-time format ("dd:MM:jj hh:mm:ss").
     *
     * @return The formatted current date-time string using the default format.
     */
    public static @NotNull String formatDateTime() {
        return formatDateTime(defaultDateTimeFormat);
    }

    /**
     * Formats a duration in milliseconds into a human-readable format (e.g., "1h 30min 45s").
     *
     * @param millis The duration in milliseconds.
     * @return The formatted duration string.
     */
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

    /**
     * Formats a speed (in bytes per second) into a human-readable format with the appropriate unit (e.g., Bps, KBps, MBps).
     *
     * @param bytesPerSecond The speed in bytes per second.
     * @return The formatted speed string with a suitable unit.
     */
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
