package com.x_tornado10.lccp.ui;

import com.x_tornado10.lccp.LCCP;
import com.x_tornado10.lccp.util.Paths;
import org.gnome.adw.*;
import org.gnome.adw.AboutDialog;
import org.gnome.adw.Application;
import org.gnome.adw.ApplicationWindow;
import org.gnome.adw.HeaderBar;
import org.gnome.gtk.*;

public class Window extends ApplicationWindow {
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
                case "status" -> toastOverlay.addToast(new Toast("Status"));
                case "settings" -> toastOverlay.addToast(new Toast("Settings"));
                case "about" -> aDialog.present(this);
            }
        });

        var popover = new Popover();
        popover.setChild(listBox);
        mbutton.setPopover(popover);

        headerBar.packStart(sbutton);
        headerBar.packEnd(mbutton);

        box.append(headerBar);
        box.append(toastOverlay);

        this.setContent(box);
    }
}
