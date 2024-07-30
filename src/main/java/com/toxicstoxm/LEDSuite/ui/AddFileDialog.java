package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.LEDSuite;
import com.toxicstoxm.LEDSuite.communication.files.AllowedFileTypes;
import com.toxicstoxm.LEDSuite.communication.network.Networking;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteGuiRunnable;
import com.toxicstoxm.LEDSuite.time.TimeManager;
import com.toxicstoxm.LEDSuite.yaml_factory.YAMLMessage;
import com.toxicstoxm.LEDSuite.yaml_factory.YAMLSerializer;
import io.github.jwharm.javagi.base.GErrorException;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.gnome.adw.*;
import org.gnome.gio.File;
import org.gnome.gio.FileQueryInfoFlags;
import org.gnome.gio.FileType;
import org.gnome.gtk.*;

import java.net.URLConnection;

/**
 * The {@code AddFileDialog} class represents a dialog for uploading files, allowing users to select
 * and upload files to a server. It provides options to view upload statistics, set preferences for
 * automatic playback after upload, and handles file validation and uploading operations.
 * <p>
 * This class is a part of the user interface for the LEDSuite application and extends
 * {@link PreferencesPage} to integrate with the application's settings and preferences system.
 * </p>
 *
 * @since 1.0.0
 */
public class AddFileDialog extends PreferencesPage {

    /**
     * The {@code VarPool} class serves as a global storage for file metadata.
     * <p>
     * It holds static fields for file path and file name, and an initialization flag.
     * </p>
     *
     * @since 1.0.0
     */
    public static class VarPool {
        public static boolean init = false;
        public static String filePath = null;
        public static String fileName = null;
    }

    private final Button uploadButton;
    private boolean uploading = false;
    private final Spinner spinner;
    private String filePath = null;
    private String fileName = null;
    private final ExpanderRow statsRow;
    private final ActionRow speed;
    private final ActionRow eta;

