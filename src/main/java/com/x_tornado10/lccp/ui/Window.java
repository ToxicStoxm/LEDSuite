package com.x_tornado10.lccp.ui;

import com.x_tornado10.lccp.LCCP;
import com.x_tornado10.lccp.event_handling.listener.EventListener;
import com.x_tornado10.lccp.task_scheduler.LCCPRunnable;
import com.x_tornado10.lccp.task_scheduler.LCCPTask;
import com.x_tornado10.lccp.util.Networking;
import com.x_tornado10.lccp.util.Paths;
import org.gnome.adw.AboutDialog;
import org.gnome.adw.Application;
import org.gnome.adw.ApplicationWindow;
import org.gnome.adw.HeaderBar;
import org.gnome.adw.*;
import org.gnome.gio.SimpleAction;
import org.gnome.gio.SimpleActionGroup;
import org.gnome.gtk.*;
import org.gnome.pango.AttrList;
import org.gnome.pango.Pango;

//@GtkTemplate(name = "window", ui = "/com/x_tornado10/lccp/ui/window.ui")
// main application window
public class Window extends ApplicationWindow implements EventListener {
    // status banner and toast overlay used in the main window
    // made public to enable global toggling
    public Banner status = new Banner("");
    public ToastOverlay toastOverlay = null;

    // booleans to keep track of autoUpdate and statusBarEnabled settings
    private boolean statusBarCurrentState = false;
    private boolean autoUpdate = false;
    private boolean sideBarVisible = true;

