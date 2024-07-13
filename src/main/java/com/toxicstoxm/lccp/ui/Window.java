package com.toxicstoxm.lccp.ui;

import com.toxicstoxm.lccp.Constants;
import com.toxicstoxm.lccp.LCCP;
import com.toxicstoxm.lccp.communication.network.Networking;
import com.toxicstoxm.lccp.event_handling.EventHandler;
import com.toxicstoxm.lccp.event_handling.Events;
import com.toxicstoxm.lccp.event_handling.listener.EventListener;
import com.toxicstoxm.lccp.task_scheduler.LCCPRunnable;
import com.toxicstoxm.lccp.time.TimeManager;
import com.toxicstoxm.lccp.yaml_factory.YAMLMessage;
import com.toxicstoxm.lccp.yaml_factory.YAMLSerializer;
import com.toxicstoxm.lccp.yaml_factory.wrappers.message_wrappers.StatusUpdate;
import com.toxicstoxm.lccp.task_scheduler.LCCPProcessor;
import com.toxicstoxm.lccp.task_scheduler.LCCPTask;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.gnome.adw.AboutDialog;
import org.gnome.adw.Application;
import org.gnome.adw.ApplicationWindow;
import org.gnome.adw.HeaderBar;
import org.gnome.adw.*;
import org.gnome.gio.*;
import org.gnome.gtk.*;
import org.gnome.pango.AttrList;
import org.gnome.pango.EllipsizeMode;
import org.gnome.pango.Pango;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

// main application window
public class Window extends ApplicationWindow implements EventListener {
    // status banner and toast overlay used in the main window
    // made public to enable global toggling
    public Banner status = new Banner("");
    public ToastOverlay toastOverlay = null;
    private ListBox animationsList = null;
    private ListBox addFileList = null;
    private Box addFile = null;
    public ToolbarView rootView = null;
    public ProgressBar progressBar = null;
    public Button playPauseButton = null;
    public Button stopButton = null;
    private Revealer controlButtonsRevealer = null;
    private Revealer StopRevealer = null;
    private Revealer SidebarSpinner = null;
    private HashMap<String, String> availableAnimations;
    private Map.Entry<String, String> currentAnimation = null;

    // booleans to keep track of autoUpdate and statusBarEnabled settings
    private boolean statusBarCurrentState = false;