    /**
     * Constructs a new {@code AddFileDialog} and initializes its components.
     * <p>
     * This constructor sets up the user interface elements, including file selection, upload button,
     * upload statistics, and integrates with the LEDSuite settings for file handling preferences.
     * </p>
     */
    public AddFileDialog() {

        if (VarPool.init) {
            this.fileName = VarPool.fileName;
            this.filePath = VarPool.filePath;
        }

        // Create a preferences group for file selection
        var file = PreferencesGroup.builder()
                .setTitle("File")
                .build();

        // Create an EntryRow for displaying the selected file path, initially empty
        var filenameRow = ActionRow.builder()
                .setTitle("Selected File")
                .setSubtitle(filePath == null ? "N/A" : fileName)
                .setUseMarkup(false)
                .build();

        // Create a button for opening the file dialog, with an icon indicating file sending
        var fileSelButton = Button.builder()
                .setIconName("document-send-symbolic")
                .build();

        var filter = FileFilter.builder()
                .setSuffixes(new String[]{"so"})
                .setMimeTypes(new String[]{"image/*", "video/*"})
                .setName("Animations")
                .build();

        // Create the file dialog for selecting files
        var fileSel = FileDialog.builder()
                .setTitle("Pick file to upload")
                .setModal(true)
                .setInitialFolder(
                        File.newForPath(LEDSuite.settings.getSelectionDir())
                )
                .setFilters(FilterListModel.builder().setFilter(filter).build())
                .setDefaultFilter(filter)
                .build();

        // Set the onClicked event for the fileSelButton to open the file dialog
        fileSelButton.onClicked(() -> {
            LEDSuite.logger.debug("Clicked file select button: opening new open file dialog");
            // Open the file dialog with a callback to handle the user's file selection

            fileSel.setInitialFolder(
                    File.newForPath(LEDSuite.settings.getSelectionDir())
            );

            fileSel.open(LEDSuite.mainWindow, null, (_, result, _) -> {
                try {
                    // Finish the file selection operation and get the selected file
                    var selectedFile = fileSel.openFinish(result);
                    if (selectedFile != null) {
                        String filePath = selectedFile.getPath();
                        // Update the filenameRow text with the selected file path
                        fileDialogProcessResult(selectedFile, filenameRow);
                        LEDSuite.logger.debug("Selected file: " + filePath);
                    }
                } catch (GErrorException e) {
                    LEDSuite.logger.debug("User canceled open file dialog!");
                }
            });
        });

        fileSelButton.setSizeRequest(40, 40);

        var clamp = Clamp.builder().setMaximumSize(40).setOrientation(Orientation.VERTICAL).setTighteningThreshold(40).build();
        clamp.setChild(fileSelButton);

        // Add the file selection button as a suffix to the filenameRow
        filenameRow.addSuffix(clamp);

        // Add the filenameRow to the file preferences group
        file.add(filenameRow);

        final boolean[] toggled = new boolean[]{LEDSuite.settings.isAutoPlayAfterUpload()};

        var autoPlay = SwitchRow.builder()
                .setActive(toggled[0])
                .setTitle("Start animation after upload")
                .setTooltipText("If true, the animation is automatically started after the upload finishes")
                .build();

        autoPlay.getActivatableWidget().onStateFlagsChanged(_ -> {
            boolean active = autoPlay.getActive();
            if (toggled[0] != active) {
                LEDSuite.settings.setAutoPlayAfterUpload(active);
                LEDSuite.logger.debug("AutoPlayAfterUpload -> " + active);
                toggled[0] = active;
            }
        });

        file.add(autoPlay);

        statsRow = ExpanderRow.builder()
                .setTitle("Upload Statistics")
                .setExpanded(false)
                .setActivatable(false)
                .build();

        speed = ActionRow.builder()
                .setTitle("Speed")
                .setSubtitle("N/A")
                .build();
        eta = ActionRow.builder()
                .setTitle("ETA")
                .setSubtitle("N/A")
                .build();

        statsRow.addRow(speed);
        statsRow.addRow(eta);

        file.add(statsRow);

        uploadButton = Button.builder()
                .setCssClasses(new String[]{"suggested-action", "pill"})
                .build();

        final long[] last = new long[]{System.currentTimeMillis()};
        long cooldown = 500;

        uploadButton.onClicked(() -> {
            if (!(System.currentTimeMillis() - last[0] >= cooldown)) {
                last[0] = System.currentTimeMillis();
                return;
            }
            if (uploading) return;

            upload((error, fileName) -> {
                if (!error && LEDSuite.settings.isAutoPlayAfterUpload()) {
                    try {
                        Networking.Communication
                                .sendYAMLDefaultHost(
                                        YAMLMessage.builder()
                                                .setPacketType(YAMLMessage.PACKET_TYPE.request)
                                                .setRequestType(YAMLMessage.REQUEST_TYPE.play)
                                                .setRequestFile(fileName)
                                                .build(),
                                        success -> {
                                            if (!success) displayPlayRequestError(
                                                    new Networking.ServerCommunicationException(
                                                            "Failed to communicate with server! Possible cause: Invalid or no response!"
                                                    ),
                                                    fileName
                                            );
                                        }
                                );
                    } catch (YAMLSerializer.YAMLException | ConfigurationException e) {
                        displayPlayRequestError(e, fileName);

                    }
                }
            });

            last[0] = System.currentTimeMillis();

        });

        spinner = Spinner.builder().setSpinning(true).build();

        var buttonBox = Box.builder().setOrientation(Orientation.HORIZONTAL).setSpacing(5).build();
        buttonBox.append(Label.builder().setLabel("Upload").build());
        buttonBox.append(spinner);
        var clamp0 = Clamp.builder()
                .setMaximumSize(70)
                .setTighteningThreshold(70)
                .setOrientation(Orientation.HORIZONTAL)
                .setChild(buttonBox)
                .build();

        uploadButton.setChild(clamp0);
        spinner.setVisible(false);

        var clamp1 = Clamp.builder()
                .setMarginTop(25)
                .setMaximumSize(120)
                .setTighteningThreshold(120)
                .setOrientation(Orientation.HORIZONTAL)
                .setChild(uploadButton)
                .build();

        file.add(clamp1);

        // Add the file preferences group to the dialog
        add(file);
    }

    /**
     * Displays an error message when the play request for an uploaded file fails.
     * <p>
     * This method logs the error and shows a toast notification to inform the user about the failure.
     * </p>
     *
     * @param e       The exception that occurred.
     * @param fileName The name of the file for which the play request failed.
     * @since 1.0.0
     */
    private void displayPlayRequestError(Exception e, String fileName) {
        LEDSuite.logger.error("Failed to send play request for uploaded file! File name : '" + fileName + "' File path: '" + filePath + "'");
        LEDSuite.mainWindow.toastOverlay.addToast(
                Toast.builder()
                        .setTitle("Error: Failed to autostart animation!")
                        .build()
        );
        LEDSuite.logger.error(e);
    }

