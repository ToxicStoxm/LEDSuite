package com.toxicstoxm.LEDSuite.logging.network;

import com.toxicstoxm.LEDSuite.LEDSuite;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * The `NetworkLogger` class is responsible for logging network events within the LEDSuite application.
 * It stores events with unique identifiers and provides methods for adding, aligning, and printing these events.
 *
 * <p>This class maintains a mapping of events to their unique identifiers and their order of addition.
 * The events are logged with timestamps and can be aligned for better readability.
 *
 * @since 1.0.0
 */
public class NetworkLogger {

    /**
     * A map of network events keyed by their unique identifiers.
     *
     * @since 1.0.0
     */
    private final HashMap<UUID, String> networkEvents;

    /**
     * A map of the order in which events were added, keyed by their index.
     *
     * @since 1.0.0
     */
    private final TreeMap<Integer, UUID> order;

    /**
     * Constructs a new `NetworkLogger` instance.
     * Initializes the data structures for storing network events and their order.
     *
     * @since 1.0.0
     */
    public NetworkLogger() {
        networkEvents = new HashMap<>();
        order = new TreeMap<>();
    }

    /**
     * Adds a new network event with a specified UUID and description.
     * If the event is added successfully, it is also added to the order map.
     *
     * @param id The UUID of the event.
     * @param description The description of the event.
     * @return The UUID of the added event.
     * @since 1.0.0
     */
    public UUID addEvent(UUID id, String description) {
        // Add the event description to the map if it's not already present
        if (networkEvents.putIfAbsent(id, description) == null) {
            // Add the event to the order map
            order.put(order.size(), id);
        }
        return id;
    }

    /**
     * Prints all network events to the LEDSuite logger.
     * The events are aligned for better readability.
     *
     * @since 1.0.0
     */
    public void printEvents() {
        alignNetworkEvents(); // Align event descriptions
        boolean empty = networkEvents.isEmpty() && order.isEmpty();
        LEDSuite.logger.verbose("-------------------- Network Events ---------------------------------------------------------------------------------------------------------");
        LEDSuite.logger.verbose(empty ? "Couldn't find any network events!" : "Network event count: " + networkEvents.size());
        for (Map.Entry<Integer, UUID> entry : order.entrySet()) {
            // Print each event with its description and UUID
            LEDSuite.logger.verbose(networkEvents.get(entry.getValue()) + " " + entry.getValue());
        }
        LEDSuite.logger.verbose("----------------------------------------------------------------------------------------------------------------------------------------------");
    }

    /**
     * Generates a random UUID, attaches the current time to the description,
     * and adds the event with the generated UUID.
     *
     * @param description The description of the event.
     * @return The generated UUID of the added event.
     * @since 1.0.0
     */
    public UUID getRandomUUID(String description) {
        // Add a new event with a randomly generated UUID and a timestamped description
        return addEvent(UUID.randomUUID(), attachTime(description));
    }

    /**
     * Attaches the current time to the given message.
     * The time is formatted as HH:mm:ss and prefixed to the message.
     *
     * @param message The message to which the time will be attached.
     * @return The message with the current time attached.
     * @since 1.0.0
     */
    private String attachTime(String message) {
        // Format the current time
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        // Return the message with the timestamp
        return "[" + df.format(new Date()) + "] " + message + "[ID]";
    }

    /**
     * Aligns the network event descriptions for better readability when printed.
     * Extracts and aligns descriptions based on column widths.
     *
     * @since 1.0.0
     */
    public void alignNetworkEvents() {
        // Extract the event descriptions
        List<String> strings = new ArrayList<>(networkEvents.values());
        // Align the descriptions
        List<String> alignedStrings = alignStrings(strings);

        // Update the networkEvents map with the aligned strings
        int index = 0;
        for (UUID key : networkEvents.keySet()) {
            networkEvents.put(key, alignedStrings.get(index++));
        }
    }

    /**
     * Splits each string by square brackets and determines the maximum length of each column for alignment.
     *
     * @param strings The list of strings to align.
     * @return The list of aligned strings.
     * @since 1.0.0
     */
    private List<String> alignStrings(List<String> strings) {
        // List to hold the split parts of each string
        List<String[]> splitStrings = new ArrayList<>();
        // Array to hold the maximum length of each column
        int[] maxLengths = new int[0];

        for (String s : strings) {
            // Split each string by square brackets
            String[] parts = s.split("(?<=])(?=\\[)");
            splitStrings.add(parts);
            if (parts.length > maxLengths.length) {
                // Update maxLengths if necessary
                maxLengths = new int[parts.length];
            }
            for (int i = 0; i < parts.length; i++) {
                // Determine the maximum length for each column
                if (parts[i].length() > maxLengths[i]) {
                    maxLengths[i] = parts[i].length();
                }
            }
        }

        // Construct the aligned strings based on column widths
        return getStrings(splitStrings, maxLengths);
    }

    /**
     * Constructs the aligned strings based on the maximum column lengths.
     *
     * @param splitStrings The list of split strings.
     * @param maxLengths The maximum lengths of each column.
     * @return The list of aligned strings.
     * @since 1.0.0
     */
    private List<String> getStrings(List<String[]> splitStrings, int[] maxLengths) {
        List<String> alignedStrings = new ArrayList<>();
        for (String[] parts : splitStrings) {
            StringBuilder alignedString = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                // Format each part to align with column widths
                alignedString.append(String.format("%-" + maxLengths[i] + "s", parts[i]));
                if (i < parts.length - 1) {
                    alignedString.append(" ");
                }
            }
            alignedStrings.add(alignedString.toString());
        }
        return alignedStrings;
    }
}
