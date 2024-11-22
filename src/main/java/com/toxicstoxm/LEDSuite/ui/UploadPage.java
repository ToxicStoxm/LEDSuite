package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.communication.websocket.WebSocketClient;
import com.toxicstoxm.LEDSuite.formatting.StringFormatter;
import com.toxicstoxm.LEDSuite.gettext.Translations;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.settings.LEDSuiteSettingsBundle;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import io.github.jwharm.javagi.base.GErrorException;
import io.github.jwharm.javagi.gtk.annotations.GtkCallback;
import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.TemplateTypes;
import org.gnome.adw.ApplicationWindow;
import org.gnome.adw.Spinner;
import org.gnome.adw.*;
import org.gnome.gio.File;
import org.gnome.glib.GLib;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.*;
import org.jetbrains.annotations.NotNull;

import java.lang.foreign.MemorySegment;
import java.util.Objects;

/**
 * Adw dialog for uploading files to the server. It includes a file picker, upload statistics and a toggle for automatically starting the file after the upload finished.
 * <br>Template file: {@code UploadPage.ui}
 * @since 1.0.0
 */
@GtkTemplate(name = "UploadPage", ui = "/com/toxicstoxm/LEDSuite/UploadPage.ui")
public class UploadPage extends PreferencesPage implements UploadPageEndpoint {

    private static final Type gtype = TemplateTypes.register(UploadPage.class);

    public UploadPage(MemorySegment address) {
        super(address);
    }

    public static Type getType() {
        return gtype;
    }

    private ApplicationWindow parent;

    private String selectedFile;

    public static @NotNull UploadPage create(ApplicationWindow parent) {
        UploadPage uploadPage = GObject.newInstance(getType());
        uploadPage.parent = parent;
        uploadPage.setUploadButtonState(false);
        uploadPage.uploadStatistics.setSensitive(false);
        return uploadPage;
    }

    @GtkChild(name = "start_animation_after_upload_switch")
    public SwitchRow startAnimationAfterUploadSwitch;

    @GtkCallback(name = "start_animation_after_upload_switch_cb")
    public void startAnimationAfterUploadCb() {
        boolean switch_state = startAnimationAfterUploadSwitch.getActive();
        LEDSuiteApplication.getLogger().info("Start_animation_after_upload switch toggle -> " + switch_state, new LEDSuiteLogAreas.UI());
    }

    @GtkChild(name = "file_picker_button_row")
    public ActionRow filePickerRow;

    @GtkCallback(name = "file_picker_button_cb")
    public void filePickerButtonClickedCb() {

        var filter = FileFilter.builder()
                .setSuffixes(new String[]{"jar"})
                .setMimeTypes(new String[]{"image/*", "video/*"})
                .setName(Translations.getText("Animations"))
                .build();

        String initialFolder = LEDSuiteSettingsBundle.FilePickerInitialFolder.getInstance().get();

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

        filePicker.open(parent, null, (_, result, _)  -> {
            try {
                var selectedFile = filePicker.openFinish(result);
                if (selectedFile != null) {
                    String parentPath = selectedFile.getParent().getPath();
                    if (!Objects.equals(parentPath, System.getProperty("user.home")) && new java.io.File(parentPath).exists()) {
                        LEDSuiteApplication.getLogger().info("Changed initial folder for file picker to: " + parentPath, new LEDSuiteLogAreas.UI());
                        LEDSuiteSettingsBundle.FilePickerInitialFolder.getInstance().set(parentPath);
                    }
                    this.selectedFile = selectedFile.getPath();
                    LEDSuiteApplication.getLogger().info("Selected file: " + this.selectedFile, new LEDSuiteLogAreas.UI());
                    filePickerRow.setSubtitle(StringFormatter.getFileNameFromPath(this.selectedFile));
                    WebSocketClient communication = LEDSuiteApplication.getWebSocketCommunication();
                    if (communication != null && communication.isConnected()) {
                        uploadButton.setSensitive(true);
                    }
                }
            } catch (GErrorException e) {
                LEDSuiteApplication.getLogger().info("User canceled file picker! No file selected!", new LEDSuiteLogAreas.UI());
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

    public void setUploadButtonState(boolean state) {
        uploadButton.setSensitive(!filePickerRow.getSubtitle().isBlank() && !filePickerRow.getSubtitle().equals(Translations.getText("N/A")) && state);
    }

    public void setServerConnected(boolean serverConnected) {
        GLib.idleAddOnce(() -> {
            WebSocketClient webSocketClient = LEDSuiteApplication.getWebSocketCommunication();
            setUploadButtonActive(webSocketClient != null && webSocketClient.isConnected() && !loading);
        });
    }

    @GtkChild(name = "upload_statistics")
    public ExpanderRow uploadStatistics;

    @GtkChild(name = "upload_speed")
    public ActionRow uploadSpeed;

    @GtkChild(name = "upload_eta")
    public ActionRow uploadEta;

    public void setUploadStatistics(@NotNull UploadStatistics newUploadStats) {
        long eta = newUploadStats.millisecondsRemaining();
        long speed = newUploadStats.bytesPerSecond();

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

    public void resetUploadStatistics() {
        GLib.idleAddOnce(() -> {
            uploadStatistics.setExpanded(false);
            uploadStatistics.setSensitive(false);
            String notAvailableText = Translations.getText("N/A");
            uploadSpeed.setSubtitle(notAvailableText);
            uploadEta.setSubtitle(notAvailableText);
        });
    }

    public void setUploadButtonActive(boolean uploading) {
        GLib.idleAddOnce(() -> {
            filePickerRow.setSensitive(!uploading);
            startAnimationAfterUploadSwitch.setSensitive(!uploading);
            setUploadButtonState(!uploading);
            uploadButtonSpinnerRevealer.setRevealChild(uploading);
        });
    }

    @GtkCallback(name = "upload_button_cb")
    public void uploadButtonClickedCb() {
        if (filePickerRow.getSubtitle().isBlank() || filePickerRow.getSubtitle().equals(Translations.getText("N/A"))) {
            setUploadButtonState(false);
            return;
        }
        if (loading) return;
        loading = true;
        setUploadButtonActive(true);
        LEDSuiteApplication.getLogger().info("Upload button clicked, starting upload!", new LEDSuiteLogAreas.USER_INTERACTIONS());

        new LEDSuiteRunnable() {
            @Override
            public void run() {
                LEDSuiteApplication.triggerFileUpload(selectedFile, startAnimationAfterUploadSwitch.getActive());
            }
        }.runTaskAsynchronously();
    }

    public void uploadCompleted(boolean successfully) {
        GLib.idleAddOnce(() -> {
            setUploadButtonActive(false);
            uploadButton.setCssClasses(new String[]{"pill", successfully ? "success" : "destructive-action"});
            resetUploadStatistics();
        });

        new LEDSuiteRunnable() {
            @Override
            public void run() {
                GLib.idleAddOnce(() -> {
                    uploadButton.setCssClasses(new String[]{"pill", "suggested-action"});
                    loading = false;
                });
            }
        }.runTaskLaterAsynchronously(2000);
    }
}
