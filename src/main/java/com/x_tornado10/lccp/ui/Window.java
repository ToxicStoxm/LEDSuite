package com.x_tornado10.lccp.ui;

import com.x_tornado10.lccp.LCCP;
import com.x_tornado10.lccp.event_handling.listener.EventListener;
import com.x_tornado10.lccp.util.Paths;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.gnome.adw.*;
import org.gnome.adw.AboutDialog;
import org.gnome.adw.Application;
import org.gnome.adw.ApplicationWindow;
import org.gnome.adw.HeaderBar;
import org.gnome.gio.SimpleAction;
import org.gnome.gio.SimpleActionGroup;
import org.gnome.gtk.*;
import org.gnome.pango.*;

import java.io.File;
import java.util.Arrays;


public class Window extends ApplicationWindow implements EventListener {
    public Banner status = new Banner("");
    private boolean statusBarCurrentState = false;
    private boolean autoUpdate = false;
    public ToastOverlay toastOverlay = null;
    public Window(Application app) {
        super(app);
        this.setTitle(LCCP.settings.getWindowTitle());
        this.setDefaultSize(LCCP.settings.getWindowDefWidth(), LCCP.settings.getWindowDefHeight());

        setAutoUpdate(LCCP.settings.isAutoUpdateRemote());

        var box = Box.builder()
                .setOrientation(Orientation.VERTICAL)
                .build();

        var headerBar = new HeaderBar();

        var sbutton = new ToggleButton();
        toastOverlay = new ToastOverlay();
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
        getSettingsDialog().onClosed(() -> settingsRow.emitMoveFocus(DirectionType.TAB_BACKWARD));


        listBox.setSelectionMode(SelectionMode.SINGLE);

        listBox.append(statusRow);
        listBox.append(settingsRow);
        listBox.append(aboutRow);

        var popover = new Popover();

        listBox.onRowActivated(e -> {
            if (e == null) return;
            switch (e.getName()) {
                case "status" -> {
                    new StatusWindow().present();
                    /*
                    //File file1 = new File(Paths.server_config);
                    try {
                        // parsing config and loading the values from storage (Default: ./LED-Cube-Control-Panel/server_config.yaml)
                        // using Apache-Commons-Config (and dependencies like snakeyaml and commons-beanutils)
                        YAMLConfiguration yamlConfig = new YAMLConfiguration();

                        // Load the YAML file
                        FileHandler fileHandler = new FileHandler(yamlConfig);
                        fileHandler.load(Paths.server_config);

                        // Access the nested configuration
                        //Configuration subConfig = yamlConfig.configurationAt("test");

                        // Retrieve the value

                        String value = (String) yamlConfig.getList("test").get(1);
                        String value1 = (String) yamlConfig.getList("test").get(2);


                        LCCP.logger.fatal(yamlConfig.getString("test1.test2.test3.test4.test5"));
                        // settings are loaded into an instance of the settings class, so they can be used during runtime without any IO-Calls
                    } catch (ConfigurationException ex) {
                        // if any errors occur during config parsing an error is displayed in the console
                        // the program is halted to prevent any further unwanted behavior
                        ex.printStackTrace();
                    }
                     */

                }
                case "settings" -> getSettingsDialog().present(this);
                case "about" -> getAboutDialog().present(this);
            }
            popover.emitClosed();
        });

        popover.setChild(listBox);
        mbutton.setPopover(popover);

        headerBar.packStart(sbutton);
        headerBar.packEnd(mbutton);

        box.append(headerBar);

        box.append(status);
        setBannerVisible(LCCP.settings.isDisplayStatusBar());

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
    private StatusBarUpdater statusBarUpdater;
    private void updateStatus() {
        LCCP.logger.debug("Running new StatusBar update Task!");
        statusBarUpdater = new StatusBarUpdater();
    }

    private class StatusBarUpdater extends Thread {
        public StatusBarUpdater() {
            setName("StatusBarUpdaterTask_" + getName());
            this.start();
        }

        private boolean stop = false;

        @Override
        public void run() {
            long lastexec = System.currentTimeMillis() - 1001;
            while (true) {
                if (stop) return;
                long current = System.currentTimeMillis();
                if (current - lastexec >= 1000) {
                    if (!status.getRevealed()) return;
                    if (stop) return;
                    status.setTitle("LED-Cube-Status: " + getStatus());
                    lastexec = current;
                }
            }
        }
    }

    public void setBannerVisible(boolean visible) {
        LCCP.logger.debug("---------------------------------------------------------------");
        LCCP.logger.debug("Fulfilling StatusBarToggle: " + statusBarCurrentState + " >> " + visible);
        status.setVisible(true);
        if (!statusBarCurrentState && visible) {
            statusBarCurrentState = true;
            updateStatus();
        } else {
            if (statusBarUpdater != null) statusBarUpdater.stop = true;
            statusBarCurrentState = false;
        }
        status.setRevealed(visible);
        if (LCCP.mainWindow != null) LCCP.settings.setDisplayStatusBar(visible);
        LCCP.logger.debug("---------------------------------------------------------------");
    }
    public boolean isBannerVisible() {
        return statusBarCurrentState;
    }
    private SettingsDialog sD = null;
    private SettingsDialog getSettingsDialog() {
        if (sD == null) {
            sD = new SettingsDialog();
        }
        return sD;
    }
    public boolean isSettingsDialogVisible() {
        return sD != null;
    }
    public void setAutoUpdate(boolean active) {
        LCCP.logger.debug("Fulfilling autoUpdateToggle request -> " + active);
        if (!autoUpdate && active) {
            if (isSettingsDialogVisible()) getSettingsDialog().startRemoteUpdate();
            getSettingsDialog().removeManualRemoteApplySwitch();
        } else if (autoUpdate && !active) {
            getSettingsDialog().addManualRemoteApplySwitch();
            if (isSettingsDialogVisible()) getSettingsDialog().stopRemoteUpdate();
        }
        autoUpdate = active;
    }
}