    /**
     * Processes the result of the file dialog selection.
     * <p>
     * Validates the file type and updates the relevant fields and UI components with the selected file's information.
     * </p>
     *
     * @param result       The selected file.
     * @param filenameRow  The {@link ActionRow} to update with the selected file's name.
     * @since 1.0.0
     */
    private void fileDialogProcessResult(File result, ActionRow filenameRow) {
        FileType fType = result.queryFileType(FileQueryInfoFlags.NONE, null);
        String path = result.getPath();
        if (fType == FileType.REGULAR && path != null) {
            String fileName = path.substring(path.lastIndexOf("/") + 1);
            if (checkFileType(fileName)) {
                filePath = path;
                this.fileName = fileName;
                VarPool.fileName = fileName;
                filenameRow.setSubtitle(fileName);
                VarPool.filePath = filePath;
                VarPool.init = true;
                File parent = result.getParent();
                String parentPath = parent.getPath();
                if (parentPath != null &&
                        !parentPath.isEmpty() &&
                        parent.queryFileType(
                                FileQueryInfoFlags.NONE, null
                        ) == FileType.DIRECTORY
                ) {
                    LEDSuite.logger.debug("Updating default selection dir: " + LEDSuite.settings.getSelectionDir() + " -> " + parentPath);
                    LEDSuite.settings.setSelectionDir(parentPath);
                }
                LEDSuite.logger.debug("Valid file type! " + path);
                return;
            }
        }
        LEDSuite.logger.error("Invalid file type selected! " + path);
        LEDSuite.logger.warn("Only Image, Video and Shared Library files are allowed!");
        LEDSuite.mainWindow.toastOverlay.addToast(
                Toast.builder().setTitle("Error: Invalid file type selected!").build()
        );
    }

    /**
     * Checks if the given file name has a valid file type based on its suffix or MIME type.
     * <p>
     * Valid types are restricted to certain suffixes or MIME types related to images, videos, and shared libraries.
     * </p>
     *
     * @param fileName The name of the file to check.
     * @return {@code true} if the file type is allowed; {@code false} otherwise.
     * @since 1.0.0
     */
    private boolean checkFileType(String fileName) {
        String mimeType = URLConnection.guessContentTypeFromName(fileName);
        String suffix = fileName.substring(
                fileName.lastIndexOf(".") + 1
        );
        return  suffix.equalsIgnoreCase("SO") ||
                mimeType == null ? AllowedFileTypes.isAllowed(suffix) :
                (
                        mimeType.contains("image/") ||
                                mimeType.contains("video/")
                );
    }

    /**
     * Callback interface for handling the result of the upload operation.
     * <p>
     * This interface is used to notify when the upload process has completed, either with or without errors.
     * </p>
     *
     * @since 1.0.0
     */
    private interface FinishCallback {
        void onFinish(boolean error, String fileName);
    }

    /**
     * Initiates the file upload process.
     * <p>
     * This method manages the upload process, including updating the UI with progress information and handling
     * errors or completion events. It uses a progress tracker to update the UI with upload statistics.
     * </p>
     *
     * @param callback The callback to invoke once the upload process is finished.
     * @since 1.0.0
     */
    private void upload(FinishCallback callback) {
        LEDSuite.logger.debug("Triggered animation upload!");
        if (filePath == null || filePath.isEmpty() || !new java.io.File(filePath).exists()) {
            LEDSuite.logger.debug("File path is null or empty.");
            resetUI(1000, true, true, callback);
            return;
        }
        spinner.setVisible(true);
        uploadButton.setCssClasses(new String[]{"regular", "pill"});
        uploading = true;

        LEDSuite.logger.debug("Requesting send with stats tracking!");
        Networking.Communication.ProgressTracker progressTracker = new Networking.Communication.ProgressTracker();

        long start = System.currentTimeMillis();
        long timeout = 2000; // time in ms until sending operation times out
        int resetDelay = 500; // time in ms until reset is triggered
        final boolean[] cancelled = {false};

        new LEDSuiteGuiRunnable() {
            @Override
            public void processGui() {
                if (!cancelled[0] && progressTracker.isUpdated()) {
                    cancelled[0] = true;
                    LEDSuite.mainWindow.progressBar.setFraction(0.0);
                    LEDSuite.mainWindow.rootView.setRevealBottomBars(true);
                    statsRow.setExpanded(true);
                    new LEDSuiteGuiRunnable() {
                        @Override
                        public void processGui() {
                            boolean error = progressTracker.isError();
                            String speedNew = calculateNewSpeed(progressTracker.getSpeedInBytes());
                            String etaNew = progressTracker.getEta();
                            double progressbar = progressTracker.getProgressPercentage();
                            if (error || progressTracker.getProgressPercentage() >= 1.0) {
                                if (!error) {
                                    if (!speed.getSubtitle().equals(speedNew)) speed.setSubtitle(speedNew);
                                    if (!eta.getSubtitle().equals(etaNew)) eta.setSubtitle(etaNew);
                                    if (progressbar - LEDSuite.mainWindow.progressBar.getFraction() > 0.001) LEDSuite.mainWindow.progressBar.setFraction(progressTracker.getProgressPercentage());
                                }
                                resetUI(1000, error, false, callback);
                                cancel();
                            }
                            if (!speed.getSubtitle().equals(speedNew)) speed.setSubtitle(speedNew);
                            if (!eta.getSubtitle().equals(etaNew)) eta.setSubtitle(etaNew);
                            if (progressbar - LEDSuite.mainWindow.progressBar.getFraction() > 0.001) LEDSuite.mainWindow.progressBar.setFraction(progressTracker.getProgressPercentage());
                        }
                    }.runTaskTimerAsynchronously(0, 100);
                    cancel();

                } else if (System.currentTimeMillis() - start > timeout && !cancelled[0] && !progressTracker.isStarted()) {
                    resetUI(resetDelay, false, false, callback);
                    cancel();
                } else if (progressTracker.isError() && !cancelled[0]) {
                    resetUI(resetDelay, true, false, callback);
                    cancel();
                }
            }
        }.runTaskTimerAsynchronously(10, 10);

        new LEDSuiteGuiRunnable() {
            @Override
            public void processGui() {
                Networking.Communication.sendFileDefaultHost(filePath, progressTracker);
                TimeManager.setTimeTracker("animations", System.currentTimeMillis() - 10000);
            }
        }.runTaskAsynchronously();
    }

