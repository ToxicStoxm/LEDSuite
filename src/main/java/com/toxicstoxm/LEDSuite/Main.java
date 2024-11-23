package com.toxicstoxm.LEDSuite;

import com.toxicstoxm.LEDSuite.gettext.Translations;
import com.toxicstoxm.LEDSuite.tools.ExceptionTools;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import org.gnome.gio.Resource;
import org.gnome.glib.Bytes;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Main entry point for the LEDSuite application.
 * This class is responsible for initializing the necessary resources, setting up translations,
 * managing the application's directory structure, and running the application.
 *
 * @since 1.0.0
 * @see LEDSuiteApplication
 * @author ToxicStoxm
 */
public class Main {

    /**
     * The main method of the LEDSuite application. It is responsible for:
     * 1. Ensuring the application's directory exists.
     * 2. Parsing command-line arguments for a translation directory.
     * 3. Initializing the translation system.
     * 4. Loading UI resources.
     * 5. Running the application.
     *
     * @param args Command-line arguments passed to the application.
     */
    public static void main(String[] args) {

        // Ensure the app's directory exists, creating it if necessary
        File appDirectory = new File(Constants.FileSystem.getAppDir());
        if (!appDirectory.isDirectory()) {
            System.out.println("App directory wasn't found, creating it...");
            if (!appDirectory.mkdirs()) {
                System.err.println("Failed to create app dir at: '" + appDirectory + "'!");
                return;
            }
            System.out.println("Successfully created app dir at: '" + appDirectory + "'!");
        }

        // Attempt to extract a translation directory from the command-line arguments
        List<String> argsList = new java.util.ArrayList<>(Arrays.stream(args).toList());
        String translationDirectory = null;

        // Check if the translation directory argument exists (flag -t)
        if (argsList.contains("-t")) {
            int textDomainOption = argsList.indexOf("-t");
            translationDirectory = argsList.get(textDomainOption + 1);
            argsList.remove(textDomainOption);
            argsList.remove(textDomainOption);
        }

        // Initialize translations with the specified text domain (if provided)
        Translations.init(Constants.Application.ID, translationDirectory);

        System.out.println("Bound test domain '" + Constants.Application.ID + "' to '" + translationDirectory + "'!");

        // Load UI templates from a bundled resource (LEDSuite.gresource) and register them using Java-GI
        try (var stream = Main.class.getResourceAsStream("/LEDSuite.gresource")) {
            Objects.requireNonNull(stream, "Resource stream is null, failed to load gresource.");
            byte[] bytes = stream.readAllBytes();
            Resource resource = Resource.fromData(Bytes.static_(bytes));
            resource.resourcesRegister();
        } catch (Exception e) {
            // If the gresource cannot be found, or an error occurred during loading or registering it,
            // print an error and provide a bug report link
            ExceptionTools.printStackTrace(e, System.err::println);
            System.err.println("You can report this error at: " + Constants.Application.ISSUES);
        }

        // Create and run the LEDSuite application instance
        var app = LEDSuiteApplication.create();
        try {
            // Run the application, passing in the modified list of arguments (after parsing translation directory)
            app.run(argsList.toArray(new String[0]));
        } catch (Exception e) {
            // If the application fails, print the error and provide a bug report link
            ExceptionTools.printStackTrace(e, System.err::println);
            System.err.println("You can report this error at: " + Constants.Application.ISSUES);
        }
    }
}