    // constructor for the main window
    public Window(Application app) {
        super(app);
        // setting title and default size
        this.setTitle(LCCP.settings.getWindowTitle());
        this.setDefaultSize(LCCP.settings.getWindowDefWidth(), LCCP.settings.getWindowDefHeight());
        this.setIconName("LCCP-logo-256x256");

        // settings auto update to user specified value
        setAutoUpdate(LCCP.settings.isAutoUpdateRemote());

        // toast overlay used to display toasts (notification) to the user
        toastOverlay = new ToastOverlay();

        // container for the header bar on top of the application
        var headerBarContainer = Box.builder()
                .setOrientation(Orientation.VERTICAL)
                .build();

        // the applications header bar
        var headerBar = new HeaderBar();

        // create search button and configure it
        var sbutton = new ToggleButton();
        // setting the icon name to gnome icon name
        sbutton.setIconName("system-search-symbolic");
        // executed when th button is toggled
        sbutton.onToggled(() -> {
            // display work in progress toast as the search function is not yet implemented
            var wipToast = new Toast("Work in progress!");
            wipToast.setTimeout(1);
            toastOverlay.addToast(wipToast);
        });

        // creating and configuring menu button
        var mbutton = new MenuButton();
        mbutton.setAlwaysShowArrow(false);
        // setting the icon name to gnome icon name
        mbutton.setIconName("open-menu-symbolic");

        // defining menu drop down list, witch will be displayed when the menu button is clicked
        var menuDropDownList = new ListBox();
        // defining and configuring list rows
        // settings row (open settings dialog on click)
        var settingsRow = ListBoxRow.builder()
                .setChild(Label.builder()
                        .setLabel("Settings")
                        // setting font attributes
                        .setAttributes(getAttrDef())
                        // align the label
                        .setHalign(Align.START)
                        .setMarginEnd(10)
                        .setMarginBottom(5)
                        .build())
                .setName("settings")
                .setSelectable(false)
                .build();
        // status row (opens status dialog on click)
        var statusRow = ListBoxRow.builder()
                .setChild(Label.builder()
                        .setLabel("Status")
                        // setting font attributes
                        .setAttributes(getAttrDef())
                        // align the label
                        .setHalign(Align.START)
                        .setMarginEnd(10)
                        .setMarginBottom(5)
                        .build())
                .setName("status")
                .setSelectable(false)
                .build();
        // about row (opens about dialog on click)
        var aboutRow = ListBoxRow.builder()
                .setChild(Label.builder()
                        .setLabel("About")
                        // setting font attributes
                        .setAttributes(getAttrDef())
                        // align the label
                        .setHalign(Align.START)
                        .setMarginEnd(10)
                        .build())
                .setName("about")
                .setSelectable(false)
                .build();

        // Creating actions used for keyboard shortcuts
        var activateAboutRow = SimpleAction.builder().setName("activateAboutRow").build();
        activateAboutRow.onActivate(_ -> {
            aboutRow.emitActivate(); // Emit the activate signal on the aboutRow
            aboutRow.emitMoveFocus(DirectionType.TAB_BACKWARD); // deselect current row to close menu list and shift focus to new window
        });
        var activateSettingsRow = SimpleAction.builder().setName("activateSettingsRow").build();
        activateSettingsRow.onActivate(_ -> {
            settingsRow.emitActivate(); // Emit the activate signal on the settingsRow
            settingsRow.emitMoveFocus(DirectionType.TAB_BACKWARD); // deselect current row to close menu list and shift focus to new window
        });
        var activateStatusRow = SimpleAction.builder().setName("activateStatusRow").build();
        activateStatusRow.onActivate(_ -> {
            statusRow.emitActivate(); // Emit the activate signal on the statusRow
            statusRow.emitMoveFocus(DirectionType.TAB_BACKWARD); // deselect current row to close menu list and shift focus to new window
        });

        // Add the actions to the window's action group
        var actionGroup = new SimpleActionGroup();
        actionGroup.addAction(activateAboutRow);
        actionGroup.addAction(activateSettingsRow);
        actionGroup.addAction(activateStatusRow);
        this.insertActionGroup("main", actionGroup);

        // Set up a shortcut controller
        var shortcutController = new ShortcutController();

        // Define and add the shortcuts to the controller
        var shortcutAboutRow = new Shortcut(
                ShortcutTrigger.parseString("<Alt>a"), // ALT + A
                ShortcutAction.parseString("action(main.activateAboutRow)") // trigger previously defined action for about row
        );
        var shortcutSettingsRow = new Shortcut(
                ShortcutTrigger.parseString("<Alt>s"), // ALT + S
                ShortcutAction.parseString("action(main.activateSettingsRow)") // trigger previously defined action for settings row
        );
        var shortcutStatusRow = new Shortcut(
                ShortcutTrigger.parseString("<Control>s"), // CTRL + S
                ShortcutAction.parseString("action(main.activateStatusRow)") // trigger previously defined action for status row
        );
        shortcutController.addShortcut(shortcutAboutRow);
        shortcutController.addShortcut(shortcutSettingsRow);
        shortcutController.addShortcut(shortcutStatusRow);

        // Add the controller to the window
        this.addController(shortcutController);

        // listen for about / settings dialog close and shift the focus backwards to focus the main window again
        getAboutDialog().onClosed(() -> aboutRow.emitMoveFocus(DirectionType.TAB_BACKWARD));
        getSettingsDialog().onClosed(() -> settingsRow.emitMoveFocus(DirectionType.TAB_BACKWARD));

        // change menu list selection mode to single so the user can only select one entry at a time
        menuDropDownList.setSelectionMode(SelectionMode.SINGLE);

        // adding the list rows to the menu list
        menuDropDownList.append(statusRow);
        menuDropDownList.append(settingsRow);
        menuDropDownList.append(aboutRow);

        // creating new popover (small popup)
        var popover = new Popover();

        // listening for menu list entry click events and opening the window / dialog associated with the licked row
        menuDropDownList.onRowActivated(e -> {
            if (e == null) return;
            switch (e.getName()) {
                case "status" -> {
                    //new StatusWindow().present();
                    Networking.FileSender.sendFileToServer(LCCP.server_settings.getIPv4(), LCCP.server_settings.getPort(), Paths.File_System.config);

                }
                // opening settings dialog
                case "settings" -> getSettingsDialog().present(this);
                // opening about dialog
                case "about" -> getAboutDialog().present(this);
            }
            // close the popover
            popover.emitClosed();
        });

        // adding the menu list to the popover
        popover.setChild(menuDropDownList);
        // adding popover to menu button, so it is displayed when the button is clicked
        mbutton.setPopover(popover);

        // adding the search button to the start of the header bar and the menu button to its end
        //headerBar.packStart(sbutton);
        headerBar.packEnd(mbutton);


        // creating main container witch will hold the main window content
        var mainContent = new Box(Orientation.VERTICAL, 0);
        // creating north / center / south containers to correctly align window content
        var northBox = new Box(Orientation.VERTICAL, 0);
        var centerBox = new Box(Orientation.VERTICAL, 0);
        var southBox = new Box(Orientation.VERTICAL, 0);
        // set vertical expanding to true for the center box, so it pushed the north box to the top of the window and the south box to the bottom
        centerBox.setVexpand(true);
        // aligning the south box to the end (bottom) of the window to ensure it never aligns wrongly when resizing window
        southBox.setValign(Align.END);


        // adding the header bar to the header bar container
        headerBarContainer.append(headerBar);

        // adding the status row to the header bar container
        headerBarContainer.append(status);
        // toggling status bar visibility depending on user preferences
        setBannerVisible(LCCP.settings.isDisplayStatusBar());

        // adding the toast overlay to the south box
        southBox.append(toastOverlay);
        // adding the header bar container to the north box
        northBox.append(headerBarContainer);

        // adding all alignment boxes to the main window container
        mainContent.append(northBox);
        mainContent.append(centerBox);
        mainContent.append(southBox);

        var overlaySplitView = new OverlaySplitView();

        overlaySplitView.setEnableHideGesture(true);
        overlaySplitView.setEnableShowGesture(true);

        overlaySplitView.setContent(mainContent);
        overlaySplitView.setSidebarWidthUnit(LengthUnit.PX);
        overlaySplitView.setSidebarWidthFraction(0.2);
        overlaySplitView.setShowSidebar(sideBarVisible);

        var boxList = new ListBox();
        boxList.append(ListBoxRow.builder().setName("Hey").build());

        var smallHeaderBar = HeaderBar.builder().build();
        smallHeaderBar.setTitleWidget(Label.builder().setLabel("Animations").build());
        smallHeaderBar.setHexpand(true);

        var animations = Box.builder().build();
        animations.append(smallHeaderBar);
        animations.setValign(Align.START);
        animations.setHexpand(true);

        overlaySplitView.setSidebar(animations);

        var sideBarToggleButton = new ToggleButton();
        sideBarToggleButton.setIconName("sidebar-show-symbolic");
        headerBar.packStart(sideBarToggleButton);
        sideBarToggleButton.setVisible(overlaySplitView.getShowSidebar());

        sideBarToggleButton.onToggled(() -> {
            boolean active = sideBarToggleButton.getActive();
            if (active && !overlaySplitView.getShowSidebar()) {
                sideBarVisible = true;
                overlaySplitView.setShowSidebar(true);
            } else if (!active && overlaySplitView.getShowSidebar()) {
                sideBarVisible = false;
                overlaySplitView.setCollapsed(false);
            }
        });

        final boolean[] temp = {false};

        new LCCPRunnable() {
            @Override
            public void run() {
                if (getWidth() <= 500) {
                    if (!temp[0]) {
                        temp[0] = true;
                        overlaySplitView.setCollapsed(true);
                        sideBarToggleButton.setVisible(true);
                    }
                } else {
                    temp[0] = false;
                    overlaySplitView.setCollapsed(false);
                    sideBarToggleButton.setVisible(false);
                }
                if (sideBarToggleButton.getActive() && !overlaySplitView.getShowSidebar()) {
                    sideBarToggleButton.setActive(false);
                }
            }
        }.runTaskTimerAsynchronously(0, 1);

        // adding the main container to the window
        this.setContent(overlaySplitView);
    }

