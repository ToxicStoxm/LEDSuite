package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.communication.websocket.WebSocketClient;
import com.toxicstoxm.LEDSuite.formatting.StringFormatter;
import com.toxicstoxm.LEDSuite.gettext.Translations;
import com.toxicstoxm.LEDSuite.scheduler.SmartRunnable;
import com.toxicstoxm.YAJL.Logger;
import com.toxicstoxm.YAJL.placeholders.StringPlaceholder;
import io.github.jwharm.javagi.base.GErrorException;
import io.github.jwharm.javagi.gtk.annotations.GtkCallback;
import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import org.gnome.adw.*;
import org.gnome.adw.ApplicationWindow;
import org.gnome.adw.Spinner;
import org.gnome.gio.File;
import org.gnome.glib.GLib;
import org.gnome.gobject.GObject;
import org.gnome.gobject.ParamSpec;
import org.gnome.gtk.*;
import org.jetbrains.annotations.NotNull;

import java.lang.foreign.MemorySegment;
import java.util.Objects;

/**
 * Adw dialog for uploading files to the server.
 * Includes a file picker, upload statistics, and a toggle for automatically starting the file after the upload finishes.
 * <br>Template file: {@code UploadPage.ui}
 *
 * <p>Responsible for file upload management, showing the progress of uploads, and interacting with WebSocket communication.</p>
 *
 * @since 1.0.0
 */
@GtkTemplate(name = "UploadPage", ui = "/com/toxicstoxm/LEDSuite/UploadPage.ui")
public class UploadPage extends PreferencesPage implements UploadPageEndpoint {

    private static final Logger logger = Logger.autoConfigureLogger();

    public UploadPage(MemorySegment address) {
        super(address);
    }

    private ApplicationWindow parent;
    private String selectedFile;

    /**
     * Creates a new instance of the UploadPage.
     *
     * @param parent The parent ApplicationWindow.
     * @return A new instance of UploadPage.
     */
    public static @NotNull UploadPage create(ApplicationWindow parent) {
        UploadPage uploadPage = GObject.newInstance(UploadPage.class);
        uploadPage.parent = parent;
        uploadPage.setUploadButtonUploading(false);
        uploadPage.uploadStatistics.setSensitive(false);
        return uploadPage;
    }

    @GtkChild(name = "start_animation_after_upload_switch")
    public SwitchRow startAnimationAfterUploadSwitch;

    /**
     * Callback for the "start animation after upload" switch.
     * Logs the state of the switch (on/off).
     */
    @GtkCallback(name = "start_animation_after_upload_switch_cb")
    public void startAnimationAfterUploadCb(ParamSpec paramSpec) {
        if (!Objects.equals(paramSpec.getName(), "active")) return;
        boolean switchState = startAnimationAfterUploadSwitch.getActive();
        logger.debug("Start animation after upload switch toggled -> {}", switchState);
    }

    @GtkChild(name = "file_picker_button_row")
    public ActionRow filePickerRow;

    /**
     * Callback for the file picker button. Opens the file picker dialog and sets the selected file.
     * The file picker only allows certain types of files (e.g., JAR, images, videos).
     */
    @GtkCallback(name = "file_picker_button_cb")
    public void filePickerButtonClickedCb() {
        logger.verbose("File picker button clicked");
        var filter = FileFilter.builder()
                .setSuffixes(new String[]{"jar"})
                .setMimeTypes(new String[]{"image/*", "video/*"})
                .setName(Translations.getText("Animations"))
                .build();

        String initialFolder = LEDSuiteApplication.getSettings().mainSection.uiSettings.filePickerInitialFolder;
        if (initialFolder.isBlank()) {
            initialFolder = System.getProperty("user.home");
        }

        var filePicker = FileDialog.builder()
                .setTitle(Translations.getText("Pick a file"))
                .setModal(true)
                .setFilters(FilterListModel.builder().setFilter(filter).build())
                .setDefaultFilter(filter)
                .setInitialFolder(File.newForPath(initialFolder))
                .build();

        filePicker.open(parent, null, (_, result, _) -> {
            try {
                var selectedFile = filePicker.openFinish(result);
                if (selectedFile != null) {
                    String parentPath = selectedFile.getParent().getPath();
                    if (new java.io.File(parentPath).exists()) {
                        logger.info("Changed initial folder for file picker to: {}", parentPath);
                        LEDSuiteApplication.getSettings().mainSection.uiSettings.filePickerInitialFolder = parentPath;
                    }
                    this.selectedFile = selectedFile.getPath();
                    logger.debug("Selected file: {}", this.selectedFile);
                    filePickerRow.setSubtitle(StringFormatter.getFileNameFromPath(this.selectedFile));
                    WebSocketClient communication = LEDSuiteApplication.getWebSocketCommunication();
                    if (communication != null && communication.isConnected()) {
                        uploadButton.setSensitive(true);
                    }
                }
            } catch (GErrorException e) {
                logger.debug("User canceled file picker! No file selected!");
            }
        });
    }

