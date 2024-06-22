package com.x_tornado10.lccp.ui;

import com.x_tornado10.lccp.LCCP;
import com.x_tornado10.lccp.task_scheduler.LCCPRunnable;
import io.github.jwharm.javagi.base.GErrorException;
import org.gnome.adw.*;
import org.gnome.gio.File;
import org.gnome.gio.ListModel;
import org.gnome.glib.Variant;
import org.gnome.gtk.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class AddFileDialog extends PreferencesPage {
    public AddFileDialog() {
        // Create a preferences group for file selection
        var file = PreferencesGroup.builder()
                .setTitle("File")
                .build();

        // Create an EntryRow for displaying the selected file path, initially empty
        var pathRow = ActionRow.builder()
                .setTitle("Path")
                .setSubtitle("path/to/file.mp4")
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

        var file1 =  File.newForPath(LCCP.settings.getSelectionDir());
        LCCP.logger.debug(file1.getPath());

        // Create the file dialog for selecting files
        var fileSel = FileDialog.builder()
                .setTitle("Pick file to upload")
                .setModal(true)
                .setInitialFolder(file1)
                .setFilters(FilterListModel.builder().setFilter(filter).build())
                .setDefaultFilter(filter)
                .build();

        // Set the onClicked event for the fileSelButton to open the file dialog
        fileSelButton.onClicked(() -> {
            LCCP.logger.debug("Clicked file select button: opening new open file dialog");
            // Open the file dialog with a callback to handle the user's file selection

            fileSel.open(LCCP.mainWindow, null, (_, result, _) -> {
                try {
                    // Finish the file selection operation and get the selected file
                    var selectedFile = fileSel.openFinish(result);
                    if (selectedFile != null) {
                        String filePath = selectedFile.getPath();
                        // Update the pathRow text with the selected file path
                        pathRow.setSubtitle(filePath);
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

        // Add the file preferences group to the dialog
        add(file);
    }

}