    // constructor for the main window
    public Window(Application app) {
        super(app);
        // setting title and default size
        this.setTitle(Constants.Application.NAME);
        this.setDefaultSize(LCCP.settings.getWindowDefWidth(), LCCP.settings.getWindowDefHeight());
        this.setSizeRequest(300, 500);
        this.setIconName(Constants.Application.ICON);

        // toast overlay used to display toasts (notification) to the user
        toastOverlay = ToastOverlay.builder().build();

        // the applications header bar
        var headerBar = new HeaderBar();

        // create search button and configure it
        var sbutton = new ToggleButton();
        // setting the icon name to gnome icon name
        sbutton.setIconName(Constants.GTK.Icons.Symbolic.SEARCH);
        // executed when th button is toggled
        sbutton.onToggled(() -> {
            // display work in progress toast as the search function is not yet implemented
            var wipToast = new Toast("Work in progress!");
            wipToast.setTimeout(1);
            toastOverlay.addToast(wipToast);
        });

        availableAnimations = new HashMap<>();

        // creating and configuring menu button
        var mbutton = new MenuButton();
        mbutton.setAlwaysShowArrow(false);
        // setting the icon name to gnome icon name
        mbutton.setIconName(Constants.GTK.Icons.Symbolic.MENU);

        var mainMenu = Menu.builder().build();
        var _status = MenuItem.builder().build();
        _status.setLabel("Status");
        _status.setDetailedAction(Constants.GTK.Actions._Actions._STATUS);
        mainMenu.appendItem(_status);
        var settings = MenuItem.builder().build();
        settings.setLabel("Settings");
        settings.setDetailedAction(Constants.GTK.Actions._Actions._SETTINGS);
        mainMenu.appendItem(settings);
        var generalMenu = Menu.builder().build();
        var shortcuts = MenuItem.builder().build();
        shortcuts.setLabel("Shortcuts");
        shortcuts.setDetailedAction(Constants.GTK.Actions._Actions._SHORTCUTS);
        generalMenu.appendItem(shortcuts);
        var about = MenuItem.builder().build();
        about.setLabel("About");
        about.setDetailedAction(Constants.GTK.Actions._Actions._ABOUT);
        generalMenu.appendItem(about);
        mainMenu.appendSection(null, generalMenu);

        // Creating actions used for keyboard shortcuts
        var aboutRowAction = SimpleAction.builder().setName(Constants.GTK.Actions._Actions.ABOUT).build();
        aboutRowAction.onActivate(_ -> triggerAboutRow());
        var settingsRowAction = SimpleAction.builder().setName(Constants.GTK.Actions._Actions.SETTINGS).build();
        settingsRowAction.onActivate(_ -> triggerSettingsRow());
        var statusRowAction = SimpleAction.builder().setName(Constants.GTK.Actions._Actions.STATUS).build();
        statusRowAction.onActivate(_ -> triggerStatusRow());
        var shortcutAction = SimpleAction.builder().setName(Constants.GTK.Actions._Actions.SHORTCUTS).build();
        shortcutAction.onActivate(_ -> triggerShortcutRow());

        // Add the actions to the window's action group
        var actionGroup = new SimpleActionGroup();
        actionGroup.addAction(aboutRowAction);
        actionGroup.addAction(settingsRowAction);
        actionGroup.addAction(shortcutAction);
        actionGroup.addAction(statusRowAction);
        this.insertActionGroup(Constants.GTK.Actions.Groups.MENU, actionGroup);

        // Set up a shortcut controller
        var shortcutController = ShortcutController.builder().setScope(ShortcutScope.MANAGED).build();
        // Define and add the shortcuts to the controller
        var shortcutStatusRow = Shortcut.builder()
                .setTrigger(ShortcutTrigger.parseString(Constants.GTK.Shortcuts.STATUS))
                .setAction(ShortcutAction.parseString(Constants.GTK.Actions.SHORTCUT_STATUS))
                .build();

        var shortcutSettingsRow = Shortcut.builder()
                .setTrigger(ShortcutTrigger.parseString(Constants.GTK.Shortcuts.SETTINGS))
                .setAction(ShortcutAction.parseString(Constants.GTK.Actions.SHORTCUT_SETTINGS))
                .build();

        var shortcutShortcutsRow = Shortcut.builder()
                .setTrigger(ShortcutTrigger.parseString(Constants.GTK.Shortcuts.SHORTCUTS))
                .setAction(ShortcutAction.parseString(Constants.GTK.Actions.SHORTCUT_SHORTCUTS))
                .build();

        var shortcutAboutRow = Shortcut.builder()
                .setTrigger(ShortcutTrigger.parseString(Constants.GTK.Shortcuts.ABOUT))
                .setAction(ShortcutAction.parseString(Constants.GTK.Actions.SHORTCUT_ABOUT))
                .build();

        shortcutController.addShortcut(shortcutStatusRow);
        shortcutController.addShortcut(shortcutSettingsRow);
        shortcutController.addShortcut(shortcutShortcutsRow);
        shortcutController.addShortcut(shortcutAboutRow);

        // Add the controller to the window
        this.addController(shortcutController);

        mbutton.setMenuModel(mainMenu);

        mbutton.onActivate(() -> LCCP.logger.debug("Menu button clicked"));

        // adding the search button to the start of the header bar and the menu button to its end
        //headerBar.packStart(sbutton);
        headerBar.packEnd(mbutton);

        // creating main container witch will hold the main window content
        var mainContent = new Box(Orientation.VERTICAL, 0);
        mainContent.setHomogeneous(false);
        // creating north / center / south containers to correctly align window content
        var TopBox = new Box(Orientation.VERTICAL, 0);
        var CenterBox = Box.builder()
                .setOrientation(Orientation.VERTICAL)
                .setSpacing(0)
                .setValign(Align.CENTER)
                .build();
        // set vertical expanding to true for the center box, so it pushed the north box to the top of the window and the south box to the bottom
        CenterBox.setVexpand(true);
        // aligning the south box to the end (bottom) of the window to ensure it never aligns wrongly when resizing window

        // toggling status bar visibility depending on user preferences
        setBannerVisible(LCCP.settings.isDisplayStatusBar());

        updateStatus(StatusUpdate.notConnected());

        // adding the header bar container to the north box
        TopBox.append(status);

        var TopRevealer = Revealer.builder()
                .setChild(TopBox)
                .setRevealChild(true)
                .build();
        TopRevealer.setTransitionType(RevealerTransitionType.SLIDE_DOWN);
        var CenterRevealer = Revealer.builder()
                .setChild(CenterBox)
                .setRevealChild(true)
                .build();
        CenterRevealer.setTransitionType(RevealerTransitionType.CROSSFADE);

        // adding all alignment boxes to the main window container
        mainContent.append(TopRevealer);
        mainContent.append(CenterRevealer);

        playPauseButton = Button.builder()
                .setIconName(Constants.GTK.Icons.Symbolic.PLAY)
                .setName("play")
                .setCssClasses(new String[]{"osd", "circular"})
                .build();
        stopButton = Button.builder()
                .setIconName(Constants.GTK.Icons.Symbolic.STOP)
                .setName("stop")
                .setCssClasses(new String[]{"osd", "circular"})
                .build();

        StopRevealer = Revealer.builder()
                .setRevealChild(false)
                .setChild(stopButton)
                .build();
        StopRevealer.setTransitionType(RevealerTransitionType.CROSSFADE);

        TimeManager.initTimeTracker("control_buttons", 500);

        AtomicBoolean allowPlayPause = new AtomicBoolean(true);
        String playState = "play";
        String pauseState = "pause";
        playPauseButton.onClicked(() -> {
            if (!TimeManager.call("control_buttons")) return;
            AtomicReference<String> state = new AtomicReference<>(playPauseButton.getName());
            if (allowPlayPause.get() && currentAnimation != null && availableAnimations.containsKey(currentAnimation.getKey())) {
                try {
                    Networking.Communication.sendYAMLDefaultHost(
                            YAMLMessage.builder()
                                    .setPacketType(YAMLMessage.PACKET_TYPE.request)
                                    .setRequestType(YAMLMessage.REQUEST_TYPE.valueOf(state.get()))
                                    .setRequestFile(currentAnimation.getKey())
                                    .build(),
                            success -> {
                                if (!success) {
                                    idle();
                                    errorFeedback(playPauseButton, allowPlayPause);
                                    LCCP.logger.error(com.toxicstoxm.lccp.yaml_factory.AnimationMenu.capitalizeFirstLetter(state.get()) + " request for " + currentAnimation.getKey() + " failed!");
                                }
                            }
                    );
                } catch (ConfigurationException | YAMLSerializer.InvalidReplyTypeException |
                         YAMLSerializer.InvalidPacketTypeException | YAMLSerializer.TODOException _) {
                    LCCP.logger.error(com.toxicstoxm.lccp.yaml_factory.AnimationMenu.capitalizeFirstLetter(state.get()) + " request for " + currentAnimation.getKey() + " failed!");
                }
            } else {
                idle();
                errorFeedback(playPauseButton, allowPlayPause);
            }
            boolean bool = state.get().equals(playState);
            playPauseButton.setName(bool ? pauseState : playState);
            playPauseButton.setIconName(bool ? Constants.GTK.Icons.Symbolic.PAUSE : Constants.GTK.Icons.Symbolic.PLAY);
            LCCP.logger.debug("Successfully send " + state + " request for " + currentAnimation.getKey() + "!");
            StopRevealer.setRevealChild(true);
        });
        AtomicBoolean allowStop = new AtomicBoolean(true);
        stopButton.onClicked(() -> {
            if (!TimeManager.call("control_buttons")) return;
            if (allowStop.get() && currentAnimation != null && availableAnimations.containsKey(currentAnimation.getKey())) {
                try {
                    Networking.Communication.sendYAMLDefaultHost(
                            YAMLMessage.builder()
                                    .setPacketType(YAMLMessage.PACKET_TYPE.request)
                                    .setRequestType(YAMLMessage.REQUEST_TYPE.stop)
                                    .setRequestFile(currentAnimation.getKey())
                                    .build(),
                            success -> {
                                if (!success) {
                                    errorFeedback(stopButton, allowStop);
                                    LCCP.logger.error("Stop request for " + currentAnimation.getKey() + " failed!");
                                }
                            }
                    );
                } catch (ConfigurationException | YAMLSerializer.InvalidReplyTypeException |
                         YAMLSerializer.InvalidPacketTypeException | YAMLSerializer.TODOException _) {
                    LCCP.logger.error("Stop request for " + currentAnimation.getKey() + " failed!");

                }
            } else errorFeedback(stopButton, allowStop);
            idle();
            LCCP.logger.debug("Successfully send Stop request for " + currentAnimation.getKey() + "!");
        });

        var controlButtons = Box.builder()
                .setOrientation(Orientation.HORIZONTAL)
                .setSpacing(10)
                .build();
        controlButtons.append(StopRevealer);
        controlButtons.append(playPauseButton);

        controlButtonsRevealer = Revealer.builder()
                .setChild(controlButtons)
                .setRevealChild(false)
                .build();
        controlButtonsRevealer.setTransitionType(RevealerTransitionType.CROSSFADE);

        var controlButtonsWrapper = Clamp.builder()
                .setChild(controlButtonsRevealer)
                .setOrientation(Orientation.VERTICAL)
                .setMaximumSize(30)
                .setMarginEnd(30)
                .setMarginBottom(25)
                .setTighteningThreshold(30)
                .setHalign(Align.END)
                .build();

        toastOverlay.setValign(Align.END);
        toastOverlay.setHalign(Align.CENTER);
        var mainView = ToolbarView.builder()
                .setContent(mainContent)
                .build();
        mainView.setBottomBarStyle(ToolbarStyle.FLAT);

        mainView.addTopBar(headerBar);

        var overlay = Overlay.builder().setChild(mainView).build();
        overlay.addOverlay(toastOverlay);
        controlButtonsWrapper.setHalign(Align.END);
        controlButtonsWrapper.setValign(Align.END);
        overlay.addOverlay(controlButtonsWrapper);

        mainView.setTopBarStyle(ToolbarStyle.FLAT);

        var overlaySplitView = new OverlaySplitView();

        overlaySplitView.setEnableHideGesture(true);
        overlaySplitView.setEnableShowGesture(true);

        overlaySplitView.setContent(overlay);
        overlaySplitView.setSidebarWidthUnit(LengthUnit.PX);
        overlaySplitView.setSidebarWidthFraction(0.2);
        overlaySplitView.setShowSidebar(true);

        var smallHeaderBar = HeaderBar.builder().build();
        smallHeaderBar.setTitleWidget(Label.builder().setLabel("File Management").build());
        smallHeaderBar.setHexpand(true);

        smallHeaderBar.setCssClasses(new String[]{"flat"});

        var sidebarContentBox = Box.builder()
                .setOrientation(Orientation.VERTICAL)
                .setSpacing(10)
                .setValign(Align.START)
                .setHexpand(true)
                .build();

        addFileList = ListBox.builder()
                .setSelectionMode(SelectionMode.BROWSE)
                .setCssClasses(
                        new String[]{"navigation-sidebar"}
                )
                .build();
        addFile = Box.builder()
                .setOrientation(Orientation.HORIZONTAL)
                .setTooltipText("Add file to LED-Cube (Upload)")
                .setSpacing(10)
                .build();
        addFile.append(Image.fromIconName(Constants.GTK.Icons.Symbolic.DOCUMENT_SEND));
        addFile.append(
                Label.builder()
                        .setLabel("Add File")
                        .setEllipsize(EllipsizeMode.END)
                        .setXalign(0)
                        .build()
        );
        addFileList.append(
                ListBoxRow.builder()
                .setSelectable(true)
                .setChild(
                       addFile
                ).build()
        );

        var Animations = Label.builder().setLabel("Animations").build();

        animationsList = ListBox.builder()
                .setSelectionMode(SelectionMode.BROWSE)
                .setCssClasses(
                        new String[]{"navigation-sidebar"}
                )
                .build();

        var spinnerBox = Box.builder().setHalign(Align.CENTER).build();
        spinnerBox.append(Spinner.builder().setSpinning(true).build());

        SidebarSpinner = Revealer.builder().setChild(spinnerBox).setRevealChild(true).build();
        SidebarSpinner.setTransitionType(RevealerTransitionType.CROSSFADE);

        AtomicReference<ListBoxRow> current = new AtomicReference<>(new ListBoxRow());

        TimeManager.initTimeTracker("sidebarClickDebounce", 200, System.currentTimeMillis() - 1000);

        addFileList.onRowActivated(_ -> {
            if (!TimeManager.call("sidebarClickDebounce")) {
                addFileList.setSelectionMode(SelectionMode.NONE);
                addFileList.setSelectionMode(SelectionMode.BROWSE);
                return;
            }
            controlButtonsRevealer.setRevealChild(false);
            LCCP.logger.debug("Clicked add file row!");
            CenterRevealer.setRevealChild(false);
            if (CenterBox.getFirstChild() != null) CenterBox.remove(CenterBox.getFirstChild());
            CenterBox.setValign(Align.START);
            CenterBox.append(new AddFileDialog());
            animationsList.setSelectionMode(SelectionMode.NONE);
            animationsList.setSelectionMode(SelectionMode.BROWSE);
            CenterRevealer.setRevealChild(true);
        });

        animationsList.onRowActivated(row -> {
            controlButtonsRevealer.setRevealChild(true);
            if (current.get() == row) return;
            if (!TimeManager.call("sidebarClickDebounce")) {
                animationsList.setSelectionMode(SelectionMode.NONE);
                animationsList.unselectAll();
                animationsList.setSelectionMode(SelectionMode.BROWSE);
                return;
            }
            current.set(row);
            if (!row.getSelectable()) return;
            if (CenterBox.getFirstChild() != null) {
                CenterRevealer.setRevealChild(false);
                CenterBox.remove(CenterBox.getFirstChild());
            }
            CenterBox.setValign(Align.CENTER);
            AtomicBoolean spinner = new AtomicBoolean(true);
            new LCCPRunnable() {
                @Override
                public void run() {
                    if (spinner.get()) {
                        //GLib.idleAdd(GLib.PRIORITY_DEFAULT, () -> {
                          //  CenterBox.append(Spinner.builder().setSpinning(true).build());
                            CenterRevealer.setRevealChild(true);
                            //return true;
                        //});

                    }
                }
            }.runTaskLaterAsynchronously(500);
            String rowName = row.getName();
            if (!availableAnimations.containsKey(rowName)) LCCP.logger.warn("Requesting menu for unknown animation menu! Name: '" + rowName + "'");
            else {
                String icon = availableAnimations.get(rowName);
                currentAnimation = new AbstractMap.SimpleEntry<>(rowName, icon);
            }
            try {
                Networking.Communication.sendYAMLDefaultHost(
                        YAMLMessage.builder()
                                .setPacketType(YAMLMessage.PACKET_TYPE.request)
                                .setRequestType(YAMLMessage.REQUEST_TYPE.menu)
                                .setRequestFile(rowName)
                                .build(),
                        result -> {
                            if (result) {
                                LCCP.logger.debug("Requesting animation menu for '" + rowName + "' from server.");
                                /*toastOverlay.addToast(
                                        Toast.builder()
                                                .setTimeout(3)
                                                .setTitle("Loading menu for '" + rowName + "'...")
                                                .build()
                                );*/
                                getStatus(null);
                            } else {
                                LCCP.logger.error("Failed to load menu for '" + rowName + "'!");
                                /*toastOverlay.addToast(
                                        Toast.builder()
                                                .setTimeout(0)
                                                .setTitle("Failed to load menu for '" + rowName + "'!")
                                                .build()
                                );*/
                                //LCCP.eventManager.fireEvent(new Events.Status(StatusUpdate.notConnected()));
                            }
                        },
                        new LCCPProcessor() {
                            @Override
                            public void run(YAMLMessage yaml) throws DefaultHandleException {
                                if (yaml.getPacketType().equals(YAMLMessage.PACKET_TYPE.reply) && yaml.getReplyType().equals(YAMLMessage.REPLY_TYPE.menu)) {
                                    LCCP.logger.debug(yaml.getAnimationMenu().toString());
                                    String id = "[" + yaml.getNetworkID() + "] ";
                                    LCCP.logger.debug(id + "Converting animation menu to displayable menu!");
                                    spinner.set(false);
                                    CenterRevealer.setRevealChild(false);
                                    if (CenterBox.getFirstChild() != null) {
                                        CenterRevealer.setRevealChild(false);
                                        CenterBox.remove(CenterBox.getFirstChild());
                                    }
                                    CenterBox.setValign(Align.START);
                                    CenterBox.append(AnimationMenu.display(yaml.getAnimationMenu()));
                                    LCCP.logger.debug(id + "Displaying converted menu!");
                                    CenterRevealer.setRevealChild(true);
                                } else throw new DefaultHandleException("Unexpected response!");
                            }
                        }
                );
            } catch (ConfigurationException | YAMLSerializer.YAMLException e) {
                LCCP.logger.error("Failed to send / get menu request for: " + rowName);
                animationsList.remove(row);
                LCCP.logger.error(e);
            }
            LCCP.logger.debug("AnimationSelected: " + rowName);
            addFileList.setSelectionMode(SelectionMode.NONE);
            addFileList.setSelectionMode(SelectionMode.BROWSE);
        });
        sidebarContentBox.append(addFileList);
        sidebarContentBox.append(Separator.builder().build());
        sidebarContentBox.append(Animations);
        sidebarContentBox.append(animationsList);
        sidebarContentBox.append(SidebarSpinner);

        var sidebarMainBox = new Box(Orientation.VERTICAL, 0);
        sidebarMainBox.append(smallHeaderBar);
        sidebarMainBox.append(sidebarContentBox);

        var scrolledView = ScrolledWindow.builder().setChild(sidebarMainBox).build();

        overlaySplitView.setSidebar(scrolledView);

        var sideBarToggleButton = new ToggleButton();
        sideBarToggleButton.setIconName(Constants.GTK.Icons.Symbolic.SIDEBAR_SHOW);
        headerBar.packStart(sideBarToggleButton);
        sideBarToggleButton.setVisible(overlaySplitView.getCollapsed());

        sideBarToggleButton.onToggled(() -> {
            if (sideBarToggleButton.getActive() && !overlaySplitView.getShowSidebar()) {
                LCCP.logger.debug("Sidebar show button pressed (toggle:true)");
                overlaySplitView.setShowSidebar(true);
                // resetting button to avoid checking continuously for sidebar hide signal
                sideBarToggleButton.setActive(false);
            }
        });

        int min = 680;

        var sideBarBreakpoint = Breakpoint.builder()
                .setCondition(
                        BreakpointCondition.parse("max-width: 600px")
                )
                .build();
        sideBarBreakpoint.onApply(() -> {
            overlaySplitView.setCollapsed(true);
            sideBarToggleButton.setVisible(true);
            LCCP.logger.debug("Window with <= " + min + ". Collapsing sidebar");
        });
        sideBarBreakpoint.onUnapply(() -> {
            overlaySplitView.setCollapsed(false);
            sideBarToggleButton.setVisible(false);
        });
        this.addBreakpoint(sideBarBreakpoint);
        overlaySplitView.getSidebar().onStateFlagsChanged(_ -> {
            if (sideBarToggleButton.getActive() && !overlaySplitView.getShowSidebar()) {
                sideBarToggleButton.setActive(false);
            }
        });

        status.onButtonClicked(this::triggerStatusRow);
        status.setButtonLabel("LED Cube Status");

        progressBar = ProgressBar.builder().setFraction(0.0).build();

        rootView = ToolbarView.builder()
                .setContent(overlaySplitView)
                .build();
        rootView.addBottomBar(progressBar);
        rootView.setRevealBottomBars(false);

        // adding the main container to the window
        this.setContent(rootView);
    }

