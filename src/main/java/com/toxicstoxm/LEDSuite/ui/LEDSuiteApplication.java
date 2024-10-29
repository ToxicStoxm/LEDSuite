package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.PacketManager;
import com.toxicstoxm.LEDSuite.communication.packet_management.PacketReceivedHandler;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.enums.FileState;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.enums.LidState;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.ErrorPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.MenuReplyPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.SettingsReplyPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.StatusReplyPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.upload_reply.UploadFileCollisionReplyPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.upload_reply.UploadSuccessReplyPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.*;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.media_request.PauseRequestPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.media_request.PlayRequestPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.media_request.StopRequestPacket;
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.foreign.MemorySegment;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static com.toxicstoxm.LEDSuite.settings.LEDSuiteSettingsBundle.*;

/**
 * Main application class. Initializes and starts {@link LEDSuiteWindow} and other vital elements like: <br>
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
    private static LEDSuiteWindow window;

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
        var settingsApply = new SimpleAction("settings_apply", null);

        quit.onActivate(_ -> quit());
        status.onActivate(_ -> window.displayStatusDialog());
        settings.onActivate(_ -> window.displayPreferencesDialog());
        shortcuts.onActivate(_ -> window.displayShortcutsWindow());
        about.onActivate(_ -> window.displayAboutDialog());
        sidebarToggle.onActivate(_ -> window.toggle_sidebar());

        CooldownManger.addAction("sidebarFileManagementUploadPage", () -> window.uploadPageSelect(), 500);
        sidebarFileManagementUploadPage.onActivate(_ -> {
            if (!CooldownManger.call("sidebarFileManagementUploadPage")) logger.info("Upload page select on cooldown!", new LEDSuiteLogAreas.USER_INTERACTIONS());
        });

        CooldownManger.addAction("settingsApply", () -> window.settingsDialogApply(), 500);
        CooldownManger.addAction("settingsApplyFail", () -> window.settingsDialogApplyFail(), 600);
        settingsApply.onActivate(_ -> {
            if (!CooldownManger.call("settingsApply")) {
                logger.info("Settings apply on cooldown!", new LEDSuiteLogAreas.USER_INTERACTIONS());
                CooldownManger.call("settingsApplyFail");
            }
        });

        addAction(quit);
        addAction(status);
        addAction(settings);
        addAction(shortcuts);
        addAction(about);
        addAction(sidebarToggle);
        addAction(sidebarFileManagementUploadPage);
        addAction(settingsApply);

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
        packetManager.autoRegisterPackets("com.toxicstoxm.LEDSuite.communication.packet_management.packets");
    }

    /**
     * Creates a new websocket client instance and connects it with the websocket server.
     */
    private void startCommunicationSocket() {
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

        new LEDSuiteRunnable() {
            @Override
            public void run() {
                webSocketCommunication.enqueueMessage(
                        StatusRequestPacket.builder().build().serialize()
                );
            }
        }.runTaskTimerAsynchronously(1000, 1000);
    }

    /**
     * Registers all supported network packets in {@link PacketManager}.
     * @see PacketManager
     * @see Packet
     */
    private void registerPackets() {

        // request packets

        var statusRequestPacket = StatusRequestPacket.builder().build();
        packetManager.registerPacket(statusRequestPacket);

        var errorPacket = ErrorPacket.builder().build();
        packetManager.registerPacket(errorPacket);

        var menuRequestPacket = MenuRequestPacket.builder().build();
        packetManager.registerPacket(menuRequestPacket);

        var playRequestPacket = PlayRequestPacket.builder().build();
        packetManager.registerPacket(playRequestPacket);

        var pauseRequestPacket = PauseRequestPacket.builder().build();
        packetManager.registerPacket(pauseRequestPacket);

        var stopRequestPacket = StopRequestPacket.builder().build();
        packetManager.registerPacket(stopRequestPacket);

        var fileUploadRequestPacket = FileUploadRequestPacket.builder().build();
        packetManager.registerPacket(fileUploadRequestPacket);

        var menuChangeRequestPacket = MenuChangeRequestPacket.builder().build();
        packetManager.registerPacket(menuChangeRequestPacket);

        var renameRequestPacket = RenameRequestPacket.builder().build();
        packetManager.registerPacket(renameRequestPacket);

        var settingsRequestPacket = SettingsRequestPacket.builder().build();
        packetManager.registerPacket(settingsRequestPacket);

        var settingsChangeRequestPacket = SettingsChangeRequestPacket.builder().build();
        packetManager.registerPacket(settingsChangeRequestPacket);

        // reply packets

        var statusReplyPacket = StatusReplyPacket.builder().build();
        packetManager.registerPacket(statusReplyPacket);

        var uploadFileCollisionReplyPacket = UploadFileCollisionReplyPacket.builder().build();
        packetManager.registerPacket(uploadFileCollisionReplyPacket);

        var uploadSuccessReplyPacket = UploadSuccessReplyPacket.builder().build();
        packetManager.registerPacket(uploadSuccessReplyPacket);

        var menuReplyPacket = MenuReplyPacket.builder().build();
        packetManager.registerPacket(menuReplyPacket);

        var settingsReplyPacket = SettingsReplyPacket.builder().build();
        packetManager.registerPacket(settingsReplyPacket);
    }

    /**
     * Tests all registered communication packets.
     * @see PacketManager
     * @see CommunicationPacket
     */
    private void testPackets() {

        try {

            logger.debug("Testing communication packets:", new LEDSuiteLogAreas.COMMUNICATION());

            // request packets
            logger.debug("\nTesting request packets:", new LEDSuiteLogAreas.COMMUNICATION());

            logger.debug("\nTesting status request packet -->", new LEDSuiteLogAreas.COMMUNICATION());
            StatusRequestPacket statusRequestPacket = StatusRequestPacket.builder().build();
            packetReceivedHandler.handleIncomingPacket(packetManager.deserialize(statusRequestPacket.serialize()));

            logger.debug("\nTesting menu request packet -->", new LEDSuiteLogAreas.COMMUNICATION());
            MenuRequestPacket menuRequestPacket = MenuRequestPacket.builder()
                    .requestFile("Test-Animation")
                    .build();
            packetReceivedHandler.handleIncomingPacket(packetManager.deserialize(menuRequestPacket.serialize()));

            logger.debug("\nTesting play request packet -->", new LEDSuiteLogAreas.COMMUNICATION());
            PlayRequestPacket playRequestPacket = PlayRequestPacket.builder()
                    .requestFile("Test-Animation")
                    .build();
            packetReceivedHandler.handleIncomingPacket(packetManager.deserialize(playRequestPacket.serialize()));

            logger.debug("\nTesting pause request packet -->", new LEDSuiteLogAreas.COMMUNICATION());
            PauseRequestPacket pauseRequestPacket = PauseRequestPacket.builder()
                    .requestFile("Test-Animation")
                    .build();
            packetReceivedHandler.handleIncomingPacket(packetManager.deserialize(pauseRequestPacket.serialize()));

            logger.debug("\nTesting stop request packet -->", new LEDSuiteLogAreas.COMMUNICATION());
            StopRequestPacket stopRequestPacket = StopRequestPacket.builder()
                    .requestFile("Test-Animation")
                    .build();
            packetReceivedHandler.handleIncomingPacket(packetManager.deserialize(stopRequestPacket.serialize()));

            logger.debug("\nTesting file upload request packet -->", new LEDSuiteLogAreas.COMMUNICATION());
            FileUploadRequestPacket fileUploadRequestPacket = FileUploadRequestPacket.builder()
                    .requestFile("Test-Animation")
                    .packetCount(512)
                    .uploadSessionId(String.valueOf(UUID.randomUUID()))
                    .build();
            packetReceivedHandler.handleIncomingPacket(packetManager.deserialize(fileUploadRequestPacket.serialize()));

            logger.debug("\nTesting menu change request packet -->", new LEDSuiteLogAreas.COMMUNICATION());
            MenuChangeRequestPacket menuChangeRequestPacket = MenuChangeRequestPacket.builder()
                    .objectPath("Test-Object")
                    .objectValue("6942")
                    .build();
            packetReceivedHandler.handleIncomingPacket(packetManager.deserialize(menuChangeRequestPacket.serialize()));

            logger.debug("\nTesting rename request packet -->", new LEDSuiteLogAreas.COMMUNICATION());
            RenameRequestPacket renameRequestPacket = RenameRequestPacket.builder()
                    .requestFile("Test-Animation")
                    .newName("Test-Animation-2")
                    .build();
            packetReceivedHandler.handleIncomingPacket(packetManager.deserialize(renameRequestPacket.serialize()));

            logger.debug("\nTesting settings request packet -->", new LEDSuiteLogAreas.COMMUNICATION());
            SettingsRequestPacket settingsRequestPacket = SettingsRequestPacket.builder().build();
            packetReceivedHandler.handleIncomingPacket(packetManager.deserialize(settingsRequestPacket.serialize()));

            logger.debug("\nTesting settings change request packet -->", new LEDSuiteLogAreas.COMMUNICATION());
            SettingsChangeRequestPacket settingsChangeRequestPacket = SettingsChangeRequestPacket.builder()
                    .brightness(100)
                    .selectedColorMode("RGB")
                    .build();
            packetReceivedHandler.handleIncomingPacket(packetManager.deserialize(settingsChangeRequestPacket.serialize()));

            // reply packets
            logger.debug("\nTesting reply packets:", new LEDSuiteLogAreas.COMMUNICATION());

            logger.debug("\nTesting status reply packet -->", new LEDSuiteLogAreas.COMMUNICATION());
            StatusReplyPacket statusReplyPacket  = StatusReplyPacket.builder()
                    .fileState(FileState.playing)
                    .selectedFile("Test-Animation")
                    .currentDraw(1.9)
                    .voltage(5.0)
                    .lidState(LidState.open)
                    .animations(List.of(
                            new StatusReplyPacket.InteractiveAnimation("1", "Test-Animation1", "some-gnome-label", true),
                            new StatusReplyPacket.InteractiveAnimation("1", "Test-Animation2", "some-gnome-label", false),
                            new StatusReplyPacket.InteractiveAnimation("1", "Test-Animation3", "some-gnome-label", true)
                    ))
                    .build();
            packetReceivedHandler.handleIncomingPacket(packetManager.deserialize(statusReplyPacket.serialize()));

            logger.debug("\nTesting upload file collision reply packet -->", new LEDSuiteLogAreas.COMMUNICATION());
            UploadFileCollisionReplyPacket uploadFileCollisionReplyPacket = UploadFileCollisionReplyPacket.builder()
                    .currentName("Test-Animation")
                    .build();
            packetReceivedHandler.handleIncomingPacket(packetManager.deserialize(uploadFileCollisionReplyPacket.serialize()));

            logger.debug("\nTesting upload file success reply packet -->", new LEDSuiteLogAreas.COMMUNICATION());
            UploadSuccessReplyPacket uploadSuccessReplyPacket = UploadSuccessReplyPacket.builder()
                    .fileName("Test-Animation")
                    .build();
            packetReceivedHandler.handleIncomingPacket(packetManager.deserialize(uploadSuccessReplyPacket.serialize()));

            logger.debug("\nTesting settings reply packet -->", new LEDSuiteLogAreas.COMMUNICATION());
            SettingsReplyPacket settingsReplyPacket = SettingsReplyPacket.builder()
                    .brightness(100)
                    .selectedColorMode("RGB")
                    .availableColorModes(List.of("RGB", "RGBW"))
                    .build();
            packetReceivedHandler.handleIncomingPacket(packetManager.deserialize(settingsReplyPacket.serialize()));

            // error packets
            logger.debug("\nTesting error packets:", new LEDSuiteLogAreas.COMMUNICATION());

            logger.debug("\nTesting upload file collision reply packet -->", new LEDSuiteLogAreas.COMMUNICATION());
            ErrorPacket errorPacket = ErrorPacket.builder()
                    .code(1)
                    .source(Constants.Communication.YAML.Values.Error.Sources.PARSING_ERROR)
                    .name("Failed to parse YAML!")
                    .severity(5)
                    .build();
            packetReceivedHandler.handleIncomingPacket(packetManager.deserialize(errorPacket.serialize()));

            logger.info("All packet tests passed!", new LEDSuiteLogAreas.COMMUNICATION());

        } catch (PacketManager.DeserializationException e) {
            logger.warn("Tests for communication packets failed!\n", new LEDSuiteLogAreas.COMMUNICATION());
            logger.stacktrace("Stacktrace: \n", new LEDSuiteLogAreas.COMMUNICATION());
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.stacktrace(sw.toString());
        }

    }

    /**
     * Called when the application window is closed. <br>
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

        testPackets();

        startCommunicationSocket();
    }
}
