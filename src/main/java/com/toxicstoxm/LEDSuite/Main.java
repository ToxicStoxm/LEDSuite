package com.toxicstoxm.LEDSuite;

import com.toxicstoxm.LEDSuite.gettext.Translations;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import org.gnome.gio.Resource;
import org.gnome.glib.Bytes;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @since 1.0.0
 * @see LEDSuiteApplication
 * @author ToxicStoxm
 */
public class Main {
    public static void main(String[] args) throws Exception {

        // Checks if the app directory already exists, if not tries to create it
        File appDirectory = new File(Constants.FileSystem.getAppDir());
        if (!appDirectory.isDirectory()) {
            System.out.println("App directory wasn't found, creating it...");
            if (!appDirectory.mkdirs()) {
                System.err.println("Failed to create app dir at: '" + appDirectory + "'!");
                return;
            }
            System.out.println("Successfully created app dir at: '" + appDirectory + "'!");
        }

        // Tries to extract a translationDirectory from CLI arguments
        List<String> argsList = new java.util.ArrayList<>(Arrays.stream(args).toList());
        String translationDirectory = null;
        if (argsList.contains("-t")) {
            int textDomainOption = argsList.indexOf("-t");
            translationDirectory = argsList.get(textDomainOption + 1);
            argsList.remove(textDomainOption);
            argsList.remove(textDomainOption);
        }

        // Inits translation implementation with APP_ID and a TextDomain if one was specified
        Translations.init(Constants.Application.ID, translationDirectory);

        // Loads UI template files (.ui) and registers them using java-gi
        try (var stream = Main.class.getResourceAsStream("/LEDSuite.gresource")) {
            Objects.requireNonNull(stream);
            byte[] bytes = stream.readAllBytes();
            Resource resource = Resource.fromData(Bytes.static_(bytes));
            resource.resourcesRegister();
        }

        // Creates new app instance and runs it
        var app = LEDSuiteApplication.create();
        try {
            app.run(argsList.toArray(new String[]{}));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Please report this error: " + Constants.Application.ISSUES);
        }
    }
}