    // about dialog
    private AboutDialog aDialog = null;
    // method to either create a new about dialog or get an already existing one
    // this ensures that only one about dialog is created to prevent the app unnecessarily using up system resources
    private AboutDialog getAboutDialog() {
        // checking if an existing about dialog can be reused
        if (aDialog == null) {
            // if not a new one is created
            aDialog = AboutDialog.builder()
                    .setDevelopers(new String[]{"x_Tornado10", "Bukkit GitHub Repo", "CraftBukkit GitHub Repo"})
                    .setArtists(new String[]{"Hannes Campidell", "GNOME Foundation"})
                    .setVersion(LCCP.version)
                    .setLicenseType(License.GPL_3_0)
                    .setApplicationIcon("LCCP-logo-256x256")
                    .setIssueUrl(Paths.Links.Project_GitHub + "issues")
                    .setWebsite(Paths.Links.Project_GitHub)
                    .setApplicationName(LCCP.settings.getWindowTitle())
                    .build();
        }
         return aDialog;
    }
    // generates default font arguments
    public static AttrList getAttrDef() {
        var attr = new AttrList();
        // sets the font to 'Bahnschrift' (does not work :c)
        attr.change(Pango.attrFamilyNew("Bahnschrift"));
        // sets the scale to be 1 (100%)
        attr.change(Pango.attrScaleNew(1));
        return attr;
    }
    // placeholder code for status row (will be replaced with actual status display in the future)
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
    // status bar updater
    private LCCPTask statusBarUpdater;
    // creates a new status bar updater
    private void updateStatus() {
        LCCP.logger.debug("Running new StatusBar update Task!");
        statusBarUpdater = new LCCPRunnable() {
            @Override
            public void run() {
                if (!status.getRevealed()) return;
                // updating status bar to show current status
                status.setTitle("LED-Cube-Status: " + getStatus());
            }
        }.runTaskTimerAsynchronously(0, 20);
    }

