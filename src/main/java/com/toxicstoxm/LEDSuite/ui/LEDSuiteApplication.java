package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.WebSocketClient;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.communication.WebSocketClientEndpoint;
import com.toxicstoxm.LEDSuite.settings.LEDSuiteSettingsBundle;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteScheduler;
import com.toxicstoxm.LEDSuite.time.CooldownManger;
import com.toxicstoxm.LEDSuite.time.TickingSystem;
import com.toxicstoxm.YAJL.YAJLLogger;
import com.toxicstoxm.YAJL.colors.YAJLMessage;
import com.toxicstoxm.YAJL.levels.YAJLLogLevels;
import com.toxicstoxm.YAJSI.api.settings.YAJSISettingsManager;
import io.github.jwharm.javagi.gobject.annotations.InstanceInit;
import io.github.jwharm.javagi.gtk.types.Types;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.Session;
import lombok.Getter;
import org.glassfish.tyrus.client.ClientManager;
import org.gnome.adw.Application;
import org.gnome.gio.ApplicationFlags;
import org.gnome.gio.SimpleAction;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.Window;

import java.awt.*;
import java.io.IOException;
import java.lang.foreign.MemorySegment;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

import static com.toxicstoxm.LEDSuite.settings.LEDSuiteSettingsBundle.*;

public class LEDSuiteApplication extends Application {

    private static final Type gtype = Types.register(LEDSuiteApplication.class);

    public static Type getType() {
        return gtype;
    }

    public LEDSuiteApplication(MemorySegment address) {
        super(address);
    }

    public static String version = "@version@";

    @Getter
    private static YAJLLogger logger;

    @Getter
    private static YAJSISettingsManager configMgr;

    @Getter
    private LEDSuiteWindow window;

    @Getter
    private static LEDSuiteScheduler scheduler;

    @Getter
    private static TickingSystem tickingSystem;

    @Getter
    private static WebSocketClient webSocketClient;

    public static LEDSuiteApplication create() {
        return GObject.newInstance(getType(),
                "application-id", "com.toxicstoxm.LEDSuite",
                "flags", ApplicationFlags.DEFAULT_FLAGS);
    }

    @InstanceInit
    public void init() {

        var quit = new SimpleAction("quit", null);
        var status = new SimpleAction("status", null);
        var settings = new SimpleAction("settings", null);
        var shortcuts = new SimpleAction("shortcuts", null);
        var about = new SimpleAction("about", null);
        var sidebarToggle = new SimpleAction("sidebar_toggle", null);
        var sidebarFileManagementUploadPage = new SimpleAction("sidebar_file_management_upload_page", null);


        quit.onActivate(_ -> quit());
        status.onActivate(_ -> window.displayStatusDialog());
        settings.onActivate(_ -> window.displayPreferencesDialog());
        shortcuts.onActivate(_ -> window.displayShortcutsWindow());
        about.onActivate(_ -> window.displayAboutDialog());
        sidebarToggle.onActivate(_ -> window.toggle_sidebar());

        CooldownManger.addAction("sidebarFileManagementUploadPage", () -> window.uploadPageSelect(), 500);
        sidebarFileManagementUploadPage.onActivate(_ -> {
            if (!CooldownManger.call("sidebarFileManagementUploadPage")) logger.info("Action on cooldown!");
        });

        addAction(quit);
        addAction(status);
        addAction(settings);
        addAction(shortcuts);
        addAction(about);
        addAction(sidebarToggle);
        addAction(sidebarFileManagementUploadPage);

        setAccelsForAction("app.quit", new String[]{"<Control>q"});
        setAccelsForAction("app.status", new String[]{"<Alt>s"});
        setAccelsForAction("app.settings", new String[]{"<Control>comma"});
        setAccelsForAction("app.shortcuts", new String[]{"<Control>question"});
        setAccelsForAction("app.about", new String[]{"<Alt>a"});
        setAccelsForAction("app.sidebar_toggle", new String[]{"F9"});

        this.onShutdown(this::exit);

        configMgr = YAJSISettingsManager.builder()
                .buildWithConfigFile(
                        new YAJSISettingsManager.ConfigFile(
                                Constants.FileSystem.configFilePath,
                                getClass().getClassLoader().getResource("config.yaml")
                        ),
                        LEDSuiteSettingsBundle.class
                );

        logger = YAJLLogger.builder()
                // share settingsManager with the logger to use the same settings log implementation
                .setSettingsManager(configMgr)
                .buildWithArea(
                        Constants.FileSystem.getAppDir(),
                        System.out,
                        new LEDSuiteLogAreas.GENERAL(),
                        new LEDSuiteLogAreas(),
                        true
                )
                // configure how the log should be displayed
                .configureYajsiLog(
                        PrintLoggerTestMessages.getInstance().get(),
                        new YAJLLogger.LogMeta(
                                new YAJLLogLevels.Info(),
                                new LEDSuiteLogAreas.YAML_EVENTS()
                        )
                );

        scheduler = new LEDSuiteScheduler();
        tickingSystem = new TickingSystem();

        if (PrintLoggerTestMessages.getInstance().get()) {
            logger.log(
                    YAJLMessage.builder()
                            .color(Color.BLUE)
                            .text("Hello ")
                            .color(Color.CYAN)
                            .text("World! :)")
                            .reset()
                            .text(" This should not be blue, if it is you fucked up!")
                            .build()
            );
            logger.log(
                    new YAJLMessage().colorMessage("Hello ", Color.CYAN) +
                            new YAJLMessage().colorMessage("World! :)", Color.BLUE) +
                            " This should not be blue, if it is you fucked up!"
            );

            for (int i = 250; i > 6; i--) {
                logger.fatal(YAJLMessage.builder().colorMessage(String.valueOf(UUID.randomUUID()), new Color(i, i + 5, i - 5)));
            }

            logger.fatal("Some general fatal occurred!", new LEDSuiteLogAreas.GENERAL());
            logger.error("Some ui error occurred!", new LEDSuiteLogAreas.UI());
            logger.warn("Some network warn occurred!", new LEDSuiteLogAreas.NETWORK());
            logger.info("Some yaml events info occurred!", new LEDSuiteLogAreas.YAML_EVENTS());
            logger.debug("Some communication debug occurred!", new LEDSuiteLogAreas.COMMUNICATION());
            logger.verbose("Some ui construction verbose occurred!", new LEDSuiteLogAreas.UI_CONSTRUCTION());
            logger.stacktrace("Some user interactions stacktrace occurred!", new LEDSuiteLogAreas.USER_INTERACTIONS());
            logger.debug("newline \n support \n test \n\n\n newline \n support \n test\n", new LEDSuiteLogAreas.UI_CONSTRUCTION());

        }
    }

    public void exit() {
        logger.saveSettings();
        configMgr.save();
        System.exit(0);
    }

    @Override
    public void activate() {
        Window win = this.getActiveWindow();
        if (win == null)
            win = LEDSuiteWindow.create(this);
        window = (LEDSuiteWindow) win;
        win.present();

        URI serverAddress;
        try {
            serverAddress = new URI(WebsocketURI.getInstance().get());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        webSocketClient = new WebSocketClient(WebSocketClientEndpoint.class, serverAddress);
    }
}
