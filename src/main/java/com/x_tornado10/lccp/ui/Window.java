package com.x_tornado10.lccp.ui;

import com.x_tornado10.lccp.LCCP;
import com.x_tornado10.lccp.util.Paths;
import org.gnome.adw.*;
import org.gnome.adw.AboutDialog;
import org.gnome.adw.Application;
import org.gnome.adw.ApplicationWindow;
import org.gnome.adw.HeaderBar;
import org.gnome.gio.SimpleAction;
import org.gnome.gio.SimpleActionGroup;
import org.gnome.gtk.*;
import org.gnome.pango.*;


public class Window extends ApplicationWindow {
    public Banner status = new Banner("");
    private boolean stopUpdate = true;
    public Window(Application app) {
        super(app);
        this.setTitle(LCCP.settings.getWindowTitle());
        this.setDefaultSize(1280, 720);

        var box = Box.builder()
                .setOrientation(Orientation.VERTICAL)
                .build();

        var headerBar = new HeaderBar();


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
                        .setAttributes(getAttrDef())
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
                        .setAttributes(getAttrDef())
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
                        .setAttributes(getAttrDef())
                        .setHalign(Align.START)
                        .setMarginEnd(10)
                        .build())
                .setName("about")
                .setSelectable(false)
                .build();

        // Create the 'activate' action
        var activateAboutRow = SimpleAction.builder().setName("activateAboutRow").build();
        activateAboutRow.onActivate(_ -> {
            aboutRow.emitActivate(); // Emit the activate signal on the aboutRow
            aboutRow.emitMoveFocus(DirectionType.TAB_BACKWARD);
        });
        var activateSettingsRow = SimpleAction.builder().setName("activateSettingsRow").build();
        activateSettingsRow.onActivate(_ -> {
            settingsRow.emitActivate(); // Emit the activate signal on the aboutRow
            settingsRow.emitMoveFocus(DirectionType.TAB_BACKWARD);
        });
        var activateStatusRow = SimpleAction.builder().setName("activateStatusRow").build();
        activateStatusRow.onActivate(_ -> {
            statusRow.emitActivate(); // Emit the activate signal on the aboutRow
            statusRow.emitMoveFocus(DirectionType.TAB_BACKWARD);
        });

        // Add the action to the window's action group
        var actionGroup = new SimpleActionGroup();
        actionGroup.addAction(activateAboutRow);
        actionGroup.addAction(activateSettingsRow);
        actionGroup.addAction(activateStatusRow);
        this.insertActionGroup("main", actionGroup);

        // Set up a shortcut controller
        var shortcutController = new ShortcutController();

        // Define and add the shortcut to the controller
        var shortcutAboutRow = new Shortcut(
                ShortcutTrigger.parseString("<Alt>a"),
                ShortcutAction.parseString("action(main.activateAboutRow)")
        );
        var shortcutSettingsRow = new Shortcut(
                ShortcutTrigger.parseString("<Alt>s"),
                ShortcutAction.parseString("action(main.activateSettingsRow)")
        );
        var shortcutStatusRow = new Shortcut(
                ShortcutTrigger.parseString("<Control>s"),
                ShortcutAction.parseString("action(main.activateStatusRow)")
        );
        shortcutController.addShortcut(shortcutAboutRow);
        shortcutController.addShortcut(shortcutSettingsRow);
        shortcutController.addShortcut(shortcutStatusRow);

        // Add the controller to the window
        this.addController(shortcutController);

        getAboutDialog().onClosed(() -> aboutRow.emitMoveFocus(DirectionType.TAB_BACKWARD));


        listBox.setSelectionMode(SelectionMode.SINGLE);

        listBox.append(statusRow);
        listBox.append(settingsRow);
        listBox.append(aboutRow);

        listBox.onRowActivated(e -> {
            if (e == null) return;
            switch (e.getName()) {
                case "status" -> new StatusWindow().present();
                case "settings" -> new SettingsWindow().present();
                case "about" -> getAboutDialog().present(this);
            }
        });

