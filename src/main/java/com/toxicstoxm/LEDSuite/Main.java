package com.toxicstoxm.LEDSuite;

import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import org.gnome.gio.Resource;
import org.gnome.glib.Bytes;

import java.util.Objects;

/**
 * @since 1.0.0
 * @see LEDSuiteApplication
 * @author ToxicStoxm
 */
public class Main {
    public static void main(String[] args) throws Exception {

        // loads UI template files (.ui) and registers them using java-gi
        try (var stream = Main.class.getResourceAsStream("/LEDSuite.gresource")) {
            Objects.requireNonNull(stream);
            byte[] bytes = stream.readAllBytes();
            Resource resource = Resource.fromData(Bytes.static_(bytes));
            resource.resourcesRegister();
        }

        // creates new app instance and runs it
        var app = LEDSuiteApplication.create();
        app.run(args);
    }
}