    @GtkChild(name = "upload_button")
    public Button uploadButton;

    @GtkChild(name = "upload_button_spinner")
    public Spinner uploadButtonSpinner;

    @GtkChild(name = "upload_button_spinner_revealer")
    public Revealer uploadButtonSpinnerRevealer;

    private boolean loading = false;

    /**
     * Updates the state of the upload button based on whether a file is selected.
     *
     * @param state If true, the upload button is enabled. Otherwise, it is disabled.
     */
    public void setUploadButtonState(boolean state) {
        uploadButton.setSensitive(!filePickerRow.getSubtitle().isBlank() && !filePickerRow.getSubtitle().equals(Translations.getText("N/A")) && state);
    }

    /**
     * Sets the server connection state and updates the upload button's active state.
     *
     * @param serverConnected {@code true} if the server is connected, {@code false} otherwise.
     */
    public void setServerConnected(boolean serverConnected) {
        GLib.idleAddOnce(() -> setUploadButtonState(serverConnected && !loading));
    }

    @GtkChild(name = "upload_statistics")
    public ExpanderRow uploadStatistics;

    @GtkChild(name = "upload_speed")
    public ActionRow uploadSpeed;

    @GtkChild(name = "upload_eta")
    public ActionRow uploadEta;

    /**
     * Updates the upload statistics (speed and ETA) in the UI.
     *
     * @param newUploadStats The updated upload statistics.
     */
    public void setUploadStatistics(@NotNull UploadStatistics newUploadStats) {
        long eta = newUploadStats.millisecondsRemaining();
        long speed = newUploadStats.bytesPerSecond();

        logger.verbose("Upload Statistics: ETA: '{}' Speed: '{}'",
                (StringPlaceholder) () -> StringFormatter.formatDuration(eta),
                (StringPlaceholder) () -> StringFormatter.formatSpeed(speed)
        );

        if (!this.uploadStatistics.getExpanded()) {
            GLib.idleAddOnce(() -> {
                uploadStatistics.setExpanded(true);
                uploadStatistics.setSensitive(true);
            });
        }

        GLib.idleAddOnce(() -> {
            uploadEta.setSubtitle(StringFormatter.formatDuration(eta));
            uploadSpeed.setSubtitle(StringFormatter.formatSpeed(speed));
        });
    }

    /**
     * Resets the upload statistics display to its initial state.
     */
    public void resetUploadStatistics() {
        logger.verbose("Resetting upload statistics");
        GLib.idleAddOnce(() -> {
            uploadStatistics.setExpanded(false);
            uploadStatistics.setSensitive(false);
            String notAvailableText = Translations.getText("N/A");
            uploadSpeed.setSubtitle(notAvailableText);
            uploadEta.setSubtitle(notAvailableText);
        });
    }

    /**
     * Updates the upload button's active state based on the upload status.
     *
     * @param uploading {@code true} if the upload is in progress, {@code false} otherwise.
     */
    public void setUploadButtonUploading(boolean uploading) {
        GLib.idleAddOnce(() -> {
            filePickerRow.setSensitive(!uploading);
            startAnimationAfterUploadSwitch.setSensitive(!uploading);
            setUploadButtonState(!uploading);
            uploadButtonSpinnerRevealer.setRevealChild(uploading);
        });
    }

    @GtkCallback(name = "upload_button_cb")
    public void uploadButtonClickedCb() {
        logger.verbose("Upload button clicked");
        if (filePickerRow.getSubtitle().isBlank() || filePickerRow.getSubtitle().equals(Translations.getText("N/A"))) {
            setUploadButtonState(false);
            logger.warn("Upload button click ignored, button should be insensitive!");
            return;
        }
        if (loading) return;
        loading = true;
        setUploadButtonUploading(true);
        logger.debug("Upload button click accepted, starting upload!");

        new SmartRunnable() {
            @Override
            public void run() {
                LEDSuiteApplication.triggerFileUpload(selectedFile, startAnimationAfterUploadSwitch.getActive());
            }
        }.runTaskAsync();

        logger.debug("Upload scheduled");
    }

    /**
     * Called when the upload is completed, updates the button style and resets the statistics.
     *
     * @param successfully {@code true} if the upload was successful, {@code false} otherwise.
     */
    public void uploadCompleted(boolean successfully) {
        logger.verbose("Updating UI state -> upload-complete");
        GLib.idleAddOnce(() -> {
            setUploadButtonUploading(false);
            uploadButton.setCssClasses(new String[]{"pill", successfully ? "success" : "destructive-action"});
            resetUploadStatistics();
        });

        new SmartRunnable() {
            @Override
            public void run() {
                GLib.idleAddOnce(() -> {
                    uploadButton.setCssClasses(new String[]{"pill", "suggested-action"});
                    loading = false;
                });
            }
        }.runTaskLaterAsync(2000);
    }
}
