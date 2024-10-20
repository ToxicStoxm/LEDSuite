package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.PacketManager;
import com.toxicstoxm.LEDSuite.communication.packet_management.PacketReceivedHandler;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.StatusReplyPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.StatusRequestPacket;
import com.toxicstoxm.LEDSuite.communication.websocket.WebSocketClient;
import com.toxicstoxm.LEDSuite.communication.websocket.WebSocketCommunication;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.settings.LEDSuiteSettingsBundle;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteScheduler;
import com.toxicstoxm.LEDSuite.time.CooldownManger;
import com.toxicstoxm.LEDSuite.time.TickingSystem;
import com.toxicstoxm.YAJL.YAJLLogger;
import com.toxicstoxm.YAJL.levels.YAJLLogLevels;
import com.toxicstoxm.YAJSI.api.settings.YAJSISettingsManager;
import io.github.jwharm.javagi.gobject.annotations.InstanceInit;
import io.github.jwharm.javagi.gtk.types.Types;
import lombok.Getter;
import org.gnome.adw.Application;
import org.gnome.gio.ApplicationFlags;
import org.gnome.gio.SimpleAction;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.Window;

import java.lang.foreign.MemorySegment;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static com.toxicstoxm.LEDSuite.settings.LEDSuiteSettingsBundle.*;

/**
 * Main application class. Initializes and starts {@link LEDSuiteWindow} and other vital components like: <br>
 * {@link YAJLLogger} {@link LEDSuiteScheduler} {@link YAJSISettingsManager}
 * @since 1.0.0
 */
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
    private static WebSocketClient webSocketCommunication;

    @Getter
    private static PacketManager packetManager;

    @Getter
    private static PacketReceivedHandler packetReceivedHandler;

    /**
     * Creates a new LEDSuiteApplication object with app-id and default flags
     * @return the newly created LEDSuiteApplication instance
     */
    public static LEDSuiteApplication create() {
        return GObject.newInstance(getType(),
                "application-id", "com.toxicstoxm.LEDSuite",
                "flags", ApplicationFlags.DEFAULT_FLAGS);
    }

    /**
     * Creates and registers necessary UI actions. Creates and starts the logger, settings manager and task scheduler.
     */
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
                                Constants.FileSystem.CONFIG_FILE_PATH,
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
                        EnableSettingsLogging.getInstance().get(),
                        new YAJLLogger.LogMeta(
                                new YAJLLogLevels.Info(),
                                new LEDSuiteLogAreas.YAML()
                        )
                );

        scheduler = new LEDSuiteScheduler();
        tickingSystem = new TickingSystem();

        packetManager = new PacketManager(CommunicationPacket.class);
        packetReceivedHandler = new PacketReceivedHandler();
        registerPackets();

        startWebsocket();
    }

    /**
     * Creates a new websocket client instance and connects it with the websocket server.
     */
    private void startWebsocket() {
        URI serverAddress;
        try {
            serverAddress = new URI(WebsocketURI.getInstance().get());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        webSocketCommunication = new WebSocketClient(WebSocketCommunication.class, serverAddress);

        new LEDSuiteRunnable() {
            @Override
            public void run() {
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    webSocketCommunication.enqueueMessage(scanner.nextLine().replace("\\n", "\n"));
                }
            }
        }.runTaskAsynchronously();
    }

    /**
     * Registers all supported network packets in {@link PacketManager}.
     * @see PacketManager
     * @see Packet
     */
    private void registerPackets() {

        StatusRequestPacket requestPacket = StatusRequestPacket.builder().build();

        packetManager.registerPacket(requestPacket);

        StatusReplyPacket statusReplyPacket = StatusReplyPacket.builder().build();

        packetManager.registerPacket(statusReplyPacket);

    }

    /**
     * Called when application window is closed. <br>
     * Saves all settings and exits.
     */
    public void exit() {
        configMgr.save();
        System.exit(0);
    }

    /**
     * Called when the application starts. From {@link Application} <br>
     * Creates a new instance of the application window and presents it.
     */
    @Override
    public void activate() {
        Window win = this.getActiveWindow();
        if (win == null)
            win = LEDSuiteWindow.create(this);
        window = (LEDSuiteWindow) win;
        win.present();
    }
}