        var popover = new Popover();
        popover.setChild(listBox);
        mbutton.setPopover(popover);

        headerBar.packStart(sbutton);
        headerBar.packEnd(mbutton);

        /*
        var box1 = new Box(Orientation.HORIZONTAL, 0);
        var statusButton = ToggleButton.builder()
                .setLabel("LED-Cube Status")
                .setMarginEnd(15)
                .build();
        statusButton.setActive(true);
        statusLabel1 = new Label("");
        statusLabel1.setAttributes(getAttrBig());
        statusButton.onToggled(() -> statusLabel1.setVisible(statusButton.getActive()));
        box1.append(statusButton);
        updateStatus();
        box1.append(statusLabel1);
        box1.setHalign(Align.START);
        box1.setMarginTop(15);
        box1.setMarginStart(15);
         */


        box.append(headerBar);

        setVisible(true);
        box.append(status);
        //box.append(box1);


        var mainContent = new Box(Orientation.VERTICAL, 0);
        var northBox = new Box(Orientation.VERTICAL, 0);
        var centerBox = new Box(Orientation.VERTICAL, 0);
        var southBox = new Box(Orientation.VERTICAL, 0);
        centerBox.setVexpand(true);
        southBox.setValign(Align.END);

        southBox.append(toastOverlay);
        northBox.append(box);

        mainContent.append(northBox);
        mainContent.append(centerBox);
        mainContent.append(southBox);

        this.setContent(mainContent);
    }

    private AboutDialog aDialog = null;
    private AboutDialog getAboutDialog() {
        if (aDialog == null) {
            aDialog = AboutDialog.builder()
                    .setDevelopers(new String[]{"x_Tornado10"})
                    .setVersion(LCCP.version)
                    .setLicense("GPL-3.0")
                    .setDeveloperName("x_Tornado10")
                    .setWebsite(Paths.Links.Project_GitHub)
                    .setApplicationName(LCCP.settings.getWindowTitle())
                    .build();
        }
         return aDialog;
    }
    public static AttrList getAttrBig() {
        var attr = new AttrList();
        attr.change(Pango.attrFamilyNew("Bahnschrift"));
        attr.change(Pango.attrScaleNew(1.5));
        return attr;
    }
    public static AttrList getAttrSmall() {
        var attr = new AttrList();
        attr.change(Pango.attrFamilyNew("Bahnschrift"));
        attr.change(Pango.attrScaleNew(1.3));
        return attr;
    }
    public static AttrList getAttrDef() {
        var attr = new AttrList();
        attr.change(Pango.attrFamilyNew("Bahnschrift"));
        attr.change(Pango.attrScaleNew(1));
        return attr;
    }
    boolean uploading = true;
    boolean displayingAnimation = false;
    private String getStatus() {

        StringBuilder stringBuilder = new StringBuilder();
        if (uploading) {
            stringBuilder.append("Uploading -> 500MB/s | ");
        }
        if (displayingAnimation) {
            stringBuilder.append("Current Animation -> 'Never Gonna Give You Up.mp4'");
        }
        uploading = !uploading;
        displayingAnimation = !displayingAnimation;
        return stringBuilder.toString();
    }
    private void updateStatus() {
        new Thread(() -> {
            long lastexec = System.currentTimeMillis() - 1001;
            while (!stopUpdate) {
                if (System.currentTimeMillis() - lastexec >= 1000) {
                    status.setTitle("LED-Cube-Status: " + getStatus());
                    lastexec = System.currentTimeMillis();
                }
            }
        }).start();
    }
    public void setVisible(boolean visible) {
        if (stopUpdate && visible) {
            updateStatus();
        }
        stopUpdate = !visible;
        status.setRevealed(visible);
    }
    public boolean isBannerVisible() {
        return status.getRevealed();
    }
}
