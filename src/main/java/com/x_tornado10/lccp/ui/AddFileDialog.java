package com.x_tornado10.lccp.ui;

import com.x_tornado10.lccp.LCCP;
import io.github.jwharm.javagi.base.GErrorException;
import org.gnome.adw.ActionRow;
import org.gnome.adw.EntryRow;
import org.gnome.adw.PreferencesGroup;
import org.gnome.adw.PreferencesPage;
import org.gnome.gtk.Button;
import org.gnome.gtk.FileDialog;

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
                .build();

        // Create a button for opening the file dialog, with an icon indicating file sending
        var fileSelButton = Button.builder()
                .setIconName("document-send-symbolic")
                .build();

        // Create the file dialog for selecting files
        var fileSel = FileDialog.builder().build();

        // Set the onClicked event for the fileSelButton to open the file dialog
        fileSelButton.onClicked(() -> {
            // Open the file dialog with a callback to handle the user's file selection
            fileSel.open(LCCP.mainWindow, null, (_, result, _) -> {
                try {
                    // Finish the file selection operation and get the selected file
                    var selectedFile = fileSel.openFinish(result);
                    if (selectedFile != null) {
                        // Update the pathRow text with the selected file path
                        pathRow.setSubtitle(selectedFile.getPath());
                    }
                } catch (GErrorException e) {
                    LCCP.logger.error(e); // Handle any errors that occur during file selection
                }
            });
        });

        fileSelButton.setSizeRequest(50, 50);

        // Add the file selection button as a suffix to the pathRow
        pathRow.addSuffix(fileSelButton);

        // Add the pathRow to the file preferences group
        file.add(pathRow);

        // Add the file preferences group to the dialog
        add(file);
    }


}