    private void triggerStatusRow() {
        LCCP.logger.debug("User click: status row");
        new StatusDialog().present(this);
    }
    private void triggerSettingsRow() {
        LCCP.logger.debug("User click: settings row");
        getSettingsDialog().present(this);
    }
    private void triggerShortcutRow() {
        LCCP.logger.debug("User click: shortcut row");
        var shortcuts = ShortcutsWindow.builder().build();
        var shortcutSection = ShortcutsSection.builder().build();
        var shortcutGroup = ShortcutsGroup.builder().setTitle("General").build();
        shortcutGroup.addShortcut(
                ShortcutsShortcut.builder()
                        .setShortcutType(ShortcutType.ACCELERATOR)
                        .setAccelerator(Constants.GTK.Shortcuts.STATUS)
                        .setTitle("Open status dialog")
                        .build()
        );
        shortcutGroup.addShortcut(
                ShortcutsShortcut.builder()
                        .setShortcutType(ShortcutType.ACCELERATOR)
                        .setAccelerator(Constants.GTK.Shortcuts.SETTINGS)
                        .setTitle("Open settings dialog")
                        .build()
        );
        shortcutGroup.addShortcut(
                ShortcutsShortcut.builder()
                        .setShortcutType(ShortcutType.ACCELERATOR)
                        .setAccelerator(Constants.GTK.Shortcuts.SHORTCUTS)
                        .setTitle("Open this dialog")
                        .build()
        );
        shortcutGroup.addShortcut(
                ShortcutsShortcut.builder()
                        .setShortcutType(ShortcutType.ACCELERATOR)
                        .setAccelerator(Constants.GTK.Shortcuts.ABOUT)
                        .setTitle("Open about dialog")
                        .build()
        );

        shortcutSection.addGroup(shortcutGroup);
        shortcuts.addSection(shortcutSection);
        shortcuts.setModal(true);
        shortcuts.setTransientFor(this);
        shortcuts.present();

    }
    private void triggerAboutRow() {
        LCCP.logger.debug("User click: about row");
        getAboutDialog().present(this);
    }

