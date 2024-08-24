package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.logger.colors.LEDSuiteMessage;
import com.toxicstoxm.LEDSuite.logger.areas.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogger;
import com.toxicstoxm.LEDSuite.logger.Logger;
import com.toxicstoxm.LEDSuite.logger.placeholders.LEDSuitePlaceholderManager;
import com.toxicstoxm.LEDSuite.logger.placeholders.PlaceholderManager;
import com.toxicstoxm.LEDSuite.network.LEDSuiteSocketComms;
import com.toxicstoxm.LEDSuite.settings.config.LEDSuiteSettingsBundle;
import com.toxicstoxm.LEDSuite.settings.config.LEDSuiteSettingsManager;
import io.github.jwharm.javagi.gobject.annotations.InstanceInit;
import io.github.jwharm.javagi.gtk.types.Types;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import lombok.SneakyThrows;
import org.glassfish.tyrus.client.ClientManager;
import org.gnome.adw.Application;
import org.gnome.gio.ApplicationFlags;
import org.gnome.gio.SimpleAction;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.Window;

import java.awt.*;
import java.lang.foreign.MemorySegment;
import java.net.URI;

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

    @SneakyThrows
    @InstanceInit
    public void init() {
        var quit = new SimpleAction("quit", null);
        var status = new SimpleAction("status", null);
        var settings = new SimpleAction("settings", null);
        var shortcuts = new SimpleAction("shortcuts", null);
        var about = new SimpleAction("about", null);

        quit.onActivate(_ -> {
            quit();
        });
        status.onActivate(_ -> {logger.debug("status");});
        settings.onActivate(_ -> {logger.fatal("settings");});
        shortcuts.onActivate(_ -> {logger.error("shortcuts");});
        about.onActivate(_ -> {logger.stacktrace("about");});

        addAction(quit);
        addAction(status);
        addAction(settings);
        addAction(shortcuts);
        addAction(about);

        setAccelsForAction("app.quit", new String[]{"<Control>q"});
        setAccelsForAction("app.status", new String[]{"<Alt>s"});
        setAccelsForAction("app.settings", new String[]{"<Control>comma"});
        setAccelsForAction("app.shortcuts", new String[]{"<Control>question"});
        setAccelsForAction("app.about", new String[]{"<Alt>a"});

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

        WebSocketContainer container = ClientManager.createClient();
        String uri = "wss://echo.websocket.org/";
        container.connectToServer(LEDSuiteSocketComms.class, URI.create(uri));

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