    /**
     * Resets the user interface after the upload operation completes or fails.
     * <p>
     * This method hides the spinner, resets UI elements, and displays error messages if necessary.
     * It also invokes the provided callback to indicate the completion of the upload process.
     * </p>
     *
     * @param delayInMillis The delay before resetting the UI.
     * @param error          {@code true} if an error occurred during the upload; {@code false} otherwise.
     * @param invalidPath    {@code true} if the file path was invalid; {@code false} otherwise.
     * @param callback       The callback to invoke once the UI has been reset.
     * @since 1.0.0
     */
    private void resetUI(int delayInMillis, boolean error, boolean invalidPath, FinishCallback callback) {
        new LEDSuiteGuiRunnable() {
            @Override
            public void processGui() {
                LEDSuite.mainWindow.rootView.setRevealBottomBars(false);
                uploadButton.setCssClasses(new String[]{"suggested-action", "pill"});
                uploading = false;
                spinner.setVisible(false);
                statsRow.setExpanded(false);
                speed.setSubtitle("N/A");
                eta.setSubtitle("N/A");
                LEDSuite.mainWindow.progressBar.setFraction(0.0);
                if (error) {
                    LEDSuite.mainWindow.toastOverlay.addToast(
                            Toast.builder()
                                    .setTitle("Error:" +
                                            (invalidPath ?
                                                    " Invalid File for path: " +
                                                            (filePath == null || filePath.isEmpty() ?
                                                                    "N/A" : filePath) :
                                                    " Failed to send file!")
                                    )
                                    .build()
                    );

                    uploadButton.setCssClasses(new String[]{"destructive-action", "pill"});
                    new LEDSuiteGuiRunnable() {
                        @Override
                        public void processGui() {
                            uploadButton.setCssClasses(new String[]{"suggested-action", "pill"});
                        }
                    }.runTaskLaterAsynchronously(1000);
                }

                callback.onFinish(error, fileName);

            }
        }.runTaskLaterAsynchronously(delayInMillis);
    }

    /**
     * Calculates the speed of the upload operation in a human-readable format.
     * <p>
     * The speed is converted from bytes per second to a more readable format such as KB/S, MB/S, or GB/S.
     * </p>
     *
     * @param bytesPerSec The speed of the upload in bytes per second.
     * @return A string representing the speed in a human-readable format.
     * @since 1.0.0
     */
    private String calculateNewSpeed(double bytesPerSec) {

        StringBuilder sb = new StringBuilder();

        int decimals = 2;

        double change = Math.pow(10,  decimals);

        double bitsPerSecond = (double) Math.round(
                (bytesPerSec * Math.pow(2, 3)) * change) / change;

        double kbPerSecond = (double) Math.round(
                (bytesPerSec / Math.pow(2, 10)) * change) / change;

        double mbPerSecond = (double) Math.round(
                (bytesPerSec / Math.pow(2, 20)) * change) / change;

        double gbPerSecond = (double) Math.round(
                (bytesPerSec / Math.pow(2, 30)) * change) / change;

        if (gbPerSecond >= 1) sb.append(gbPerSecond).append(" GB/S");
        else if (mbPerSecond >= 1) sb.append(mbPerSecond).append(" MB/S");
        else if (kbPerSecond >= 1) sb.append(kbPerSecond).append(" KB/S");
        else if (bytesPerSec >= 1) sb.append(bytesPerSec).append( " B/S");
        else if (bitsPerSecond >= 1) sb.append(bitsPerSecond).append(" b/S");
        else sb.append("N/A");

        return sb.toString();
    }
}
