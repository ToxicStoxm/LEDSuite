package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.authentication.AuthManagerEndpoint;
import com.toxicstoxm.LEDSuite.communication.packet_management.PacketManager;
import com.toxicstoxm.LEDSuite.communication.packet_management.PacketReceivedHandler;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.AnimationMenuManager;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.FileUploadRequestPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.media_request.PlayRequestPacket;
import com.toxicstoxm.LEDSuite.communication.websocket.BinaryPacket;
import com.toxicstoxm.LEDSuite.communication.websocket.WebSocketClient;
import com.toxicstoxm.LEDSuite.communication.websocket.WebSocketCommunication;
import com.toxicstoxm.LEDSuite.communication.websocket.WebSocketUpload;
import com.toxicstoxm.LEDSuite.formatting.StringFormatter;
import com.toxicstoxm.LEDSuite.gettext.Translations;
import com.toxicstoxm.LEDSuite.settings.LEDSuiteSettingsBundle;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteScheduler;
import com.toxicstoxm.LEDSuite.time.CooldownManager;
import com.toxicstoxm.LEDSuite.time.TickingSystem;
import com.toxicstoxm.LEDSuite.ui.dialogs.UpdateCallback;
import com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs.*;
import com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog.ServerState;
import com.toxicstoxm.LEDSuite.upload.UploadAbortException;
import com.toxicstoxm.LEDSuite.upload.UploadManager;
import com.toxicstoxm.YAJL.Logger;
import com.toxicstoxm.YAJL.YAJLManager;
import com.toxicstoxm.YAJL.config.YAJLManagerConfig;
import com.toxicstoxm.YAJSI.api.settings.SettingsManager;
import com.toxicstoxm.YAJSI.api.settings.YAMLUpdatingBehaviour;
import io.github.jwharm.javagi.gobject.annotations.InstanceInit;
import io.github.jwharm.javagi.gtk.types.TemplateTypes;
import lombok.Getter;
import org.gnome.adw.AlertDialog;
import org.gnome.adw.Application;
import org.gnome.adw.ApplicationWindow;
import org.gnome.gio.ApplicationFlags;
import org.gnome.gio.SimpleAction;
import org.gnome.glib.GLib;
import org.gnome.gobject.GObject;
import org.gnome.gtk.Window;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Main application class. Initializes and starts {@link LEDSuiteWindow} and other vital elements like: <br>
 * {@link Logger} {@link LEDSuiteScheduler}
 * @since 1.0.0
 */
public class LEDSuiteApplication extends Application {

    static {
        TemplateTypes.register(LEDSuiteApplication.class);
    }

    public LEDSuiteApplication(MemorySegment address) {
        super(address);
    }

    public static String version = "@version@";

    private static Logger logger;

    @Getter
    private static MainWindow window;

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

    @Getter
    private static AuthManagerEndpoint authManager;

    @Getter
    private static LEDSuiteSettingsBundle settings;

    /**
     * Creates a new LEDSuiteApplication object with app-id and default flags
     * @return the newly created LEDSuiteApplication instance
     */
    public static LEDSuiteApplication create() {
        return GObject.newInstance(LEDSuiteApplication.class,
                "application-id", Constants.Application.ID,
                "flags", ApplicationFlags.DEFAULT_FLAGS);
    }