    // toggle status bar
    public void setBannerVisible(boolean visible) {
        LCCP.logger.debug("---------------------------------------------------------------");
        LCCP.logger.debug("Fulfilling StatusBarToggle: " + statusBarCurrentState + " >> " + visible);
        status.setVisible(true);
        // if status bar is currently turned off and should be activated
        if (!statusBarCurrentState && visible) {
            // the current status is set to true
            statusBarCurrentState = true;
            // and a new update status task is created and started
            updateStatus();
        } else {
            // if status bar is currently turned on and should be deactivated
            // if there is an updater task running
            if (statusBarUpdater != null) statusBarUpdater.cancel(); // trigger the tasks kill switch
            // set the current status to false
            statusBarCurrentState = false;
        }
        // toggle status bar visibility based on the provided value 'visible'
        status.setRevealed(visible);
        // update user settings to match new value
        if (LCCP.mainWindow != null) LCCP.settings.setDisplayStatusBar(visible);
        LCCP.logger.debug("---------------------------------------------------------------");
    }
    // check if status bar is currently visible
    public boolean isBannerVisible() {
        return statusBarCurrentState;

    }
    // settings dialog
    private SettingsDialog sD = null;
    // method to either create a new settings dialog or get an already existing one
    // this ensures that only one settings dialog is created to prevent the app unnecessarily using up system resources
    private SettingsDialog getSettingsDialog() {
        // checking if an existing about dialog can be reused
        if (sD == null) {
            // if not a new one is created
            sD = new SettingsDialog();
        }
        return sD;
    }
    // check if the settings dialog is currently visible
    public boolean isSettingsDialogVisible() {
        return sD != null;
    }
    // toggle auto update option for the settings dialog
    public void setAutoUpdate(boolean active) {
        LCCP.logger.debug("Fulfilling autoUpdateToggle request -> " + active);
        // if auto updating isn't active and should be activated
        if (!autoUpdate && active) {
            // if the settings dialog is already visible
            if (isSettingsDialogVisible()) getSettingsDialog().startRemoteUpdate(); // start the remote updating loop within the settings dialog
            getSettingsDialog().removeManualRemoteApplySwitch(); // remove the manual updating button from the settings dialog
        // if auto updating is active and should be deactivated
        } else if (autoUpdate && !active) {
            // the manual updating button is re added to the settings dialog
            getSettingsDialog().addManualRemoteApplySwitch();
            // if the settings dialog is currently visible the remote updating task is stopped
            if (isSettingsDialogVisible()) getSettingsDialog().stopRemoteUpdate();
        }
        // current auto updating status is set based on the provided value 'active'
        autoUpdate = active;
    }

    public void test() {
        this.sD = null;
    }
}