    public void errorFeedback(Button button, AtomicBoolean bool) {
        if (!bool.get()) return;
        bool.set(false);
        button.setCssClasses(new String[]{"circular", "destructive-action"});

        new LCCPRunnable() {
            @Override
            public void run() {
                //GLib.idleAdd(GLib.PRIORITY_DEFAULT, () -> {
                    button.setCssClasses(new String[]{"circular", "osd"});
                    bool.set(true);
                    //return true;
                //});
            }
        }.runTaskLaterAsynchronously(750);
    }

    public HashMap<String, String> constructMap(String regex, String... entries) {
        HashMap<String, String> result = new HashMap<>();
        for (String s : entries) {
            LCCP.logger.debug(s);
            String[] parts = s.split(regex);
            LCCP.logger.debug(Arrays.toString(parts));
            result.put(parts[0], parts[1]);
        }
        return result;
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
                    .setDevelopers(new String[]{"ToxicStoxm", "CraftBukkit GitHub Repo"})
                    .setArtists(new String[]{"Hannes Campidell", "GNOME Foundation"})
                    .setVersion(LCCP.getInstance().getApp().getVersion())
                    .setLicenseType(License.GPL_3_0)
                    .setApplicationIcon(Constants.Application.ICON)
                    .setIssueUrl(Constants.Links.PROJECT_GITHUB + "issues")
                    .setWebsite(Constants.Links.PROJECT_GITHUB)
                    .setApplicationName(Constants.Application.NAME)
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
    public void getStatus(Networking.Communication.FinishCallback callback) {
        try {
            Networking.Communication.sendYAML(LCCP.server_settings.getIPv4(), LCCP.server_settings.getPort(), new YAMLMessage()
                            .setPacketType(YAMLMessage.PACKET_TYPE.request)
                            .setReplyType(YAMLMessage.REPLY_TYPE.status)
                            .build(),
                    callback,
                    new LCCPProcessor() {
                        @Override
                        public void run(YAMLMessage yaml) {
                            LCCP.eventManager.fireEvent(new Events.Status(StatusUpdate.fromYAMLMessage(yaml)));
                        }
                    }
            );
        } catch (YAMLSerializer.TODOException | ConfigurationException | YAMLSerializer.InvalidReplyTypeException |
                 YAMLSerializer.InvalidPacketTypeException e) {
            LCCP.logger.error("Failed to send status request to server! Error message: " + e.getMessage());
            LCCP.logger.error(e);
        }
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
                getStatus(success -> {
                    if (!success) LCCP.eventManager.fireEvent(new Events.Status(StatusUpdate.notConnected()));
                });
                // updating status bar to show current status
                //status.setTitle("LED-Cube-Status: " + getStatus());
            }
        }.runTaskTimerAsynchronously(0, 1000);
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

