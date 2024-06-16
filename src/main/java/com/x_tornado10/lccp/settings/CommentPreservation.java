package com.x_tornado10.lccp.settings;

import com.x_tornado10.lccp.LCCP;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CommentPreservation {
    public static HashMap<Integer, String> extractComments(String filePath) {
        File f = new File(filePath);
        if (!f.exists()) throw new NullPointerException("File location not defined!");
        HashMap<Integer, String> result = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)))) {
            String line;
            int lineNumber = 1;

            while ((line = br.readLine()) != null) {
                if (line.contains("#")) {
                    result.put(lineNumber, line);
                }
                lineNumber++;
            }
        } catch (Exception e) {
            for (StackTraceElement s : e.getStackTrace()) {
                LCCP.logger.error(s.toString());
            }
        }

        return result;
    }

    public static void insertComments(String filePath, HashMap<Integer, String> comments) throws IOException {
        File f = new File(filePath);
        if (!f.exists()) throw new NullPointerException("File location not defined!");

        // Read all lines from the file
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }

        // Insert comments
        for (Map.Entry<Integer, String> entry : comments.entrySet()) {
            int lineNumber = entry.getKey() - 1; // convert to 0-based index
            String comment = entry.getValue();

            if (lineNumber >= 0 && lineNumber < lines.size()) {
                lines.add(lineNumber, comment); // Insert the comment at the specified line number
            } else if (lineNumber >= lines.size()) {
                lines.add(comment); // If the line number is greater than the size of the list, add it at the end
            }
        }

        // Write the modified lines back to the file
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        }
    }
}
