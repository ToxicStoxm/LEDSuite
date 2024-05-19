package com.x_tornado10.lccp.ui;

import com.x_tornado10.lccp.LCCP;
import com.x_tornado10.lccp.util.Paths;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gnome.adw.*;
import org.gnome.adw.AboutDialog;
import org.gnome.adw.Application;
import org.gnome.adw.ApplicationWindow;
import org.gnome.adw.HeaderBar;
import org.gnome.gio.File;
import org.gnome.gtk.*;

import java.net.URL;

public class Window extends ApplicationWindow {
    private static final Log log = LogFactory.getLog(Window.class);
    private Label statusLabel1;
    public Window(Application app) {
        super(app);
        this.setTitle(LCCP.settings.getWindowTitleRaw().replace(Paths.Placeholders.VERSION, ""));
        this.setDefaultSize(1280, 720);

        var box = Box.builder()
                .setOrientation(Orientation.VERTICAL)
                .build();

        var headerBar = new HeaderBar();

        var aDialog = new AboutDialog();
        aDialog.setDevelopers(new String[]{"x_Tornado10"});
        aDialog.setVersion(LCCP.version);
        aDialog.setLicense("GPL-3.0");
        aDialog.setDeveloperName("x_Tornado10");
        aDialog.setWebsite("https://github.com/ToxicStoxm/LED-Cube-Control-Panel");
        aDialog.setApplicationName(LCCP.settings.getWindowTitleRaw().replace(Paths.Placeholders.VERSION, ""));

        var sbutton = new ToggleButton();
        var toastOverlay = new ToastOverlay();
        sbutton.setIconName("system-search-symbolic");
        sbutton.onToggled(() -> {
            var wipToast = new Toast("Work in progress!");
            wipToast.setTimeout(1);
            toastOverlay.addToast(wipToast);
        });

        var mbutton = new MenuButton();
        mbutton.setAlwaysShowArrow(false);
        mbutton.setIconName("open-menu-symbolic");

        var statusWindow = new org.gnome.adw.Window();
        statusWindow.setTitle("LED-Cube Status");

        var box0 = Box.builder()
                .setOrientation(Orientation.VERTICAL)
                .build();

        var headerBar0 = new HeaderBar();
        box0.append(headerBar0);

        statusWindow.setContent(box0);

        var listBox = new ListBox();
        var settingsRow = ListBoxRow.builder()
                .setChild(Label.builder()
                        .setLabel("Settings")
                        .setHalign(Align.START)
                        .setMarginEnd(10)
                        .setMarginBottom(5)
                        .build())
                .setName("settings")
                .setSelectable(false)
                .build();
        var statusRow = ListBoxRow.builder()
                .setChild(Label.builder()
                        .setLabel("Status")
                        .setHalign(Align.START)
                        .setMarginEnd(10)
                        .setMarginBottom(5)
                        .build())
                .setName("status")
                .setSelectable(false)
                .build();
        var aboutRow = ListBoxRow.builder()
                .setChild(Label.builder()
                        .setLabel("About")
                        .setHalign(Align.START)
                        .setMarginEnd(10)
                        .build())
                .setName("about")
                .setSelectable(false)
                .build();

        listBox.setSelectionMode(SelectionMode.SINGLE);

        listBox.append(statusRow);
        listBox.append(settingsRow);
        listBox.append(aboutRow);

        listBox.onRowActivated(e -> {
            if (e == null) return;
            switch (e.getName()) {
                case "status" -> statusWindow.present();
                case "settings" -> toastOverlay.addToast(new Toast("Settings"));
                case "about" -> aDialog.present(this);
            }
        });

        var popover = new Popover();
        popover.setChild(listBox);
        mbutton.setPopover(popover);

        headerBar.packStart(sbutton);
        headerBar.packEnd(mbutton);

        var box1 = new Box(Orientation.HORIZONTAL, 0);
        var statusButton = ToggleButton.builder()
                .setLabel("LED-Cube Status")
                .setMarginEnd(15)
                .build();
        statusButton.setActive(true);
        statusButton.onToggled(() -> statusLabel1.setVisible(statusButton.getActive()));
        statusLabel1 = Label.builder()
                .setLabel(getStatus())
                .build();
        box1.append(statusButton);
        box1.append(statusLabel1);
        box1.setHalign(Align.START);
        box1.setMarginTop(15);
        box1.setMarginStart(15);

        box.append(headerBar);
        box.append(box1);
        box.append(toastOverlay);

        var css = CssProvider.builder().build();
        css.loadFromString(
                "body {\n" +
                        "    font-family: 'Bahnschrift', sans-serif;\n" +
                        "    font-size: 16px;\n" + // Adjust font size as needed
                        "}\n" +
                        "h1 {\n" +
                        "    font-family: 'Bahnschrift', sans-serif;\n" +
                        "    font-size: 24px;\n" + // Adjust font size for headings
                        "}"
        );



        this.setContent(box);
    }

    private String getStatus() {
        boolean uploading = true;
        boolean displayingAnimation = true;
        StringBuilder stringBuilder = new StringBuilder();
        if (uploading) {
            stringBuilder.append("Uploading -> 500MB/s | ");
        }
        if (displayingAnimation) {
            stringBuilder.append("Current Animation -> 'Never Gonna Give You Up.mp4'");
        }
        return stringBuilder.toString();
    }
    public void updateStatus() {
        statusLabel1.setLabel("LED-Cube Status: " + getStatus());
    }
}