    public void resetSettingsDialog() {
        this.sD = null;
    }

    public void updateStatus(StatusUpdate statusUpdate) {
        status.setTitle(statusUpdate.minimal());
        if (statusUpdate.isNotConnected() && controlButtonsRevealer != null) controlButtonsRevealer.setRevealChild(false);
    }

    @EventHandler
    public void onStatus(Events.Status e) {
        try {
            StatusUpdate statusUpdate = e.statusUpdate();
            updateStatus(statusUpdate);

            ListBoxRow selectedRow = animationsList.getSelectedRow();
            String name = "";
            if (selectedRow != null) name = selectedRow.getName();
            if (!name.isBlank()) setControlButtons(statusUpdate, currentAnimation);
            if (name.isBlank() || TimeManager.call("animations")) {
                animationsList.unselectAll();
                animationsList.setSelectionMode(SelectionMode.NONE);
                animationsList.removeAll();

                SidebarSpinner.setRevealChild(true);

                List<ListBoxRow> anims = new ArrayList<>();

                availableAnimations = statusUpdate.getAvailableAnimations();
                if (availableAnimations != null) {
                    for (Map.Entry<String, String> entry : availableAnimations.entrySet()) {
                        var availableAnimation = Box.builder()
                                .setOrientation(Orientation.HORIZONTAL)
                                .setTooltipText("Open " + entry.getKey() + " settings menu")
                                .setName(entry.getKey())
                                .setSpacing(10)
                                .build();
                        availableAnimation.append(Image.fromIconName(entry.getValue()));
                        availableAnimation.append(
                                Label.builder()
                                        .setLabel(entry.getKey())
                                        .setEllipsize(EllipsizeMode.END)
                                        .setXalign(0)
                                        .build()
                        );
                        ListBoxRow row = listBoxWrap(availableAnimation);
                        if (entry.getKey().equals(name)) selectedRow = row;
                        anims.add(row);
                    }
                    SidebarSpinner.setRevealChild(false);
                    for (ListBoxRow lbr : anims) {
                        animationsList.append(lbr);
                    }
                }
                animationsList.setSelectionMode(SelectionMode.BROWSE);
                animationsList.selectRow(selectedRow);
            }
        } catch (NumberFormatException ex) {
            LCCP.logger.warn("Status update failed!");
            LCCP.logger.error(ex);
        }
    }

