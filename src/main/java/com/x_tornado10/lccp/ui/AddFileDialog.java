package com.x_tornado10.lccp.ui;

import com.x_tornado10.lccp.LCCP;
import com.x_tornado10.lccp.task_scheduler.LCCPRunnable;
import com.x_tornado10.lccp.util.ALLOWED_FILE_TYPES;
import com.x_tornado10.lccp.util.network.Networking;
import com.x_tornado10.lccp.yaml_factory.YAMLAssembly;
import com.x_tornado10.lccp.yaml_factory.YAMLMessage;
import io.github.jwharm.javagi.base.GErrorException;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.gnome.adw.*;
import org.gnome.gio.*;
import org.gnome.gtk.*;

import java.net.URLConnection;

public class AddFileDialog extends PreferencesPage {

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
    private final ActionRow statsRow;
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
        var pathRow = ActionRow.builder()
                .setTitle("Path")
                .setSubtitle(filePath == null ? "N/A" : filePath)
                .setUseMarkup(false)
                .build();

        // Create a button for opening the file dialog, with an icon indicating file sending
        var fileSelButton = Button.builder()
                .setIconName("document-send-symbolic")
                .build();

        var filter = FileFilter.builder()
                .setSuffixes(new String[]{"so"})
                .setMimeTypes(new String[]{"image/*","video/*"})
                .setName("Animations")
                .build();

        // Create the file dialog for selecting files
        var fileSel = FileDialog.builder()
                .setTitle("Pick file to upload")
                .setModal(true)
                .setInitialFolder(
                        File.newForPath(LCCP.settings.getSelectionDir())
                )
                .setFilters(FilterListModel.builder().setFilter(filter).build())
                .setDefaultFilter(filter)
                .build();

        // Set the onClicked event for the fileSelButton to open the file dialog
        fileSelButton.onClicked(() -> {
            LCCP.logger.debug("Clicked file select button: opening new open file dialog");
            // Open the file dialog with a callback to handle the user's file selection

            fileSel.setInitialFolder(
                    File.newForPath(LCCP.settings.getSelectionDir())
            );

            fileSel.open(LCCP.mainWindow, null, (_, result, _) -> {
                try {
                    // Finish the file selection operation and get the selected file
                    var selectedFile = fileSel.openFinish(result);
                    if (selectedFile != null) {
                        String filePath = selectedFile.getPath();
                        // Update the pathRow text with the selected file path
                        fileDialogProcessResult(selectedFile, pathRow);
                        LCCP.logger.debug("Selected file: " + filePath);
                    }
                } catch (GErrorException _) {
                    LCCP.logger.debug("User canceled open file dialog!");
                }
            });
        });

        fileSelButton.setSizeRequest(40, 40);

        var clamp = Clamp.builder().setMaximumSize(40).setOrientation(Orientation.VERTICAL).setTighteningThreshold(40).build();
        clamp.setChild(fileSelButton);

        // Add the file selection button as a suffix to the pathRow
        pathRow.addSuffix(clamp);

        // Add the pathRow to the file preferences group
        file.add(pathRow);

        final boolean[] toggled = new boolean[]{LCCP.settings.isAutoPlayAfterUpload()};

        var autoPlay = SwitchRow.builder()
                .setActive(toggled[0])
                .setTitle("Start animation after upload")
                .setTooltipText("If true, the animation is automatically started after the upload finishes")
                .build();

        autoPlay.getActivatableWidget().onStateFlagsChanged(_ -> {
            boolean active = autoPlay.getActive();
            if (toggled[0] != active) {
                LCCP.settings.setAutoPlayAfterUpload(active);
                LCCP.logger.debug("AutoPlayAfterUpload -> " + active);
                toggled[0] = active;
            }
        });

        file.add(autoPlay);

        statsRow = ActionRow.builder()
                .setTitle("Upload statistics")
                .setSubtitle("Upload speed: N/A - ETA: N/A")
                .build();

        statsRow.setVisible(false);

        file.add(statsRow);

        uploadButton = Button.builder()
                .setCssClasses(new String[]{"suggested-action", "pill"})
                        .build();

