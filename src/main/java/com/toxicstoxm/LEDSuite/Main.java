package com.toxicstoxm.LEDSuite;

import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import org.gnome.gio.Resource;
import org.gnome.glib.Bytes;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * @since 1.0.0
 * @see LEDSuiteApplication
 * @author ToxicStoxm
 */
public class Main {
    public static void main(String[] args) throws Exception {

        // checks if the app directory already exists, if not tries to create it
        File appDirectory = new File(Constants.FileSystem.getAppDir());
        if (!appDirectory.isDirectory()) {
            System.out.println("App directory wasn't found, creating it...");
            if (!appDirectory.mkdirs()) {
                System.err.println("Failed to create app dir at: '" + appDirectory + "'!");
                return;
            }
            System.out.println("Successfully created app dir at: '" + appDirectory + "'!");
        }

        // tries to load translations, if none are found,
        // adds implementation which just returns the requested key as translated value
        ResourceBundle translationBundle;
        try {
            translationBundle = ResourceBundle.getBundle("translation/LEDSuite");
        } catch (MissingResourceException e) {
            System.err.println("Translations not found!");
            translationBundle = new ResourceBundle() {
                @Override
                protected Object handleGetObject(@NotNull String key) {
                    return key;
                }

                @Override
                public @NotNull Enumeration<String> getKeys() {
                    return new Enumeration<>() {
                        @Override
                        public boolean hasMoreElements() {
                            return false;
                        }

                        @Override
                        public String nextElement() {
                            return "";
                        }
                    };
                }
            };
        }

        // loads UI template files (.ui) and registers them using java-gi
        try (var stream = Main.class.getResourceAsStream("/LEDSuite.gresource")) {
            Objects.requireNonNull(stream);
            byte[] bytes = stream.readAllBytes();
            Resource resource = Resource.fromData(Bytes.static_(bytes));
            resource.resourcesRegister();
        }

        // creates new app instance and runs it
        var app = LEDSuiteApplication.create(translationBundle);
        try {
            app.run(args);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Please report this error: " + Constants.Application.ISSUES);
        }
    }
}