    @EventHandler
    public void onStarted(Events.Started e) {
        addFileList.emitRowSelected(addFileList.getRowAtIndex(0));
        addFileList.emitRowActivated(addFileList.getRowAtIndex(0));
        addFileList.emitSelectedRowsChanged();
    }

    public ListBoxRow listBoxWrap(Widget widget) {
        return ListBoxRow.builder()
                        .setSelectable(true)
                        .setName(widget.getName())
                        .setChild(
                                widget
                        ).build();
    }
    public void playing() {
        StopRevealer.setRevealChild(true);
        playPauseButton.setName("pause");
        playPauseButton.setIconName("media-playback-pause-symbolic");
    }
    public void paused() {
        StopRevealer.setRevealChild(true);
        playPauseButton.setName("play");
        playPauseButton.setIconName("media-playback-start-symbolic");
    }
    public void idle() {
        StopRevealer.setRevealChild(false);
        playPauseButton.setName("play");
        playPauseButton.setIconName("media-playback-start-symbolic");
    }
    public void setControlButtons(StatusUpdate statusUpdate, Map.Entry<String, String> name) {
        controlButtonsRevealer.setRevealChild(false);
        if (statusUpdate.isNotConnected()) {
            return;
        }
        if (statusUpdate.isFileLoaded()) {
            if (!statusUpdate.getFileSelected().equals(name.getKey())) {
                idle();
                controlButtonsRevealer.setRevealChild(true);
                return;
            }
            switch (statusUpdate.getFileState()) {
                case YAMLMessage.FILE_STATE.playing -> playing();
                case YAMLMessage.FILE_STATE.paused -> paused();
                default -> idle();
            }
        } else idle();
        controlButtonsRevealer.setRevealChild(true);
    }

}
