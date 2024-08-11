package com.toxicstoxm.LEDSuite.settings;

import com.toxicstoxm.LEDSuite.LEDSuite;

import java.io.*;
import java.util.*;

/**
 * The `CommentPreservation` class provides methods to extract and insert comments
 * in a text file while preserving their positions. This is particularly useful
 * for maintaining comments in configuration files.
 *
 * @since 1.0.0
 */
public final class CommentPreservation {

    /**
     * Extracts comments from the specified file. The comments and their line numbers
     * are returned in a `TreeMap` where the keys are the line numbers and the values
     * are the comment lines.
     *
     * @param filePath The path of the file from which to extract comments.
     * @return A `TreeMap` containing line numbers and their corresponding comment lines.
     * @throws NullPointerException if the file does not exist.
     * @since 1.0.0
     */
    public static TreeMap<Integer, String> extractComments(String filePath) {
        // Create a file object from the provided file path
        File f = new File(filePath);

        // Check if the file exists, throw an exception if it does not
        if (!f.exists()) throw new NullPointerException("File location not defined!");

        // Create a TreeMap to store the line numbers and comments
        TreeMap<Integer, String> result = new TreeMap<>();

        // Use try-with-resources to ensure the BufferedReader is closed after use
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)))) {
            String line;
            int lineNumber = 1;

            // Read the file line by line
            while ((line = br.readLine()) != null) {
                // Check if the line contains a comment
                if (line.contains("#")) {
                    // Store the line number and the comment in the TreeMap
                    result.put(lineNumber, line);
                }
                lineNumber++;
            }
        } catch (Exception e) {
            LEDSuite.logger.error("Failed to extract comments from YAML file! " + LEDSuite.logger.getErrorMessage(e));
        }

        // Return the TreeMap containing the comments and their line numbers
        return result;
    }

    /**
     * Inserts comments into the specified file at their respective line numbers.
     * The comments are given as a `TreeMap` where the keys are the line numbers
     * and the values are the comment lines.
     *
     * @param filePath The path of the file into which to insert comments.
     * @param comments A `TreeMap` containing line numbers and their corresponding comment lines.
     * @throws IOException if an I/O error occurs.
     * @throws NullPointerException if the file does not exist.
     * @since 1.0.0
     */
    public static void insertComments(String filePath, TreeMap<Integer, String> comments) throws IOException {
        // Create a file object from the provided file path
        File f = new File(filePath);

        // Check if the file exists, throw an exception if it does not
        if (!f.exists()) throw new NullPointerException("File location not defined!");

        // Create a list to store all lines from the file
        List<String> lines = new ArrayList<>();

        // Use try-with-resources to ensure the BufferedReader is closed after use
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)))) {
            String line;
            // Read the file line by line and store each line in the list
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }

        // Iterate over the entries in the TreeMap to insert comments
        for (SortedMap.Entry<Integer, String> entry : comments.entrySet()) {
            int lineNumber = entry.getKey() - 1; // Convert to 0-based index
            String comment = entry.getValue();

            // Check if the line number is within the range of the list
            if (lineNumber >= 0 && lineNumber < lines.size()) {
                // Insert the comment at the specified line number
                lines.add(lineNumber, comment);
            } else if (lineNumber >= lines.size()) {
                // If the line number is greater than the size of the list, add it at the end
                lines.add(comment);
            }
        }

        // Use try-with-resources to ensure the BufferedWriter is closed after use
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)))) {
            // Write the modified lines back to the file
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        }
    }
}
