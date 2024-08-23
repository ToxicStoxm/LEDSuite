package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.logger.colors.LEDSuiteMessage;
import com.toxicstoxm.LEDSuite.logger.areas.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogger;
import com.toxicstoxm.LEDSuite.logger.Logger;
import com.toxicstoxm.LEDSuite.settings.config.LEDSuiteSettingsBundle;
import com.toxicstoxm.LEDSuite.settings.config.LEDSuiteSettingsManager;
import io.github.jwharm.javagi.gobject.SignalConnection;
import io.github.jwharm.javagi.gobject.annotations.InstanceInit;
import io.github.jwharm.javagi.gtk.types.Types;
import org.gnome.adw.Application;
import org.gnome.gio.ApplicationFlags;
import org.gnome.gio.SimpleAction;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.Window;

import java.awt.*;
import java.lang.foreign.MemorySegment;
import java.util.List;

public class LEDSuiteApplication extends Application {

    private static final Type gtype = Types.register(LEDSuiteApplication.class);

    public static Type getType() {
        return gtype;
    }

    public LEDSuiteApplication(MemorySegment address) {
        super(address);
    }

    public static Logger logger;

    public static LEDSuiteSettingsManager configMgr;

    public static LEDSuiteApplication create() {
        return GObject.newInstance(getType(),
                "application-id", "com.toxicstoxm.LEDSuite",
                "flags", ApplicationFlags.DEFAULT_FLAGS);
    }

    @InstanceInit
    public void init() {
        var quit = new SimpleAction("quit", null);
        quit.onActivate(_ -> {
            quit();
        });
        addAction(quit);

        setAccelsForAction("app.quit", new String[]{"<control>q"});

        this.onShutdown(this::exit);

        configMgr = new LEDSuiteSettingsManager(System.getProperty("user.home") + "/config.yaml");

        logger = new LEDSuiteLogger(System.out, new LEDSuiteLogAreas.General());

        if (false) {
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

            logger.log(LEDSuiteSettingsBundle.ShownAreas.getInstance().get().toString());
        }
        //LEDSuiteSettingsBundle.ShownAreas.getInstance().set(List.of("ALL", "NETWORK", "YAML_EVENTS", "COMMUNICATION", "UI", "USER_INTERFACE", "UI_CONSTRUCTION", "CUSTOM"));
        //LEDSuiteSettingsBundle.ShownAreas.getInstance().set(List.of("IF", "THIS", "APPEARS", "WE", "HAVE", "A", "FINAL", "SAVE", "IMPLEMENTATION", "CANDIDATE", "!"));
    }

    public void exit() {
        configMgr.save(System.getProperty("user.home") + "/config.yaml");
        System.exit(0);
    }

    @Override
    public void activate() {
        Window win = this.getActiveWindow();
        if (win == null)
            win = LEDSuiteWindow.create(this);
        win.present();
    }
}
