package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.PacketManager;
import com.toxicstoxm.LEDSuite.communication.packet_management.PacketReceivedHandler;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.ErrorCode;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.menu_error.MenuErrorPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.menu_error.Severity;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.server_error.ServerErrorPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.SettingsReplyPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.AnimationMenuManager;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.status_reply.FileState;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.status_reply.LidState;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.status_reply.StatusReplyPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.upload_reply.UploadSuccessReplyPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.*;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.media_request.PauseRequestPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.media_request.PlayRequestPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.media_request.StopRequestPacket;
import com.toxicstoxm.LEDSuite.communication.websocket.BinaryPacket;
import com.toxicstoxm.LEDSuite.communication.websocket.WebSocketClient;
import com.toxicstoxm.LEDSuite.communication.websocket.WebSocketCommunication;
import com.toxicstoxm.LEDSuite.communication.websocket.WebSocketUpload;
import com.toxicstoxm.LEDSuite.formatting.StringFormatter;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.settings.LEDSuiteSettingsBundle;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteScheduler;
import com.toxicstoxm.LEDSuite.time.CooldownManger;
import com.toxicstoxm.LEDSuite.time.TickingSystem;
import com.toxicstoxm.LEDSuite.ui.dialogs.UpdateCallback;
import com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialog.OverwriteConfirmationDialog;
import com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialog.RenameDialog;
import com.toxicstoxm.LEDSuite.upload.UploadAbortException;
import com.toxicstoxm.LEDSuite.upload.UploadManager;
import com.toxicstoxm.YAJL.YAJLLogger;
import com.toxicstoxm.YAJL.levels.YAJLLogLevels;
import com.toxicstoxm.YAJSI.api.settings.YAJSISettingsManager;
import io.github.jwharm.javagi.gobject.annotations.InstanceInit;
import io.github.jwharm.javagi.gtk.types.TemplateTypes;
import lombok.Getter;
import org.gnome.adw.AlertDialog;
import org.gnome.adw.Application;
import org.gnome.gio.ApplicationFlags;
import org.gnome.gio.SimpleAction;
import org.gnome.glib.GLib;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.Window;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.foreign.MemorySegment;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static com.toxicstoxm.LEDSuite.settings.LEDSuiteSettingsBundle.EnableSettingsLogging;
import static com.toxicstoxm.LEDSuite.settings.LEDSuiteSettingsBundle.WebsocketURI;

/**
 * Main application class. Initializes and starts {@link LEDSuiteWindow} and other vital elements like: <br>
 * {@link YAJLLogger} {@link LEDSuiteScheduler} {@link YAJSISettingsManager}
 * @since 1.0.0
 */
public class LEDSuiteApplication extends Application {

    private static final Type gtype = TemplateTypes.register(LEDSuiteApplication.class);

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

    @Getter
    private static AnimationMenuManager animationMenuManager;

    @Getter
    private static UploadManager uploadManager;

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
     * Creates and registers necessary UI actions. Creates and starts the logger, settings manager, and task scheduler.
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

        // init settings manager
        initYAJSI();

        // init logger
        initYAJL();

        scheduler = new LEDSuiteScheduler();
        tickingSystem = new TickingSystem();

        animationMenuManager = new AnimationMenuManager("com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.widgets");
        animationMenuManager.autoRegister();

        packetManager = new PacketManager("com.toxicstoxm.LEDSuite.communication.packet_management.packets");
        packetReceivedHandler = new PacketReceivedHandler();
        packetManager.autoRegister();

