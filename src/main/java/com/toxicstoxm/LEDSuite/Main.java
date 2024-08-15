package com.toxicstoxm.LEDSuite;

import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import org.gnome.gio.Resource;
import org.gnome.glib.Bytes;

import java.util.Objects;

public class Main {
    public static void main(String[] args) throws Exception {
        try (var stream = Main.class.getResourceAsStream("/LEDSuite.gresource"))  {
            Objects.requireNonNull(stream);
            byte[] bytes = stream.readAllBytes();
            Resource resource = Resource.fromData(Bytes.static_(bytes));
            resource.resourcesRegister();
        }

        var app = LEDSuiteApplication.create();
        app.run(args);
    }
}
