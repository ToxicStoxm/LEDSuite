package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.formatting.StringFormatter;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import io.github.jwharm.javagi.base.GErrorException;
import io.github.jwharm.javagi.gtk.annotations.GtkCallback;
import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.Types;
import org.gnome.adw.ActionRow;
import org.gnome.adw.ApplicationWindow;
import org.gnome.adw.PreferencesPage;
import org.gnome.adw.SwitchRow;
import org.gnome.gio.File;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.*;

import java.lang.foreign.MemorySegment;

/**
 * Adw dialog for uploading files to the server. It includes a file picker, upload statistics and a toggle for automatically starting the file after the upload finished.
 * <br>Template file: {@code UploadPage.ui}
 * @since 1.0.0
 */
@GtkTemplate(name = "UploadPage", ui = "/com/toxicstoxm/LEDSuite/UploadPage.ui")
public class UploadPage extends PreferencesPage {

    private static final Type gtype = Types.register(UploadPage.class);

    public UploadPage(MemorySegment address) {
        super(address);
    }

    public static Type getType() {
        return gtype;
    }

    private static ApplicationWindow parent;

    public static UploadPage create(ApplicationWindow parent) {
        UploadPage.parent = parent;
        return GObject.newInstance(getType());
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
                .setName("Animations")
                .build();


        var filePicker = FileDialog.builder()
                .setTitle("Pick a file")
                .setModal(true)
                .setFilters(FilterListModel.builder().setFilter(filter).build())
                .setDefaultFilter(filter)
                .setInitialFolder(File.newForPath(System.getProperty("user.home")))
                .build();

        filePicker.open(parent, null, (_, result, _)  -> {
            try {
                var selectedFile = filePicker.openFinish(result);
                if (selectedFile != null) {
                    String filePath = selectedFile.getPath();
                    LEDSuiteApplication.getLogger().info("Selected file: " + filePath, new LEDSuiteLogAreas.UI());
                    filePickerRow.setSubtitle(StringFormatter.getFileNameFromPath(filePath));
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

    private boolean loading = false;

    @GtkCallback(name = "upload_button_cb")
    public void uploadButtonClickedCb() {
        if (loading) return;
        loading = true;
        uploadButtonSpinner.setVisible(true);
        uploadButton.setCssClasses(new String[]{"pill", loading ? "Regular" : "suggested-action"});
        LEDSuiteApplication.getLogger().info("Upload button clicked! loading = " + loading);

        new LEDSuiteRunnable() {
            @Override
            public void run() {
                loading = false;
                uploadButtonSpinner.setVisible(false);
                uploadButton.setCssClasses(new String[]{"pill", loading ? "Regular" : "suggested-action"});
                LEDSuiteApplication.getLogger().info("Upload button clicked! loading = " + loading);
            }
        }.runTaskLaterAsynchronously(2000);
    }


}
