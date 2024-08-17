package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.logger.colors.LEDSuiteMessage;
import com.toxicstoxm.LEDSuite.logger.areas.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogger;
import com.toxicstoxm.LEDSuite.logger.Logger;
import com.toxicstoxm.LEDSuite.settings.yaml.InvalidConfigurationException;
import com.toxicstoxm.LEDSuite.settings.yaml.file.YamlConfiguration;
import io.github.jwharm.javagi.gobject.annotations.InstanceInit;
import io.github.jwharm.javagi.gtk.types.Types;
import org.gnome.adw.Application;
import org.gnome.gio.ApplicationFlags;
import org.gnome.gio.SimpleAction;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.Window;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.foreign.MemorySegment;

public class LEDSuiteApplication extends Application {

    private static final Type gtype = Types.register(LEDSuiteApplication.class);

    public static Type getType() {
        return gtype;
    }

    public LEDSuiteApplication(MemorySegment address) {
        super(address);
    }

    public static Logger logger;

    public static LEDSuiteApplication create() {
        return GObject.newInstance(getType(),
                "application-id", "com.toxicstoxm.LEDSuite",
                "flags", ApplicationFlags.DEFAULT_FLAGS);
    }

    @InstanceInit
    public void init() {
        var quit = new SimpleAction("quit", null);
        quit.onActivate(_ -> quit());
        addAction(quit);

        setAccelsForAction("app.quit", new String[]{"<control>q"});

        logger = new LEDSuiteLogger(new LEDSuiteLogAreas.General());

        logger.log(
                LEDSuiteMessage.builder()
                        .color(Color.BLUE)
                        .text("Hello ")
                        .color(Color.CYAN)
                        .text("World! :)")
                        .reset()
                        .text(" This should not be blue, if it is you fucked up!")
                        .build()
        );
        logger.log(
                new LEDSuiteMessage().colorMessage("Hello ", Color.CYAN) +
                        new LEDSuiteMessage().colorMessage("World! :)", Color.BLUE) +
                        " This should not be blue, if it is you fucked up!"
        );

        File f = new File(System.getProperty("user.home") + "/config.yaml");
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.load(f);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
        for (String entry : yaml.getKeys(true)) {
            logger.log(entry + ": " + yaml.get(entry));
        }

    }

    @Override
    public void activate() {
        Window win = this.getActiveWindow();
        if (win == null)
            win = LEDSuiteWindow.create(this);
        win.present();
    }
}