    /**
     * Creates and registers necessary UI actions. Creates and starts the logger, settings manager, and task scheduler.
     */
    @InstanceInit
    public void init() {

        System.out.println("Initializing...");

        // init settings manager
        System.out.println("Initializing YAJSI (settings manager)...");
        initYAJSI();

        // init logger
        System.out.println("Initializing YAJL (logger)...");
        initYAJL();

        logger.info("Initializing application...");
        logger.verbose("Initializing UI...");
        logger.verbose("Initializing UI-Actions...");
        logger.verbose(" > create");

        var quit = new SimpleAction("quit", null);
        var status = new SimpleAction("status", null);
        var settings = new SimpleAction("settings", null);
        var shortcuts = new SimpleAction("shortcuts", null);
        var about = new SimpleAction("about", null);
        var sidebarToggle = new SimpleAction("sidebar_toggle", null);
        var sidebarFileManagementUploadPage = new SimpleAction("sidebar_file_management_upload_page", null);
        var settingsApply = new SimpleAction("settings_apply", null);

        logger.verbose(" > add callbacks");

        quit.onActivate(_ -> GLib.idleAddOnce(() -> {
            window.asApplicationWindow().close();
            this.quit();
        }));
        status.onActivate(_ -> window.displayStatusDialog());
        settings.onActivate(_ -> window.displayPreferencesDialog());
        shortcuts.onActivate(_ -> window.displayShortcutsWindow());
        about.onActivate(_ -> window.displayAboutDialog());
        sidebarToggle.onActivate(_ -> window.toggleSidebar());

        CooldownManager.addAction("sidebarFileManagementUploadPage", () -> window.uploadPageSelect(), 500);
        sidebarFileManagementUploadPage.onActivate(_ -> {
            if (!CooldownManager.call("sidebarFileManagementUploadPage"))
                logger.info("Upload page select on cooldown!");
        });

        CooldownManager.addAction("settingsApply", () -> window.settingsDialogApply(), 500);
        CooldownManager.addAction("settingsApplyFail", () -> window.applyButtonCooldown(), 600);
        settingsApply.onActivate(_ -> {
            if (!CooldownManager.call("settingsApply")) {
                logger.info("Settings apply on cooldown!");
                CooldownManager.call("settingsApplyFail");
            }
        });

        logger.verbose(" > register");

        addAction(quit);
        addAction(status);
        addAction(settings);
        addAction(shortcuts);
        addAction(about);
        addAction(sidebarToggle);
        addAction(sidebarFileManagementUploadPage);
        addAction(settingsApply);

        logger.verbose(" > set keyboard shortcuts");

        setAccelsForAction("app.quit", new String[]{"<Control>q"});
        setAccelsForAction("app.status", new String[]{"<Alt>s"});
        setAccelsForAction("app.settings", new String[]{"<Control>comma"});
        setAccelsForAction("app.shortcuts", new String[]{"<Control>question"});
        setAccelsForAction("app.sidebar_toggle", new String[]{"F9"});

        logger.verbose(" > DONE");

        this.onShutdown(this::exit);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.verbose("Processing shutdown handles...");
            logger.verbose("Shutting down websocket communication...");
            webSocketCommunication.shutdown();
            logger.verbose("Successfully processed shutdown handles.");
        }));

        logger.verbose("Initializing core managers...");

        logger.verbose("Initializing task scheduler...");
        logger.verbose(" > create");
        scheduler = new LEDSuiteScheduler();
        logger.verbose(" > DONE");

        logger.verbose("Initializing core heartbeat...");
        logger.verbose(" > create");
        tickingSystem = new TickingSystem();
        logger.verbose(" > DONE");

        logger.verbose("Initializing animation menu manager...");
        logger.verbose(" > create");
        animationMenuManager = new AnimationMenuManager("com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.widgets");
        logger.verbose(" > auto register modules (WIDGET)");
        animationMenuManager.autoRegister();
        logger.verbose(" > DONE");

        logger.verbose("Initializing packet manager...");
        logger.verbose(" > create");
        packetManager = new PacketManager("com.toxicstoxm.LEDSuite.communication.packet_management.packets");
        logger.verbose(" > auto register modules (PACKET)");
        packetManager.autoRegister();
        logger.verbose(" > DONE");

        logger.verbose("Initializing packet handler...");
        logger.verbose(" > create");
        packetReceivedHandler = new PacketReceivedHandler();
        logger.verbose(" > DONE");

        logger.verbose("Initializing upload manager...");
        logger.verbose(" > create");
        uploadManager = new UploadManager();
        logger.verbose(" > DONE");

        // Work in progress
        /*logger.verbose("Initializing authentication manager...");
        logger.verbose(" > create");
        authManager = new AuthManager();
        logger.verbose(" > DONE");*/

        logger.info("Application was successfully initialized!");
    }

    private void initYAJSI() {
        System.out.println(" > create");
        System.out.println(" > configure");
        SettingsManager.configure()
                .setAppName("LEDSuite")
                .setConfigDirectory(Constants.FileSystem.getAppDir())
                .setUpdatingBehaviour(YAMLUpdatingBehaviour.MARK_UNUSED)
                .setUnusedSettingWarning("This setting is currently unused!")
                .setEnableLogBuffer(true)
                .setLogMessageBufferSize(500)
                .setConfigClassesHaveNoArgsConstructor(true);
        settings = new LEDSuiteSettingsBundle();
        SettingsManager.getInstance().registerYAMLConfiguration(settings);
        System.out.println(" > DONE");
    }

    private void initYAJL() {
        System.out.println(" > create");
        System.out.println(" > configure");
        YAJLManager.configure(
                YAJLManagerConfig.builder()
                        .enableYAMLConfig(true)
                        .build()
        );
        logger = Logger.autoConfigureLogger();
        System.out.println(" > DONE");
    }

    @Getter
    private static boolean connecting = false;

    @Getter
    private static long connectionAttempt = -1;

    /**
     * Creates a new websocket client instance and connects it with the websocket server.
     */
    public static boolean startCommunicationSocket() {
        logger.verbose("Starting communication websocket...");
        logger.verbose(" > Computing server address...");
        URI serverAddress;
        try {
            String baseAddress = settings.mainSection.networkSettings.websocketURI;
            logger.debug("Provided server address: {}", baseAddress);
            if (!baseAddress.endsWith("/")) baseAddress = baseAddress + "/";
            baseAddress = baseAddress + "communication";
            serverAddress = new URI(baseAddress);
        } catch (NullPointerException | URISyntaxException e) {
            return false;
        }
        logger.verbose(" > > DONE");

        logger.debug("Final server address: {}", serverAddress);

        connecting = true;
        connectionAttempt = System.currentTimeMillis();
        
        logger.verbose(" > Updating UI to reflect changes...");

        long start = System.currentTimeMillis();
        long timeElapsed = System.currentTimeMillis() - start;
        boolean minDelayReached = false;
        boolean retry = false;
        Boolean result = null;

        window.setServerState(ServerState.CONNECTING);
        GLib.idleAddOnce(() -> window.showAnimationListSpinner(true));

        logger.verbose(" > > DONE");

        if (webSocketCommunication != null) {
            logger.verbose(" > Shutting down previous communication websocket...");
            webSocketCommunication.shutdown();
            logger.verbose(" > > DONE");

        }
        
        logger.verbose(" > Creating new websocket instance...");
        webSocketCommunication = new WebSocketClient(new WebSocketCommunication(), serverAddress);
        logger.verbose(" > > DONE");

        logger.verbose(" > Starting connection loop...");
        while (timeElapsed < Constants.UI.Intervals.CONNECTION_TIMEOUT) {

            if (minDelayReached) {
                if (webSocketCommunication.isConnected()) {
                    logger.verbose(" > Server connected!");
                    //GLib.idleAddOnce(() -> AuthenticationDialog.create().present(window.asApplicationWindow()));
                    result = true;
                    break;
                } else if (retry) {
                    logger.verbose(" > Connection: Stopping current instance");
                    logger.verbose(" > Shutting down communication websocket...");
                    webSocketCommunication.shutdown();
                    logger.verbose(" > Connection retrying...");
                    logger.verbose(" > Creating new websocket instance...");
                    webSocketCommunication = new WebSocketClient(new WebSocketCommunication(), serverAddress);
                    logger.verbose(" > Waiting for timeout...");

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        result = false;
                        break;
                    }
                    logger.verbose(" > Timeout reached!");
                }
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                logger.verbose(" > Connection loop was interrupted!");
                result = false;
                break;
            }

            timeElapsed = System.currentTimeMillis() - start;
            minDelayReached = timeElapsed > Constants.UI.Intervals.MINIMUM_DELAY;
            retry = timeElapsed > Constants.UI.Intervals.RETRY_DELAY;
        }
        
        logger.verbose(" > DONE");

        logger.verbose(" > Updating UI to reflect changes...");
        GLib.idleAddOnce(() -> window.showAnimationListSpinner(false));
        logger.verbose(" > DONE");
        
        connecting = false;

        if (result != null) {
            logger.verbose("Connection: {}", (result ? " success" : " failed"));
            return result;
        }

        logger.verbose("Network connection timed out!");

        return false;
    }

    public static void triggerFileUpload(String filePath, boolean startAnimationAfterUpload) {
        logger.verbose("File upload triggered. File: '{}'; Autostart Animation: {};", filePath, startAnimationAfterUpload);
        try {
            if (webSocketCommunication == null || !webSocketCommunication.isConnected()) {
                window.uploadPageSelect();
                throw new UploadAbortException(() -> {
                    notifyUser(
                            MessageData.builder()
                                    .logger(logger::warn)
                                    .message("Cancelling file upload because communication websocket is not connected!")
                                    .heading("Abort")
                                    .source("File Upload")
                                    .build()
                    );

                });
            }

            URI uploadEndpointPath;

            try {
                String baseAddress = settings.mainSection.networkSettings.websocketURI;
                if (!baseAddress.endsWith("/")) baseAddress = baseAddress + "/";
                baseAddress = baseAddress + "upload";
                uploadEndpointPath = new URI(baseAddress);
            } catch (URISyntaxException e) {
                throw new UploadAbortException(() -> {
                    notifyUser(
                            MessageData.builder()
                                    .logger(logger::warn)
                                    .message("Cancelled file upload because file upload websocket endpoint is unreachable!")
                                    .heading("Abort")
                                    .source("File Upload")
                                    .build()
                    );
                });
            }

            logger.info("Verified upload websocket endpoint at: " + uploadEndpointPath.getPath());

            File fileToUpload;

            try {
                fileToUpload = new File(filePath);
            } catch (NullPointerException e) {
                throw new UploadAbortException(() -> {
                    notifyUser(
                            MessageData.builder()
                                    .logger(logger::warn)
                                    .message("Cancelled file upload because selected file '" + filePath + "' does not exist or isn't a file!")
                                    .heading("Abort")
                                    .source("File Upload")
                                    .build()
                    );
                });
            }

            if (!fileToUpload.exists() || !fileToUpload.isFile()) {
                throw new UploadAbortException(() -> {
                    notifyUser(
                            MessageData.builder()
                                    .logger(logger::warn)
                                    .message("Cancelled file upload because selected file '" + filePath + "' does not exist or isn't a file!")
                                    .heading("Abort")
                                    .source("File Upload")
                                    .build()
                    );
                });
            }

            String checksum;
            byte[] data;
            try {
                data = Files.readAllBytes(Path.of(fileToUpload.getAbsolutePath()));
                byte[] hash = MessageDigest.getInstance("SHA-256").digest(data);
                checksum = new BigInteger(1, hash).toString(16);
            } catch (IOException | NoSuchAlgorithmException | SecurityException e) {
                throw new UploadAbortException(() -> {
                    notifyUser(
                            MessageData.builder()
                                    .logger(logger::warn)
                                    .message("Cancelled file upload because file couldn't be loaded or checksum failed!")
                                    .heading("Abort")
                                    .source("File Upload")
                                    .build()
                    );
                });
            } catch (OutOfMemoryError e) {
                throw new UploadAbortException(() -> {
                    notifyUser(
                            MessageData.builder()
                                    .logger(logger::warn)
                                    .message("Cancelled file upload because the specified file is too large!")
                                    .heading("Abort")
                                    .source("File Upload")
                                    .build()
                    );
                });
            }

            String uploadSessionID = String.valueOf(UUID.randomUUID());
            AtomicReference<String> fileName = new AtomicReference<>(StringFormatter.getFileNameFromPath(filePath));
            if (uploadSessionID == null) {
                throw new UploadAbortException(() -> {
                    LEDSuiteApplication.handleError(
                            ErrorData.builder()
                                    .message(Translations.getText("Cancelled file upload because upload session id was null! This should be reported!"))
                                    .build()
                    );
                });
            }

            uploadManager.setPending(fileName.get(), uploadPermitted -> {
                if (uploadPermitted) {
                    uploadFile(fileToUpload, startAnimationAfterUpload, uploadSessionID, uploadEndpointPath, checksum);
                } else {
                    GLib.idleAddOnce(() -> {
                        AlertDialog.ResponseCallback cb = getResponseCallback(fileName, uploadSessionID, checksum, result -> {
                            if (result) uploadManager.removePending(fileName.get());
                            GLib.idleAddOnce(() -> LEDSuiteApplication.getWindow().uploadCompleted(result));
                        });

                        window.displayFileCollisionDialog(cb, Translations.getText("Animation with name '$' already exists.", fileName.get()));
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
            LEDSuiteApplication.getWindow().uploadCompleted(false);
        }
    }

    /**
     * Constructs a response callback for handling file upload collisions in the {@link RenameDialog}.
     * This callback manages the user's response to file name collisions during an upload process
     * by offering options to rename, overwrite, or cancel the upload.
     * <p>
     * The callback performs the following:
     * <ul>
     *   <li>If the user cancels the upload, an {@link UploadAbortException} is thrown.</li>
     *   <li>If the user chooses to rename, a {@link RenameDialog} is presented to input a new file name.</li>
     *   <li>If the user chooses to overwrite, an {@link OverwriteConfirmationDialog} is presented for final confirmation.</li>
     * </ul>
     *
     * @param fileName              A reference to the current file name involved in the upload process.
     * @param uploadSessionID       The session ID of the uploads WebSocket endpoint.
     * @param checksum              The checksum of the uploaded file for verification purposes.
     * @param uploadFinishCallback  The callback to be invoked once the upload process is completed.
     * @return                      The constructed {@link org.gnome.adw.AlertDialog.ResponseCallback}.
     *
     * @throws UploadAbortException if the user cancels the upload.
     */
    @Contract(pure = true)
    private static AlertDialog.@NotNull ResponseCallback getResponseCallback(AtomicReference<String> fileName, String uploadSessionID, String checksum, UpdateCallback<Boolean> uploadFinishCallback) {
        return response -> {
            try {
                logger.info("File collision dialog response result: {}", response);

                switch (response) {
                    case "cancel" -> throw new UploadAbortException(() -> logger.info("File upload was cancelled by the user!"));
                    case "rename" -> {
                        logger.info("Rename selected.");
                        var renameDialog = RenameDialog.create(fileName.get());

                        renameDialog.onResponse(renameResponse -> GLib.idleAddOnce(() -> {
                            String newName = renameDialog.getNewName();

                            if (renameResponse.equals("cancel")) {
                                window.displayFileCollisionDialog(
                                        getResponseCallback(fileName, uploadSessionID, checksum, uploadFinishCallback),
                                        Translations.getText("Animation with name '$' already exists.", fileName.get())
                                );
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

                        GLib.idleAddOnce(() -> renameDialog.present(window.asApplicationWindow()));
                    }
                    case "overwrite" -> {

                        var confirmationDialog = OverwriteConfirmationDialog.create();

                        confirmationDialog.onResponse(confirmationResponse -> GLib.idleAddOnce(() -> {
                            if (confirmationResponse.equals("cancel")) {
                                window.displayFileCollisionDialog(
                                        getResponseCallback(fileName, uploadSessionID, checksum, uploadFinishCallback),
                                        Translations.getText("Animation with name '$' already exists.", fileName.get())
                                );
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

                        GLib.idleAddOnce(() -> confirmationDialog.present(window.asApplicationWindow()));
                    }
                }
            } catch (UploadAbortException e) {
                e.printErrorMessage();
                window.uploadCompleted(false);
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
     */
    public static void uploadFile(@NotNull File fileToUpload, boolean startAnimationAfterUpload, String uploadSessionID, URI uploadEndpointPath, String checksum) {

        logger.info("Uploading file '{} to '{}'!", fileToUpload.getAbsolutePath(), uploadEndpointPath);

        String filePath = fileToUpload.getAbsolutePath();

        final long fileSize = fileToUpload.length();
        final int packet_size = settings.mainSection.networkSettings.packetSizeBytes;
        final long startTime = System.currentTimeMillis();

        AtomicLong transferredSize = new AtomicLong(0);

        String animationName = StringFormatter.getFileNameFromPath(filePath);
        AtomicLong speedAverage = new AtomicLong(-1);
        AtomicLong speedMeasurementCount = new AtomicLong(1);
        AtomicBoolean finished = new AtomicBoolean(false);
        AtomicLong lastUploadStatisticsUpdate = new AtomicLong(System.currentTimeMillis());

        WebSocketClient uploadEndpoint = new WebSocketClient(
                WebSocketUpload.builder()
                        .ready(true)
                        .sessionID(uploadSessionID)
                        .connectionNotifyCallback(() -> {
                            logger.verbose("Connected to upload websocket endpoint at: {}", uploadEndpointPath.getPath());
                            logger.verbose("Metadata:");
                            logger.verbose(" > Upload session ID: {}", uploadSessionID);
                            logger.verbose(" > Filename: {}", animationName);
                            logger.verbose(" > Checksum: {}", checksum);
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

                            logger.debug("Sent packet to server. [ID: {}; Size: {}; Last-Packet: {}]", speedMeasurementCount.get(), transferredBytes, isLastPacket);

                            long now = System.currentTimeMillis();
                            long timeSinceLastStatsUpdate = now - lastUploadStatisticsUpdate.get();
                            if (timeSinceLastStatsUpdate > 1000) {
                                long estimatedMillisecondsRemaining = (speedAverage.get() > 0)
                                        ? ((fileSize - transferredSize.get()) / speedAverage.get()) * 1000
                                        : Long.MAX_VALUE;  // Handle division by zero for safety

                                window.setUploadStatistics(
                                        UploadStatistics.builder()
                                                .bytesPerSecond(speedAverage.get())
                                                .millisecondsRemaining(estimatedMillisecondsRemaining)
                                                .build()
                                );

                                lastUploadStatisticsUpdate.set(now);
                            }

                            if (isLastPacket) {
                                long totalTimeElapsed = System.currentTimeMillis() - startTime;

                                logger.verbose("File upload finished:");
                                logger.verbose(" > File: {}", animationName);
                                logger.verbose(" > Transferred Bytes: {}", transferredSize.get());
                                logger.verbose(" > Transferred Packets (<={}KB): {}", packet_size / 1024, speedMeasurementCount.get());
                                logger.verbose(" > Average Speed: {}", StringFormatter.formatSpeed(speedAverage.get()));
                                logger.verbose(" > Total time elapsed: {}", StringFormatter.formatDuration(totalTimeElapsed));

                                finished.set(true);
                            }
                        })
                        .build(), uploadEndpointPath
        );

        logger.debug("Splitting file into chunks:");
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

                logger.debug("Enqueueing BinaryPacket [ID: {}; isLast: {}; Data Hash: {}]", cnt, isLastChunk, Arrays.hashCode(chunk));

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

            logger.verbose("Upload preparation complete!");
        } catch (IOException e) {
            logger.warn("Error during upload preparation: {}", e.getMessage());
            window.uploadCompleted(false);
            return;
        }

        new LEDSuiteRunnable() {
            @Override
            public void run() {
                while (!finished.get()) {
                    try {
                        Thread.onSpinWait();
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        logger.warn("Upload finish callback was interrupted!");
                        window.uploadCompleted(false);
                        cancel();
                    }
                }

                logger.info("New animation was successfully created from upload file '{}'!", animationName);

                logger.verbose("Updating UI to reflect changes...");
                window.uploadCompleted(true);
                window.uploadFinished();
                logger.verbose(" > DONE");

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

    public static final AtomicBoolean errorFlag = new AtomicBoolean(false);

    /**
     * Handles an error by displaying an error dialog with a custom message, heading, and log area.
     * If a parent application window is available, the error dialog will be presented to the user.
     * The error message will also be logged in the specified log area.
     *
     * @param errorData contains all necessary variables for displaying an error to the user.
     */
    public static void handleError(@NotNull ErrorData errorData) {
        ApplicationWindow parent = window.asApplicationWindow();
        String message = errorData.getMessage();
        if (parent != null && !errorFlag.get()) {
            errorFlag.set(true);
            ErrorAlertDialog.builder()
                    .errorMessage(message)
                    .heading(errorData.getHeading())
                    .enableReporting(errorData.isEnableReporting())
                    .build()
                    .present(parent);
        }
        if (errorData.isLog() && message != null) logger.error(errorData.getMessage());
    }

    /**
     * Displays a message to the user using a {@link MessageDialog}.
     * <p>
     * If a logger interface is specified, the message gets also logged to the console.
     *
     * @param messageData contains all necessary variables for processing the message
     */
    public static void notifyUser(@NotNull MessageData messageData) {
        MessageDialog.builder()
                .messageData(messageData)
                .build().present(getWindow().asApplicationWindow());
        if (messageData.logger() != null) {
            messageData.logger().log(messageData.message());
        }
    }

    /**
     * Called when the application window is closed. <br>
     * Saves all settings and exits.
     */
    public void exit() {
        if (!webSocketCommunication.isClosed()) webSocketCommunication.shutdown();
        logger.info("Application: EXIT");
        logger.info("Goodbye!");
        SettingsManager.getInstance().save();
        System.exit(0);
    }

    /**
     * Called when the application starts. From {@link Application} <br>
     * Creates a new instance of the application window and presents it.
     */
    @Override
    public void activate() {
        logger.verbose("Initializing application window...");
        logger.verbose(" > create");
        Window win = this.getActiveWindow();
        if (win == null)
            win = LEDSuiteWindow.create(this);
        window = (LEDSuiteWindow) win;
        logger.verbose(" > present");
        win.present();
        logger.verbose(" > DONE");

        logger.verbose("Initializing websocket connection (async)...");
        new LEDSuiteRunnable() {
            @Override
            public void run() {
                logger.verbose("Initializing websocket connection (sync)...");
                startCommunicationSocket();
                logger.verbose("Initialized websocket connection!");
                logger.verbose(" > DONE (sync)");
            }
        }.runTaskAsynchronously();
        logger.verbose(" > DONE (not sync)");
    }
}