        uploadManager = new UploadManager();
    }

    private void initYAJSI() {
        configMgr = YAJSISettingsManager.builder()
                .buildWithConfigFile(
                        new YAJSISettingsManager.ConfigFile(
                                Constants.FileSystem.CONFIG_FILE_PATH,
                                getClass().getClassLoader().getResource("config.yaml")
                        ),
                        LEDSuiteSettingsBundle.class
                );
    }

    private void initYAJL() {
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
    }

    @Getter
    private static boolean connecting = false;

    @Getter
    private static long connectionAttempt = -1;

    /**
     * Creates a new websocket client instance and connects it with the websocket server.
     */
    public static boolean startCommunicationSocket() {
        URI serverAddress;
        try {
            String baseAddress = WebsocketURI.getInstance().get();
            if (!baseAddress.endsWith("/")) baseAddress = baseAddress + "/";
            baseAddress = baseAddress + "communication";
            serverAddress = new URI(baseAddress);
        } catch (NullPointerException | URISyntaxException e) {
            return false;
        }

        connecting = true;
        connectionAttempt = System.currentTimeMillis();

        long start = System.currentTimeMillis();
        long timeElapsed = System.currentTimeMillis() - start;
        boolean minDelayReached = false;
        boolean retry = false;
        Boolean result = null;

        window.setServerConnected(false);
        GLib.idleAddOnce(() -> window.showAnimationListSpinner(true));

        if (webSocketCommunication != null) webSocketCommunication.shutdown();

        webSocketCommunication = new WebSocketClient(new WebSocketCommunication(), serverAddress);

        while (timeElapsed < Constants.UI.SettingsDialog.CONNECTION_TIMEOUT) {

            if (minDelayReached) {
                if (webSocketCommunication.isConnected()) {
                    result = true;
                    break;
                } else if (retry) {
                    webSocketCommunication.shutdown();
                    webSocketCommunication = new WebSocketClient(new WebSocketCommunication(), serverAddress);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        result = false;
                        break;
                    }
                }
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                result = false;
                break;
            }

            timeElapsed = System.currentTimeMillis() - start;
            minDelayReached = timeElapsed > Constants.UI.SettingsDialog.MINIMUM_DELAY;
            retry = timeElapsed > Constants.UI.SettingsDialog.RETRY_DELAY;
        }

        GLib.idleAddOnce(() -> window.showAnimationListSpinner(false));
        connecting = false;

        if (result != null) {
            return result;
        }

        LEDSuiteApplication.getLogger().info("Network connection timed out!", new LEDSuiteLogAreas.NETWORK());

        return false;
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
                    .sha256(String.valueOf(UUID.randomUUID()))
                    .uploadSessionId(String.valueOf(UUID.randomUUID()))
                    .build();
            packetReceivedHandler.handleIncomingPacket(packetManager.deserialize(fileUploadRequestPacket.serialize()));

            logger.debug("\nTesting menu change request packet -->", new LEDSuiteLogAreas.COMMUNICATION());
            MenuChangeRequestPacket menuChangeRequestPacket = MenuChangeRequestPacket.builder()
                    .objectId("Test-Object")
                    .objectValue("6942")
                    .fileName("Test-Animation")
                    .build();
            packetReceivedHandler.handleIncomingPacket(packetManager.deserialize(menuChangeRequestPacket.serialize()));

            logger.debug("\nTesting settings request packet -->", new LEDSuiteLogAreas.COMMUNICATION());
            SettingsRequestPacket settingsRequestPacket = SettingsRequestPacket.builder().build();
            packetReceivedHandler.handleIncomingPacket(packetManager.deserialize(settingsRequestPacket.serialize()));

            logger.debug("\nTesting settings change request packet -->", new LEDSuiteLogAreas.COMMUNICATION());
            SettingsChangeRequestPacket settingsChangeRequestPacket = SettingsChangeRequestPacket.builder()
                    .brightness(100)
                    .selectedColorMode("RGB")
                    .build();
            packetReceivedHandler.handleIncomingPacket(packetManager.deserialize(settingsChangeRequestPacket.serialize()));

            logger.debug("\nTesting settings reset request packet -->", new LEDSuiteLogAreas.COMMUNICATION());
            SettingsResetRequestPacket settingsResetRequestPacket = SettingsResetRequestPacket.builder().build();
            packetReceivedHandler.handleIncomingPacket(packetManager.deserialize(settingsResetRequestPacket.serialize()));

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
                            new StatusReplyPacket.Animation("1", "Test-Animation1", "some-gnome-label", true),
                            new StatusReplyPacket.Animation("1", "Test-Animation2", "some-gnome-label", false),
                            new StatusReplyPacket.Animation("1", "Test-Animation3", "some-gnome-label", true)
                    ))
                    .build();
            packetReceivedHandler.handleIncomingPacket(packetManager.deserialize(statusReplyPacket.serialize()));

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

            logger.debug("\nTesting server error packet -->", new LEDSuiteLogAreas.COMMUNICATION());
            ServerErrorPacket serverErrorPacket = ServerErrorPacket.builder()
                    .code(ErrorCode.ChecksumOfFileIsInvalid)
                    .source(Constants.Communication.YAML.Values.Error.ServerError.Sources.PARSING_ERROR)
                    .name("Failed to parse YAML!")
                    .severity(5)
                    .build();
            packetReceivedHandler.handleIncomingPacket(packetManager.deserialize(serverErrorPacket.serialize()));

            logger.debug("\nTesting menu error packet -->", new LEDSuiteLogAreas.COMMUNICATION());
            MenuErrorPacket menuErrorPacket = MenuErrorPacket.builder()
                    .fileName("Test-Animation")
                    .severity(Severity.FATAL)
                    .code(ErrorCode.GroupSectionEmptyOrMissing)
                    .message("Failed to parse YAML!")
                    .build();
            packetReceivedHandler.handleIncomingPacket(packetManager.deserialize(menuErrorPacket.serialize()));

            logger.info("All packet tests passed!", new LEDSuiteLogAreas.COMMUNICATION());

        } catch (DeserializationException e) {
            logger.warn("Tests for communication packets failed!\n", new LEDSuiteLogAreas.COMMUNICATION());
            logger.stacktrace("Stacktrace: \n", new LEDSuiteLogAreas.COMMUNICATION());
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.stacktrace(sw.toString());
        }

    }

    public static void triggerFileUpload(String filePath, boolean startAnimationAfterUpload, UpdateCallback<Boolean> uploadFinishCallback) {
        try {
            if (webSocketCommunication == null || !webSocketCommunication.isConnected()) {
                window.uploadPageSelect();
                throw new UploadAbortException(() -> logger.warn("Cancelling file upload because communication websocket is not connected!", new LEDSuiteLogAreas.NETWORK()));
            }

            URI uploadEndpointPath;

            try {
                String baseAddress = WebsocketURI.getInstance().get();
                if (!baseAddress.endsWith("/")) baseAddress = baseAddress + "/";
                baseAddress = baseAddress + "upload";
                uploadEndpointPath = new URI(baseAddress);
            } catch (URISyntaxException e) {
                throw new UploadAbortException(() -> logger.warn("Cancelled file upload because file upload websocket endpoint is unreachable!", new LEDSuiteLogAreas.NETWORK()));
            }

            logger.info("Verified upload websocket endpoint at: " + uploadEndpointPath.getPath(), new LEDSuiteLogAreas.NETWORK());

            File fileToUpload;

            try {
                fileToUpload = new File(filePath);
            } catch (NullPointerException e) {
                throw new UploadAbortException(() -> logger.warn("Cancelled file upload because selected file '" + filePath + "' does not exist or isn't a file!", new LEDSuiteLogAreas.NETWORK()));
            }

            if (!fileToUpload.exists() || !fileToUpload.isFile()) {
                throw new UploadAbortException(() -> logger.warn("Cancelled file upload because selected file '" + filePath + "' does not exist or isn't a file!", new LEDSuiteLogAreas.NETWORK()));
            }

            String checksum;
            byte[] data;
            try {
                data = Files.readAllBytes(Path.of(fileToUpload.getAbsolutePath()));
                byte[] hash = MessageDigest.getInstance("SHA-256").digest(data);
                checksum = new BigInteger(1, hash).toString(16);
            } catch (IOException | NoSuchAlgorithmException e) {
                throw new UploadAbortException(() -> logger.warn("Cancelled file upload because file couldn't be loaded or checksum failed!", new LEDSuiteLogAreas.NETWORK()));
            }

            String uploadSessionID = String.valueOf(UUID.randomUUID());
            AtomicReference<String> fileName = new AtomicReference<>(StringFormatter.getFileNameFromPath(filePath));
            if (uploadSessionID == null) {
                throw new UploadAbortException(() -> logger.error("Cancelled file upload because upload session id was null! This should be reported!", new LEDSuiteLogAreas.NETWORK()));
            }

            uploadManager.setPending(fileName.get(), uploadPermitted -> {
                if (uploadPermitted) {
                    uploadFile(fileToUpload, startAnimationAfterUpload, uploadFinishCallback, uploadSessionID, uploadEndpointPath, checksum);
                } else {
                    GLib.idleAddOnce(() -> {
                        AlertDialog.ResponseCallback cb = getResponseCallback(fileName, uploadSessionID, checksum, result -> {
                            if (result) uploadManager.removePending(fileName.get());
                            GLib.idleAddOnce(() -> uploadFinishCallback.update(result));
                        });

                        window.displayFileCollisionDialog(cb,"Animation with name '" + fileName.get() + "' already exists.");
                    });
                }
            });

            webSocketCommunication.enqueueMessage(
                    FileUploadRequestPacket.builder()
                            .uploadSessionId(uploadSessionID)
                            .requestFile(fileName.get())
                            .sha256(checksum)
                            .build().serialize()
            );

        } catch (UploadAbortException e) {
            e.printErrorMessage();
            uploadFinishCallback.update(false);
        }
    }

    @Contract(pure = true)
    private static AlertDialog.@NotNull ResponseCallback getResponseCallback(AtomicReference<String> fileName, String uploadSessionID, String checksum, UpdateCallback<Boolean> uploadFinishCallback) {
        return response -> {
            try {
                logger.info("File collision dialog response result: " + response, new LEDSuiteLogAreas.UI_CONSTRUCTION());

                switch (response) {
                    case "cancel" -> throw new UploadAbortException(() -> logger.info("File upload was cancelled by the user!", new LEDSuiteLogAreas.USER_INTERACTIONS()));
                    case "rename" -> {
                        logger.info("Rename selected.", new LEDSuiteLogAreas.USER_INTERACTIONS());
                        var renameDialog = RenameDialog.create(fileName.get());

                        renameDialog.onResponse(renameResponse -> GLib.idleAddOnce(() -> {
                            String newName = renameDialog.getNewName();

                            if (renameResponse.equals("cancel")) {
                                window.displayFileCollisionDialog(getResponseCallback(fileName, uploadSessionID, checksum, uploadFinishCallback),"Animation with name '" + fileName.get() + "' already exists.");
                            } else if (renameResponse.equals("rename")) {
                                if (newName == null || newName.isBlank() || newName.equals(fileName.get()))
                                    throw new RuntimeException("New file name invalid '" + newName + "'!");

                                uploadManager.changePendingName(fileName.get(), newName);

                                webSocketCommunication.enqueueMessage(
                                        FileUploadRequestPacket.builder()
                                                .uploadSessionId(uploadSessionID)
                                                .requestFile(newName)
                                                .sha256(checksum)
                                                .build().serialize()
                                );
                            }
                        }));

                        GLib.idleAddOnce(() -> renameDialog.present(LEDSuiteApplication.getWindow()));
                    }
                    case "overwrite" -> {

                        var confirmationDialog = OverwriteConfirmationDialog.create();

                        confirmationDialog.onResponse(confirmationResponse -> GLib.idleAddOnce(() -> {
                            if (confirmationResponse.equals("cancel")) {
                                window.displayFileCollisionDialog(getResponseCallback(fileName, uploadSessionID, checksum, uploadFinishCallback),"Animation with name '" + fileName.get() + "' already exists.");
                            } else if (confirmationResponse.equals("overwrite")) {
                                webSocketCommunication.enqueueMessage(
                                        FileUploadRequestPacket.builder()
                                                .uploadSessionId(uploadSessionID)
                                                .requestFile(fileName.get())
                                                .sha256(checksum)
                                                .forceOverwrite(true)
                                                .build().serialize()
                                );
                            }
                        }));

                        GLib.idleAddOnce(() -> confirmationDialog.present(LEDSuiteApplication.getWindow()));
                    }
                }
            } catch (UploadAbortException e) {
                e.printErrorMessage();
                UploadPageEndpoint endpoint = window.getUploadPageEndpoint();
                if (endpoint != null) {
                    UpdateCallback<Boolean> uploadSuccessCallback = endpoint.uploadSuccessCallback();
                    if (uploadSuccessCallback != null) {
                        uploadSuccessCallback.update(false);
                    }
                }
            }
        };
    }

    /**
     * Tries to upload the file
     * located at the specified file path to the server using {@link WebSocketClient} with {@link WebSocketUpload}.
     * @param fileToUpload the file to send to the sever
     * @param checksum the file checksum (sha256)
     * @param uploadEndpointPath the websocket endpoint to send the file to
     * @param uploadSessionID the session id to use for websocket communication
     * @param startAnimationAfterUpload if the uploaded file should be automatically started after the upload completed using {@link PlayRequestPacket}
     * @param uploadFinishCallback the function to call after the upload finished
     */
    public static void uploadFile(@NotNull File fileToUpload, boolean startAnimationAfterUpload, UpdateCallback<Boolean> uploadFinishCallback, String uploadSessionID, URI uploadEndpointPath, String checksum) {

        String filePath = fileToUpload.getAbsolutePath();

        final long fileSize = fileToUpload.length();
        final int packet_size = LEDSuiteSettingsBundle.PacketSize.getInstance().get();
        final long startTime = System.currentTimeMillis();

        AtomicLong transferredSize = new AtomicLong(0);

        String animationName = StringFormatter.getFileNameFromPath(filePath);
        AtomicReference<UpdateCallback<UploadStatistics>> uploadStatisticsUpdater = new AtomicReference<>(window.getUploadPageEndpoint().uploadStatisticsUpdater());
        AtomicLong speedAverage = new AtomicLong(-1);
        AtomicLong speedMeasurementCount = new AtomicLong(1);
        AtomicBoolean finished = new AtomicBoolean(false);
        AtomicLong lastUploadStatisticsUpdate = new AtomicLong(System.currentTimeMillis());

        WebSocketClient uploadEndpoint = new WebSocketClient(
                WebSocketUpload.builder()
                        .ready(true)
                        .sessionID(uploadSessionID)
                        .connectionNotifyCallback(() -> {
                            logger.info("Connected to upload websocket endpoint at: " + uploadEndpointPath.getPath(), new LEDSuiteLogAreas.NETWORK());
                            logger.info("Metadata:", new LEDSuiteLogAreas.NETWORK());
                            logger.info(" > Upload session ID: " + uploadSessionID, new LEDSuiteLogAreas.NETWORK());
                            logger.info(" > Filename: " + animationName, new LEDSuiteLogAreas.NETWORK());
                            logger.info(" > Checksum: " + checksum, new LEDSuiteLogAreas.NETWORK());
                        })
                        .progressUpdateUpdateCallback(progressUpdate -> {
                            int transferredBytes = progressUpdate.data().array().length;
                            transferredSize.addAndGet(transferredBytes);

                            window.setUploadProgress((double) transferredSize.get() / fileSize);

                            // Corrected calculation of bytes per second
                            double bytesPerMillisecond = (double) transferredBytes / Math.max(progressUpdate.timeElapsed(), 1);
                            double bytesPerSecond = bytesPerMillisecond * 1000;

                            // Initialize and calculate weighted moving average for speed
                            if (speedAverage.get() == -1) speedAverage.set(Math.round(bytesPerSecond));
                            else {
                                long newSpeedAverage = (speedAverage.get() * speedMeasurementCount.get() + Math.round(bytesPerSecond)) / (speedMeasurementCount.incrementAndGet());
                                speedAverage.set(newSpeedAverage);
                            }

                            boolean isLastPacket = progressUpdate.lastPacket();

                            logger.debug("Sent packet to server. [ID: " + speedMeasurementCount.get() + "; Size: " + transferredBytes + "; Last-Packet: " + isLastPacket + "]", new LEDSuiteLogAreas.NETWORK());

                            long now = System.currentTimeMillis();
                            long timeSinceLastStatsUpdate = now - lastUploadStatisticsUpdate.get();
                            if (timeSinceLastStatsUpdate > 1000) {
                                long estimatedMillisecondsRemaining = (speedAverage.get() > 0)
                                        ? ((fileSize - transferredSize.get()) / speedAverage.get()) * 1000
                                        : Long.MAX_VALUE;  // Handle division by zero for safety

                                uploadStatisticsUpdater.set(window.getUploadPageEndpoint().uploadStatisticsUpdater());
                                if (uploadStatisticsUpdater.get() != null) {
                                    uploadStatisticsUpdater.get().update(
                                            UploadStatistics.builder()
                                                    .bytesPerSecond(speedAverage.get())
                                                    .millisecondsRemaining(estimatedMillisecondsRemaining)
                                                    .build()
                                    );
                                }
                                lastUploadStatisticsUpdate.set(now);
                            }

                            if (isLastPacket) {
                                long totalTimeElapsed = System.currentTimeMillis() - startTime;

                                logger.info("File upload finished:", new LEDSuiteLogAreas.NETWORK());
                                logger.info(" > File: " + animationName, new LEDSuiteLogAreas.NETWORK());
                                logger.info(" > Transferred Bytes: " + transferredSize.get(), new LEDSuiteLogAreas.NETWORK());
                                logger.info(" > Transferred Packets (<=" + packet_size / 1024 + "KB): " + speedMeasurementCount.get(), new LEDSuiteLogAreas.NETWORK());
                                logger.info(" > Average Speed: " + StringFormatter.formatSpeed(speedAverage.get()), new LEDSuiteLogAreas.NETWORK());
                                logger.info(" > Total time elapsed: " + StringFormatter.formatDuration(totalTimeElapsed), new LEDSuiteLogAreas.NETWORK());

                                finished.set(true);
                            }
                        })
                        .build(), uploadEndpointPath
        );

        logger.info("Splitting file into chunks:", new LEDSuiteLogAreas.NETWORK());
        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] buffer = new byte[packet_size];
            int bytesRead;
            boolean isLastChunk = false;

            int cnt = 0;

            // Read and send each chunk
            while ((bytesRead = fis.read(buffer)) != -1) {
                // Check if this is the last chunk by peeking the next read
                int nextByte = fis.read();
                if (nextByte == -1) {
                    isLastChunk = true;
                } else {
                    // If it's not the last chunk, push back the byte to ensure nothing is skipped
                    fis.getChannel().position(fis.getChannel().position() - 1);
                }

                // Copy buffer to chunk array of the exact bytesRead length if it's smaller than packet_size
                byte[] chunk = (bytesRead == packet_size) ? buffer : new byte[bytesRead];
                if (bytesRead < packet_size) {
                    System.arraycopy(buffer, 0, chunk, 0, bytesRead);
                }

                cnt++;

                logger.debug("Enqueueing BinaryPacket [ID: " + cnt + "; isLast: " + isLastChunk +
                        "; Data Hash: " + Arrays.hashCode(chunk) + "]", new LEDSuiteLogAreas.NETWORK());

                // Send the chunk with the isLast flag
                uploadEndpoint.enqueueBinaryMessage(
                        BinaryPacket.builder()
                                .data(ByteBuffer.wrap(Arrays.copyOf(chunk, chunk.length)))
                                .isLast(isLastChunk)
                                .build()
                );

                // Reset isLastChunk to false for the next iteration
                isLastChunk = false;
            }

            logger.info("Upload preparation complete!", new LEDSuiteLogAreas.NETWORK());
        } catch (IOException e) {
            logger.warn("Error during upload preparation: " + e.getMessage(), new LEDSuiteLogAreas.NETWORK());
            UploadPageEndpoint endpoint = window.getUploadPageEndpoint();
            if (endpoint != null) {
                UpdateCallback<Boolean> uploadSuccessCallback = endpoint.uploadSuccessCallback();
                if (uploadSuccessCallback != null) {
                    uploadSuccessCallback.update(false);
                }
            }
            return;
        }

        new LEDSuiteRunnable() {
            @Override
            public void run() {
                while (!finished.get()) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        logger.warn("Upload finish callback was interrupted!", new LEDSuiteLogAreas.NETWORK());
                        UploadPageEndpoint endpoint = window.getUploadPageEndpoint();
                        if (endpoint != null) {
                            UpdateCallback<Boolean> uploadSuccessCallback = endpoint.uploadSuccessCallback();
                            if (uploadSuccessCallback != null) {
                                uploadSuccessCallback.update(false);
                            }
                        }
                        cancel();
                    }
                }
                UploadPageEndpoint endpoint = window.getUploadPageEndpoint();
                if (endpoint != null) {
                    UpdateCallback<Boolean> uploadSuccessCallback = endpoint.uploadSuccessCallback();
                    if (uploadSuccessCallback != null) {
                        uploadSuccessCallback.update(true);
                    }
                }
                window.uploadFinished();

                if (startAnimationAfterUpload) {
                    webSocketCommunication.enqueueMessage(
                            PlayRequestPacket.builder()
                                    .requestFile(animationName)
                                    .build().serialize()
                    );
                }
            }
        }.runTaskAsynchronously();
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

        //testPackets();
        new LEDSuiteRunnable() {
            @Override
            public void run() {
                startCommunicationSocket();
            }
        }.runTaskAsynchronously();

    }
}