        uploadButton.onClicked(() -> {
            if (uploading) return;
            new LCCPRunnable() {
                @Override
                public void run() {
                    upload((error, fileName) -> {
                        if (!error && LCCP.settings.isAutoPlayAfterUpload()) {
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
                            } catch (YAMLAssembly.YAMLException | ConfigurationException e) {
                                displayPlayRequestError(e, fileName);

                            }
                        }
                    });
                }
            }.runTaskAsynchronously();
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

    private void displayPlayRequestError(Exception e, String fileName) {
        LCCP.logger.error("Failed to send play request for uploaded file! File name : '" + fileName + "' File path: '" + filePath + "'");
        LCCP.mainWindow.toastOverlay.addToast(
                Toast.builder()
                        .setTitle("Error: Failed to autostart animation!")
                        .build()
        );
        LCCP.logger.error(e);
    }

    private void fileDialogProcessResult(File result, ActionRow pathRow) {
        FileType fType = result.queryFileType(FileQueryInfoFlags.NONE, null);
        String path = result.getPath();
        if (fType == FileType.REGULAR && path != null) {
            String fileName = path.substring(path.lastIndexOf("/") + 1);
            if (checkFileType(fileName)) {
                filePath = path;
                this.fileName = fileName;
                VarPool.fileName = fileName;
                pathRow.setSubtitle(filePath);
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
                    LCCP.logger.debug("Updating default selection dir: " + LCCP.settings.getSelectionDir() + " -> " + parentPath);
                    LCCP.settings.setSelectionDir(parentPath);
                }
                LCCP.logger.debug("Valid file type! " + path);
                return;
            }
        }
        LCCP.logger.error("Invalid file type selected! " + path);
        LCCP.logger.warn("Only Image, Video and Shared Library files are allowed!");
        LCCP.mainWindow.toastOverlay.addToast(
                Toast.builder().setTitle("Error: Invalid file type selected!").build()
        );
    }

    private boolean checkFileType(String fileName) {
        String mimeType = URLConnection.guessContentTypeFromName(fileName);
        String suffix = fileName.substring(
                fileName.lastIndexOf(".") + 1
        );
        return  suffix.equalsIgnoreCase("SO") ||
                mimeType == null ? ALLOWED_FILE_TYPES.isALLOWED(suffix) :
                (
                mimeType.contains("image/") ||
                mimeType.contains("video/")
        );
    }

    private interface FinishCallback {
        void onFinish(boolean error, String fileName);
    }

    private void upload(FinishCallback callback) {
        LCCP.logger.debug("Triggered animation upload!");
        if (filePath == null || filePath.isEmpty() || !new java.io.File(filePath).exists()) {
            LCCP.logger.debug("File path is null or empty.");
            resetUI(1000, true, true, callback);
            return;
        }
        spinner.setVisible(true);
        uploadButton.setCssClasses(new String[]{"regular", "pill"});
        uploadButton.emitRealize();
        uploading = true;

        LCCP.logger.debug("Requesting send with stats tracking!");
        Networking.Communication.ProgressTracker progressTracker = new Networking.Communication.ProgressTracker();
        Networking.Communication.sendFileDefaultHost(filePath, progressTracker);

        long start = System.currentTimeMillis();

        new LCCPRunnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() - start > 500) {
                    resetUI(1000, false, false, callback);
                    cancel();
                }
                if (progressTracker.isUpdated()) {
                    LCCP.logger.debug("Changing button style!");
                    LCCP.mainWindow.rootView.setRevealBottomBars(true);
                    statsRow.setVisible(true);
                    new LCCPRunnable() {
                        @Override
                        public void run() {
                            boolean error = progressTracker.isError();
                            if (error || progressTracker.getProgressPercentage() >= 100) {
                                resetUI(1000, error, false, callback);
                                cancel();
                            }
                            statsRow.setSubtitle("Upload speed: " + progressTracker.getSpeedInMegabytes() + "MB/S - ETA: " + progressTracker.getEta());
                            LCCP.mainWindow.progressBar.setFraction(progressTracker.getProgressPercentage() / 100);
                        }
                    }.runTaskTimerAsynchronously(100, 10);
                } else if (progressTracker.isError()){
                    resetUI(1000, true, false, callback);
                    cancel();
                }
            }
        }.runTaskTimerAsynchronously(0, 10);

    }

    private void resetUI(int delayInMillis, boolean error, boolean invalidPath, FinishCallback callback) {
        new LCCPRunnable() {
            @Override
            public void run() {
                LCCP.mainWindow.rootView.setRevealBottomBars(false);
                uploadButton.setCssClasses(new String[]{"suggested-action", "pill"});
                uploading = false;
                spinner.setVisible(false);
                uploadButton.emitRealize();
                statsRow.setSubtitle("Upload speed: N/A - ETA: N/A");
                statsRow.setVisible(false);
                LCCP.mainWindow.progressBar.setFraction(0.0);
                if (error) {
                    LCCP.mainWindow.toastOverlay.addToast(
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
                    new LCCPRunnable() {
                        @Override
                        public void run() {
                            uploadButton.setCssClasses(new String[]{"suggested-action", "pill"});
                        }
                    }.runTaskLaterAsynchronously(1000);
                }

                callback.onFinish(error, fileName);

            }
        }.runTaskLaterAsynchronously(delayInMillis);
    }
}
