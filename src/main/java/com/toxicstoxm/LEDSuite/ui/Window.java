package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.LEDSuite;
import com.toxicstoxm.LEDSuite.cache.Cache;
import com.toxicstoxm.LEDSuite.communication.network.Networking;
import com.toxicstoxm.LEDSuite.event_handling.EventHandler;
import com.toxicstoxm.LEDSuite.event_handling.Events;
import com.toxicstoxm.LEDSuite.event_handling.listener.EventListener;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteGuiRunnable;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteProcessor;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteTask;
import com.toxicstoxm.LEDSuite.time.TimeManager;
import com.toxicstoxm.LEDSuite.yaml_factory.YAMLMessage;
import com.toxicstoxm.LEDSuite.yaml_factory.YAMLSerializer;
import com.toxicstoxm.LEDSuite.yaml_factory.wrappers.message_wrappers.StatusUpdate;
import lombok.NonNull;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.gnome.adw.AboutDialog;
import org.gnome.adw.ApplicationWindow;
import org.gnome.adw.HeaderBar;
import org.gnome.adw.*;
import org.gnome.gio.Menu;
import org.gnome.gio.MenuItem;
import org.gnome.gio.SimpleAction;
import org.gnome.gio.SimpleActionGroup;
import org.gnome.glib.GLib;
import org.gnome.gtk.*;
import org.gnome.pango.EllipsizeMode;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

// main application window
public class Window extends ApplicationWindow implements EventListener {
    private HashMap<String, String> availableAnimations = new HashMap<>();
    private Map.Entry<String, String> currentAnimation = null;

    public final Cache<String, Widget> cache = new Cache<>();

    // constructor for the main window
    public Window(org.gnome.adw.Application app) {

        // initializing an application window using the superclass constructor
        super(app);

        // set window title, default window size, minimum window size and app icon
        this.setTitle(Constants.Application.NAME);
        this.setDefaultSize(
                LEDSuite.argumentsSettings.getWindowDefWidth(),
                LEDSuite.argumentsSettings.getWindowDefHeight()
        );
        this.setSizeRequest(360, 500);
        this.setIconName(Constants.Application.ICON);

        // gets the GUI and adds it to the window
        // checks for null pointer exceptions that indicate problems with GUI creation
        // if any are found a shutdown is initiated
        try {

            this.setContent(getGUI());
        } catch (NullPointerException e) {
            LEDSuite.logger.fatal("Failed to create rough content hierarchy!");
            app.emitShutdown();
        }

        // adding the main and general action groups to the application window
        this.insertActionGroup(Constants.GTK.Actions.Groups.MENU, getMainActionGroup());
        this.insertActionGroup(Constants.GTK.Actions.Groups.GENERAL, getGeneralActionGroup());

        // Add the keyboard controller to the window
        this.addController(getKeyboardShortcutController());
        this.addController(getGeneralShortcutController());

        this.addBreakpoint(getSideBarBreakpoint());
    }
    // Creates the GUI structure and populates it with widgets
    private ToolbarView getGUI() {

        // Creates rough content hierarchy (hierarchy is in reverse order)
        createRoughContentHierarchy();

        // Gets the rough content hierarchy elements from cache
        // Performs null checks to avoid unexpected behavior
        @NonNull var mainView = cache.get(ToolbarView.class, "mainView");
        @NonNull var windowOverlay = cache.get(Overlay.class, "windowOverlay");
        @NonNull var sidebarContentView = cache.get(ToolbarView.class, "sidebarContentView");
        @NonNull var contentView = cache.get(ToolbarView.class, "contentView");

        // creates header bars, status bar and progress bar and caches them
        createBars();

        // Gets the created bars from cache
        // Performs null checks to avoid unexpected behavior
        @NonNull var headerBar = cache.get(HeaderBar.class, "headerBar");
        @NonNull var statusBar = cache.get(Banner.class, "statusBar");
        @NonNull var progressBar = cache.get(ProgressBar.class, "progressBar");
        @NonNull var sidebarHeaderbar = cache.get(HeaderBar.class, "sidebarHeaderBar");

        // Assigns each bar to its container
        contentView.addTopBar(headerBar);
        contentView.addTopBar(statusBar);
        mainView.addBottomBar(progressBar);
        sidebarContentView.addTopBar(sidebarHeaderbar);

        // populates the status bar with some default values bases on user preference
        setStatusBarVisible(LEDSuite.settings.isDisplayStatusBar());
        updateStatusBar(StatusUpdate.notConnected());

        // gets the animation control buttons and add them to the window overlay
        windowOverlay.addOverlay(getAnimationControlButtons());
        windowOverlay.addOverlay(getToastOverlay());

        populateSidebar();

        statusBar.onButtonClicked(this::triggerStatusRow);
        statusBar.setButtonLabel(LEDSuite.i18n("status_button"));

        // adding the main container to the window
        return mainView;
    }

    private ToastOverlay getToastOverlay() {
        // creates a new toast overlay which is used to display toasts (notifications) inside the application window
        // and add it to the window overlay
        var toastOverlay = ToastOverlay.builder()
                .setValign(Align.END)
                .setHalign(Align.CENTER)
                .build();
        cache.put("toastOverlay", toastOverlay);
        return toastOverlay;
    }

    private Breakpoint getSideBarBreakpoint() {
        @NonNull var overlaySplitView = cache.get(OverlaySplitView.class, "overlaySplitView");
        @NonNull var sideBarToggleButton = cache.get(ToggleButton.class, "sideBarToggleButton");
        int min = 680;

        var sideBarBreakpoint = Breakpoint.builder()
                .setCondition(
                        BreakpointCondition.parse("max-width: 600px")
                )
                .build();
        sideBarBreakpoint.onApply(() -> {
            overlaySplitView.setCollapsed(true);
            sideBarToggleButton.setVisible(true);
            LEDSuite.logger.debug("Window with <= " + min + ". Collapsing sidebar");
        });
        sideBarBreakpoint.onUnapply(() -> {
            overlaySplitView.setCollapsed(false);
            sideBarToggleButton.setVisible(false);
        });
        overlaySplitView.getSidebar().onStateFlagsChanged(_ -> {
            if (sideBarToggleButton.getActive() && !overlaySplitView.getShowSidebar()) {
                sideBarToggleButton.setActive(false);
            }
        });
        return sideBarBreakpoint;
    }

    private ShortcutController getGeneralShortcutController() {
        var sideBarShortcut = Shortcut.builder()
                .setTrigger(ShortcutTrigger.parseString(Constants.GTK.Shortcuts.SIDEBAR))
                .setAction(ShortcutAction.parseString(Constants.GTK.Actions.SHORTCUT_SIDEBAR))
                .build();

        var generalShortcutsController = ShortcutController.builder().setName("general").build();
        generalShortcutsController.addShortcut(sideBarShortcut);
        return generalShortcutsController;
    }

    private SimpleActionGroup getGeneralActionGroup() {
        @NonNull var overlaySplitView = cache.get(OverlaySplitView.class, "overlaySplitView");
        @NonNull var sideBarToggleButton = cache.get(ToggleButton.class, "sideBarToggleButton");
        var sideBarButtonAction = SimpleAction.builder().setName(Constants.GTK.Actions._Actions.SIDEBAR).build();
        sideBarButtonAction.onActivate(_ -> {
            if (overlaySplitView.getCollapsed()) {
                if (overlaySplitView.getShowSidebar()) {
                    overlaySplitView.setShowSidebar(false);
                } else {
                    sideBarToggleButton.emitClicked();
                }
            }
        });
        var generalActionGroup = new SimpleActionGroup();
        generalActionGroup.addAction(sideBarButtonAction);
        return generalActionGroup;
    }

    private void populateSidebar() {
        @NonNull var sidebarContentBox = cache.get(Box.class, "sidebarContentBox");
        @NonNull var mainContentRevealer = cache.get(Revealer.class, "mainContentRevealer");
        @NonNull var contentBox = cache.get(Box.class, "contentBox");
        @NonNull var sidebarAnimationSection = cache.get(ListBox.class, "sidebarAnimationSection");
        @NonNull var controlButtonsRevealer = cache.get(Revealer.class, "controlButtonsRevealer");

        var sidebarFileSection = ListBox.builder()
                .setSelectionMode(SelectionMode.BROWSE)
                .setCssClasses(
                        new String[]{"navigation-sidebar"}
                )
                .build();
        Box addFile = Box.builder()
                .setOrientation(Orientation.HORIZONTAL)
                .setTooltipText(LEDSuite.i18n("add_file_row_tooltip"))
                .setSpacing(10)
                .build();
        addFile.append(Image.fromIconName(Constants.GTK.Icons.Symbolic.DOCUMENT_SEND));
        addFile.append(
                Label.builder()
                        .setLabel(LEDSuite.i18n("add_file_row"))
                        .setEllipsize(EllipsizeMode.END)
                        .setXalign(0)
                        .build()
        );
        sidebarFileSection.append(
                ListBoxRow.builder()
                        .setSelectable(true)
                        .setChild(
                                addFile
                        ).build()
        );

        var Animations = Label.builder().setLabel(LEDSuite.i18n("animations_header")).build();

        sidebarAnimationSection = ListBox.builder()
                .setSelectionMode(SelectionMode.BROWSE)
                .setCssClasses(
                        new String[]{"navigation-sidebar"}
                )
                .build();

        var spinnerBox = Box.builder().setHalign(Align.CENTER).build();
        spinnerBox.append(Spinner.builder().setSpinning(true).build());

        var sidebarSpinnerRevealer = Revealer.builder()
                .setChild(spinnerBox)
                .setRevealChild(true)
                .setTransitionType(RevealerTransitionType.CROSSFADE)
                .build();
        cache.put("sidebarSpinnerRevealer", sidebarSpinnerRevealer);

        AtomicReference<ListBoxRow> current = new AtomicReference<>(new ListBoxRow());

        TimeManager.initTimeTracker("sidebarClickDebounce", 200, System.currentTimeMillis() - 1000);

        @NonNull ListBox finalSidebarAnimationSection = sidebarAnimationSection;
        sidebarFileSection.onRowActivated(_ -> {
            if (!TimeManager.call("sidebarClickDebounce")) {
                sidebarFileSection.setSelectionMode(SelectionMode.NONE);
                sidebarFileSection.setSelectionMode(SelectionMode.BROWSE);
                return;
            }
            controlButtonsRevealer.setRevealChild(false);
            LEDSuite.logger.debug("Clicked add file row!");
            mainContentRevealer.setRevealChild(false);
            if (contentBox.getFirstChild() != null) contentBox.remove(contentBox.getFirstChild());
            contentBox.setValign(Align.START);
            contentBox.append(new AddFileDialog());
            finalSidebarAnimationSection.setSelectionMode(SelectionMode.NONE);
            finalSidebarAnimationSection.setSelectionMode(SelectionMode.BROWSE);
            mainContentRevealer.setRevealChild(true);
        });

        @NonNull ListBox finalSidebarAnimationSection1 = sidebarAnimationSection;
        sidebarAnimationSection.onRowActivated(row -> {
            controlButtonsRevealer.setRevealChild(true);
            if (current.get() == row) return;
            if (!TimeManager.call("sidebarClickDebounce")) {
                finalSidebarAnimationSection1.setSelectionMode(SelectionMode.NONE);
                finalSidebarAnimationSection1.unselectAll();
                finalSidebarAnimationSection1.setSelectionMode(SelectionMode.BROWSE);
                return;
            }
            current.set(row);
            if (!row.getSelectable()) return;
            if (contentBox.getFirstChild() != null) {
                mainContentRevealer.setRevealChild(false);
                contentBox.remove(contentBox.getFirstChild());
            }
            contentBox.setValign(Align.CENTER);
            AtomicBoolean spinner = new AtomicBoolean(true);
            new LEDSuiteRunnable() {
                @Override
                public void run() {
                    if (spinner.get()) {
                        GLib.idleAddOnce(() -> mainContentRevealer.setRevealChild(true));
                    }
                }
            }.runTaskLaterAsynchronously(500);
            String rowName = row.getName();
            if (!availableAnimations.containsKey(rowName))
                LEDSuite.logger.warn("Requesting menu for unknown animation menu! Name: '" + rowName + "'");
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
                                LEDSuite.logger.debug("Requesting animation menu for '" + rowName + "' from server.");
                                getStatus(null);
                            } else {
                                LEDSuite.logger.error("Failed to load menu for '" + rowName + "'!");
                            }
                        },
                        new LEDSuiteProcessor() {
                            @Override
                            public void run(YAMLMessage yaml) throws DefaultHandleException {
                                if (yaml.getPacketType().equals(YAMLMessage.PACKET_TYPE.reply) && yaml.getReplyType().equals(YAMLMessage.REPLY_TYPE.menu)) {
                                    LEDSuite.logger.debug(yaml.getAnimationMenu().toString());
                                    String id = "[" + yaml.getNetworkID() + "] ";
                                    LEDSuite.logger.debug(id + "Converting animation menu to displayable menu!");
                                    spinner.set(false);
                                    mainContentRevealer.setRevealChild(false);
                                    if (contentBox.getFirstChild() != null) {
                                        mainContentRevealer.setRevealChild(false);
                                        contentBox.remove(contentBox.getFirstChild());
                                    }
                                    contentBox.setValign(Align.START);
                                    contentBox.append(AnimationMenu.display(yaml.getAnimationMenu()));
                                    LEDSuite.logger.debug(id + "Displaying converted menu!");
                                    mainContentRevealer.setRevealChild(true);
                                } else throw new DefaultHandleException("Unexpected response!");
                            }
                        }
                );
            } catch (ConfigurationException | YAMLSerializer.YAMLException e) {
                LEDSuite.logger.error("Failed to send / get menu request for: " + rowName);
                finalSidebarAnimationSection1.remove(row);
                LEDSuite.logger.displayError(e);
            }
            LEDSuite.logger.debug("AnimationSelected: " + rowName);
            sidebarFileSection.setSelectionMode(SelectionMode.NONE);
            sidebarFileSection.setSelectionMode(SelectionMode.BROWSE);
        });
        sidebarContentBox.append(sidebarFileSection);
        sidebarContentBox.append(Separator.builder().build());
        sidebarContentBox.append(Animations);
        sidebarContentBox.append(sidebarAnimationSection);
        sidebarContentBox.append(sidebarSpinnerRevealer);

        cache.put("sidebarFileSection", sidebarFileSection);
        cache.put("sidebarAnimationSection", sidebarAnimationSection);
    }

    // creates the main header bar, the sidebar header bar, the status bar and the progress bar
    // and caches them
    private void createBars() {
        var headerBar = HeaderBar.builder().build();
        // Inserts the main menu button at the end of the header bar
        headerBar.packEnd(getMainMenuButton());
        headerBar.packStart(getSideBarToggleButton());
        cache.put("headerBar", headerBar);

        // initializes the status bar with a default title and
        cache.put("statusBar",
                Banner.builder()
                        .setTitle("N/A")
                        .build()
        );

        // creates a progress bar and caches it
        cache.put("progressBar",
                ProgressBar.builder()
                        .setFraction(0.0)
                        .build()
        );

        // creates a header bar for the sidebar and caches it
        cache.put("sidebarHeaderBar",
                HeaderBar.builder()
                        .setTitleWidget(
                                Label.builder()
                                        .setLabel(LEDSuite.i18n("file_management_header"))
                                        .build()
                        )
                        .setHexpand(true)
                        .setCssClasses(new String[]{"flat"})
                        .build()
        );
    }

    private void createRoughContentHierarchy() {
        // the content box holds all main content widgets, outside the sidebar
        var contentBox = Box.builder()
                .setOrientation(Orientation.VERTICAL)
                .setSpacing(0)
                // set vertical expanding to true for the center box,
                // so it pushed the north box to the top of the window and the south box to the bottom
                .setVexpand(true)
                .setValign(Align.CENTER)
                .build();

        // main content box revealer manages visibility of the content box
        var mainContentRevealer = Revealer.builder()
                .setChild(contentBox)
                .setRevealChild(true)
                .setTransitionType(RevealerTransitionType.CROSSFADE)
                .build();

        // contains the header bar and content box
        var contentView = ToolbarView.builder()
                .setContent(mainContentRevealer)
                .setBottomBarStyle(ToolbarStyle.FLAT)
                .setTopBarStyle(ToolbarStyle.FLAT)
                .build();

        var sidebarContentBox = Box.builder()
                .setOrientation(Orientation.VERTICAL)
                .setSpacing(10)
                .setValign(Align.START)
                .setHexpand(true)
                .build();

        var sidebarContentView = ToolbarView.builder()
                .setContent(
                        ScrolledWindow.builder()
                                .setChild(sidebarContentBox)
                                .build()
                )
                .build();

        // adds the sidebar as an overlay to the main view
        var overlaySplitView = OverlaySplitView.builder()
                .setContent(contentView)
                .setSidebar(sidebarContentView)
                .setEnableHideGesture(true)
                .setEnableShowGesture(true)
                .setSidebarWidthUnit(LengthUnit.PX)
                .setSidebarWidthFraction(0.2)
                .setShowSidebar(true)
                .build();

        // adds various overlays to the overlay split view
        var windowOverlay = Overlay.builder()
                .setChild(overlaySplitView)
                .build();

        // the main view is the top level container and contains all widgets
        var mainView = ToolbarView.builder()
                .setContent(windowOverlay)
                .setRevealBottomBars(false)
                .build();

        // adding the base hierarchy elements to the cache
        cache.put("mainView", mainView);
        cache.put("windowOverlay", windowOverlay);
        cache.put("overlaySplitView", overlaySplitView);
        cache.put("sidebarContentView", sidebarContentView);
        cache.put("sidebarContentBox", sidebarContentBox);
        cache.put("contentView", contentView);
        cache.put("mainContentRevealer", mainContentRevealer);
        cache.put("contentBox", contentBox);
    }

    private Clamp getAnimationControlButtons() {
        var playPauseButton = Button.builder()
                .setIconName(Constants.GTK.Icons.Symbolic.PLAY)
                .setName("play")
                .setCssClasses(new String[]{"osd", "circular"})
                .build();

        cache.put("playPauseButton", playPauseButton);

        var stopButton = Button.builder()
                .setIconName(Constants.GTK.Icons.Symbolic.STOP)
                .setName("stop")
                .setCssClasses(new String[]{"osd", "circular"})
                .build();

        var stopButtonRevealer = Revealer.builder()
                .setRevealChild(false)
                .setChild(stopButton)
                .setTransitionType(RevealerTransitionType.CROSSFADE)
                .build();

        cache.put("stopButtonRevealer", stopButtonRevealer);

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
                                    idle(playPauseButton, stopButtonRevealer);
                                    errorFeedback(playPauseButton, allowPlayPause);
                                    LEDSuite.logger.error(com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.capitalizeFirstLetter(state.get()) + " request for " + currentAnimation.getKey() + " failed!");
                                }
                            }
                    );
                } catch (ConfigurationException | YAMLSerializer.InvalidReplyTypeException |
                         YAMLSerializer.InvalidPacketTypeException | YAMLSerializer.TODOException _) {
                    LEDSuite.logger.error(com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.capitalizeFirstLetter(state.get()) + " request for " + currentAnimation.getKey() + " failed!");
                }
            } else {
                idle(playPauseButton, stopButtonRevealer);
                errorFeedback(playPauseButton, allowPlayPause);
            }
            boolean bool = state.get().equals(playState);
            playPauseButton.setName(bool ? pauseState : playState);
            playPauseButton.setIconName(bool ? Constants.GTK.Icons.Symbolic.PAUSE : Constants.GTK.Icons.Symbolic.PLAY);
            LEDSuite.logger.debug("Successfully send " + state + " request for " + currentAnimation.getKey() + "!");
            stopButtonRevealer.setRevealChild(true);
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
                                    LEDSuite.logger.error("Stop request for " + currentAnimation.getKey() + " failed!");
                                }
                            }
                    );
                } catch (ConfigurationException | YAMLSerializer.InvalidReplyTypeException |
                         YAMLSerializer.InvalidPacketTypeException | YAMLSerializer.TODOException _) {
                    LEDSuite.logger.error("Stop request for " + currentAnimation.getKey() + " failed!");

                }
            } else errorFeedback(stopButton, allowStop);
            idle(playPauseButton, stopButtonRevealer);
            LEDSuite.logger.debug("Successfully send Stop request for " + currentAnimation.getKey() + "!");
        });

        var controlButtons = Box.builder()
                .setOrientation(Orientation.HORIZONTAL)
                .setSpacing(10)
                .build();
        controlButtons.append(stopButtonRevealer);
        controlButtons.append(playPauseButton);

        var controlButtonsRevealer = Revealer.builder()
                .setChild(controlButtons)
                .setRevealChild(false)
                .build();
        controlButtonsRevealer.setTransitionType(RevealerTransitionType.CROSSFADE);
        cache.put("controlButtonsRevealer", controlButtonsRevealer);

        return Clamp.builder()
                .setChild(controlButtonsRevealer)
                .setOrientation(Orientation.VERTICAL)
                .setMaximumSize(30)
                .setMarginEnd(30)
                .setMarginBottom(25)
                .setTighteningThreshold(30)
                .setValign(Align.END)
                .setHalign(Align.END)
                .build();
    }

    private MenuButton getMainMenuButton() {
        // creating new menu button with hamburger menu icon
        var mainMenuButton = MenuButton.builder()
                .setLabel(LEDSuite.i18n("main_menu_button_label"))
                .setTooltipText(LEDSuite.i18n("main_menu_button_tooltip"))
                .setAlwaysShowArrow(false)
                .setIconName(Constants.GTK.Icons.Symbolic.MENU)
                .build();

        // creating a new main menu dropdown, which will be displayed when pressing the main menu button
        var mainMenu = Menu.builder().build();

        // creating all menu items that are later displayed inside the main menu
        var mainMenuStatusRow = MenuItem.builder().build();
        mainMenuStatusRow.setLabel(LEDSuite.i18n("status_row"));
        mainMenuStatusRow.setDetailedAction(Constants.GTK.Actions._Actions._STATUS);

        var mainMenuSettingsRow = MenuItem.builder().build();
        mainMenuSettingsRow.setLabel(LEDSuite.i18n("settings_row"));
        mainMenuSettingsRow.setDetailedAction(Constants.GTK.Actions._Actions._SETTINGS);

        var shortcuts = MenuItem.builder().build();
        shortcuts.setLabel(LEDSuite.i18n("shortcuts_row"));
        shortcuts.setDetailedAction(Constants.GTK.Actions._Actions._SHORTCUTS);

        var about = MenuItem.builder().build();
        about.setLabel(LEDSuite.i18n("about_row", "%APP_NAME%", Constants.Application.NAME));
        about.setDetailedAction(Constants.GTK.Actions._Actions._ABOUT);

        // adding the menu items to the main menu, in this specific order
        mainMenu.appendItem(mainMenuStatusRow);
        mainMenu.appendItem(mainMenuSettingsRow);
        mainMenu.appendItem(shortcuts);
        mainMenu.appendItem(about);

        // assigning the main menu to the main menu button
        mainMenuButton.setMenuModel(mainMenu);
        mainMenuButton.onActivate(() -> LEDSuite.logger.debug("Menu button clicked"));
        return mainMenuButton;
    }

    private Button getSideBarToggleButton() {
        @NonNull var overlaySplitView = cache.get(OverlaySplitView.class, "overlaySplitView");
        var sideBarToggleButton = new ToggleButton();
        sideBarToggleButton.setIconName(Constants.GTK.Icons.Symbolic.SIDEBAR_SHOW);

        sideBarToggleButton.setVisible(overlaySplitView.getCollapsed());

        sideBarToggleButton.onToggled(() -> {
            if (sideBarToggleButton.getActive() && !overlaySplitView.getShowSidebar()) {
                LEDSuite.logger.debug("Sidebar show button pressed (toggle:true)");
                overlaySplitView.setShowSidebar(true);
                // resetting the button to avoid checking continuously for sidebar hide signal
                sideBarToggleButton.setActive(false);
            }
        });

        cache.put("sideBarToggleButton", sideBarToggleButton);
        return sideBarToggleButton;
    }

    private SimpleActionGroup getMainActionGroup() {
        // Create a new action group to hold all keyboard shortcut related actions
        var mainActionGroup = SimpleActionGroup.builder().build();

        // Creating all actions required to handle keyboard shortcuts
        var statusRowAction = SimpleAction.builder()
                .setName(Constants.GTK.Actions._Actions.STATUS)
                .build();

        var settingsRowAction = SimpleAction.builder()
                .setName(Constants.GTK.Actions._Actions.SETTINGS)
                .build();

        var shortcutAction = SimpleAction.builder()
                .setName(Constants.GTK.Actions._Actions.SHORTCUTS)
                .build();

        var aboutRowAction = SimpleAction.builder()
                .setName(Constants.GTK.Actions._Actions.ABOUT)
                .build();

        // assigning the corresponding functions to the actions
        statusRowAction.onActivate(_ -> triggerStatusRow());
        settingsRowAction.onActivate(_ -> triggerSettingsRow());
        shortcutAction.onActivate(_ -> triggerShortcutRow());
        aboutRowAction.onActivate(_ -> triggerAboutRow());

        // adding all the actions to the main action group
        mainActionGroup.addAction(statusRowAction);
        mainActionGroup.addAction(settingsRowAction);
        mainActionGroup.addAction(shortcutAction);
        mainActionGroup.addAction(aboutRowAction);
        return mainActionGroup;
    }

    private ShortcutController getKeyboardShortcutController() {
        // Set up a new shortcut controller for handling keyboard shortcuts
        var keyboardShortcutController = ShortcutController.builder()
                .setScope(ShortcutScope.GLOBAL)
                .build();

        // Creating all keyboard shortcuts
        var statusRowKeyboardShortcut = Shortcut.builder()
                .setTrigger(ShortcutTrigger.parseString(Constants.GTK.Shortcuts.STATUS))
                .setAction(ShortcutAction.parseString(Constants.GTK.Actions.SHORTCUT_STATUS))
                .build();

        var settingsRowKeyboardShortcut = Shortcut.builder()
                .setTrigger(ShortcutTrigger.parseString(Constants.GTK.Shortcuts.SETTINGS))
                .setAction(ShortcutAction.parseString(Constants.GTK.Actions.SHORTCUT_SETTINGS))
                .build();

        var shortcutsRowKeyboardShortcut = Shortcut.builder()
                .setTrigger(ShortcutTrigger.parseString(Constants.GTK.Shortcuts.SHORTCUTS))
                .setAction(ShortcutAction.parseString(Constants.GTK.Actions.SHORTCUT_SHORTCUTS))
                .build();

        var aboutRowKeyboardShortcut = Shortcut.builder()
                .setTrigger(ShortcutTrigger.parseString(Constants.GTK.Shortcuts.ABOUT))
                .setAction(ShortcutAction.parseString(Constants.GTK.Actions.SHORTCUT_ABOUT))
                .build();

        // Adding all the keyboard shortcuts to the keyboard shortcuts controller
        keyboardShortcutController.addShortcut(statusRowKeyboardShortcut);
        keyboardShortcutController.addShortcut(settingsRowKeyboardShortcut);
        keyboardShortcutController.addShortcut(shortcutsRowKeyboardShortcut);
        keyboardShortcutController.addShortcut(aboutRowKeyboardShortcut);
        return keyboardShortcutController;
    }

    private void triggerStatusRow() {
        LEDSuite.logger.debug("User click: status row");
        new com.toxicstoxm.LEDSuite.ui.StatusDialog().present(this);
    }

    private void triggerSettingsRow() {
        LEDSuite.logger.debug("User click: settings row");
        getSettingsDialog().present(this);
    }

    private void triggerShortcutRow() {
        LEDSuite.logger.debug("User click: shortcut row");
        var shortcuts = ShortcutsWindow.builder().build();
        var shortcutSection = ShortcutsSection.builder().build();
        var generalGroup = ShortcutsGroup.builder().setTitle(LEDSuite.i18n("shortcut_section_general")).build();

        generalGroup.addShortcut(
                ShortcutsShortcut.builder()
                        .setShortcutType(ShortcutType.ACCELERATOR)
                        .setAccelerator(Constants.GTK.Shortcuts.SIDEBAR)
                        .setTitle(LEDSuite.i18n("sidebar_toggle_shortcut"))
                        .build()
        );

        var menuGroup = ShortcutsGroup.builder()
                .setTitle(LEDSuite.i18n("shortcut_section_main_menu"))
                .build();
        menuGroup.addShortcut(
                ShortcutsShortcut.builder()
                        .setShortcutType(ShortcutType.ACCELERATOR)
                        .setAccelerator(Constants.GTK.Shortcuts.STATUS)
                        .setTitle(LEDSuite.i18n("status_dialog_shortcut"))
                        .build()
        );
        menuGroup.addShortcut(
                ShortcutsShortcut.builder()
                        .setShortcutType(ShortcutType.ACCELERATOR)
                        .setAccelerator(Constants.GTK.Shortcuts.SETTINGS)
                        .setTitle(LEDSuite.i18n("settings_dialog_shortcut"))
                        .build()
        );
        menuGroup.addShortcut(
                ShortcutsShortcut.builder()
                        .setShortcutType(ShortcutType.ACCELERATOR)
                        .setAccelerator(Constants.GTK.Shortcuts.SHORTCUTS)
                        .setTitle(LEDSuite.i18n("shortcut_dialog_shortcut"))
                        .build()
        );
        menuGroup.addShortcut(
                ShortcutsShortcut.builder()
                        .setShortcutType(ShortcutType.ACCELERATOR)
                        .setAccelerator(Constants.GTK.Shortcuts.ABOUT)
                        .setTitle(LEDSuite.i18n("about_dialog_shortcut"))
                        .build()
        );

        shortcutSection.addGroup(generalGroup);
        shortcutSection.addGroup(menuGroup);
        shortcuts.addSection(shortcutSection);
        shortcuts.setModal(true);
        shortcuts.setTransientFor(this);
        shortcuts.present();

    }

    private void triggerAboutRow() {
        LEDSuite.logger.debug("User click: about row");
        getAboutDialog().present(this);
    }

    public void errorFeedback(Button button, AtomicBoolean bool) {
        if (!bool.get()) return;
        bool.set(false);
        button.setCssClasses(new String[]{"circular", "destructive-action"});

        new LEDSuiteRunnable() {
            @Override
            public void run() {
                GLib.idleAddOnce(() -> {
                    button.setCssClasses(new String[]{"circular", "osd"});
                    bool.set(true);
                });
            }
        }.runTaskLaterAsynchronously(750);
    }

    // assembling about dialog
    private org.gnome.adw.AboutDialog getAboutDialog() {

        var aboutDialog = AboutDialog.builder()
                .setDevelopers(LEDSuite.i18n("developers", true))
                .setDesigners(LEDSuite.i18n("artists", true))
                .setTranslatorCredits(LEDSuite.i18n("translators"))
                .setVersion(Constants.Application.VERSION)
                .setLicenseType(License.GPL_3_0)
                .setReleaseNotes(LEDSuite.i18n("release_notes"))
                .setReleaseNotesVersion(Constants.Application.VERSION)
                .setApplicationIcon(Constants.Application.ICON)
                .setIssueUrl(Constants.Links.PROJECT_GITHUB + "issues")
                .setWebsite(Constants.Links.PROJECT_GITHUB)
                .setApplicationName(Constants.Application.NAME)
                .build();
        aboutDialog.addAcknowledgementSection(LEDSuite.i18n("special_thanks_title"), LEDSuite.i18n("special_thanks", true));

        return aboutDialog;
    }

    public void getStatus(Networking.Communication.FinishCallback callback) {
        try {
            Networking.Communication.sendYAMLDefaultHost(new YAMLMessage()
                            .setPacketType(YAMLMessage.PACKET_TYPE.request)
                            .setReplyType(YAMLMessage.REPLY_TYPE.status)
                            .build(),
                    callback,
                    new LEDSuiteProcessor() {
                        @Override
                        public void run(YAMLMessage yaml) {
                            LEDSuite.eventManager.fireEvent(new Events.Status(StatusUpdate.fromYAMLMessage(yaml)));
                        }
                    }
            );
        } catch (YAMLSerializer.TODOException | ConfigurationException | YAMLSerializer.InvalidReplyTypeException |
                 YAMLSerializer.InvalidPacketTypeException e) {
            LEDSuite.logger.error("Failed to send status request to server! Error message: " + e.getMessage());
            LEDSuite.logger.displayError(e);
        }
    }

    // status bar updater
    private LEDSuiteTask statusBarUpdater;

    // creates a new status bar updater
    private void startAutomaticStatusRequestLoop() {
        @NonNull var statusBar = cache.get(Banner.class, "statusBar");
        LEDSuite.logger.debug("Running new StatusBar update Task!");
        statusBarUpdater = new LEDSuiteGuiRunnable() {
            @Override
            public void processGui() {
                if (!statusBar.getRevealed()) return;
                getStatus(success -> {
                    if (!success) LEDSuite.eventManager.fireEvent(new Events.Status(StatusUpdate.notConnected()));
                });
            }
        }.runTaskTimerAsynchronously(0, LEDSuite.argumentsSettings.getStatusRequestClockActive());
    }

    // toggle status bar
    public void setStatusBarVisible(boolean visible) {
        @NonNull var statusBar = cache.get(Banner.class, "statusBar");
        LEDSuite.logger.debug("---------------------------------------------------------------");
        LEDSuite.logger.debug("Fulfilling StatusBarToggle: " + statusBar.getRevealed() + " >> " + visible);
        statusBar.setVisible(true);
        // if the status bar is currently turned off and should be activated
        if (!statusBar.getRevealed() && visible) {
            // A new update status task is created and started
            startAutomaticStatusRequestLoop();
        } else {
            // if the status bar is currently turned on and should be deactivated
            // if there is an updater task running
            if (statusBarUpdater != null) statusBarUpdater.cancel(); // trigger the tasks kill switch
        }
        // toggle status bar visibility based on the provided value 'visible'
        statusBar.setRevealed(visible);
        // update user settings to match new value
        if (LEDSuite.mainWindow != null) LEDSuite.settings.setDisplayStatusBar(visible);
        LEDSuite.logger.debug("---------------------------------------------------------------");
    }

    // settings dialog
    private com.toxicstoxm.LEDSuite.ui.SettingsDialog sD = null;

    // method to either create a new settings dialog or get an already existing one
    // this ensures that only one settings dialog is created to prevent the app unnecessarily using up system resources
    private com.toxicstoxm.LEDSuite.ui.SettingsDialog getSettingsDialog() {
        // checking if an existing about dialog can be reused
        if (sD == null) {
            // if not, a new one is created
            sD = new com.toxicstoxm.LEDSuite.ui.SettingsDialog();
        }
        return sD;
    }

    public void resetSettingsDialog() {
        this.sD = null;
    }

    public void updateStatusBar(StatusUpdate statusUpdate) {
        @NonNull var statusBar = cache.get(Banner.class, "statusBar");
        @NonNull var controlButtonsRevealer = cache.get(Revealer.class, "controlButtonsRevealer");
        statusBar.setTitle(statusUpdate.minimal());
        if (statusUpdate.isNotConnected() && controlButtonsRevealer != null)
            controlButtonsRevealer.setRevealChild(false);
    }

    public ListBoxRow listBoxWrap(Widget widget) {
        return ListBoxRow.builder()
                .setSelectable(true)
                .setName(widget.getName())
                .setChild(
                        widget
                ).build();
    }

    public void playing(Button playPauseButton, Revealer stopButtonRevealer) {
        stopButtonRevealer.setRevealChild(true);
        playPauseButton.setName("pause");
        playPauseButton.setIconName("media-playback-pause-symbolic");
    }

    public void paused(Button playPauseButton, Revealer stopButtonRevealer) {
        stopButtonRevealer.setRevealChild(true);
        playPauseButton.setName("play");
        playPauseButton.setIconName("media-playback-start-symbolic");
    }

    public void idle(Button playPauseButton, Revealer stopButtonRevealer) {
        stopButtonRevealer.setRevealChild(false);
        playPauseButton.setName("play");
        playPauseButton.setIconName("media-playback-start-symbolic");
    }

    public void setControlButtons(StatusUpdate statusUpdate, Map.Entry<String, String> name, Button playPauseButton, Revealer stopButtonRevealer) {
        @NonNull var controlButtonsRevealer = cache.get(Revealer.class, "controlButtonsRevealer");
        controlButtonsRevealer.setRevealChild(false);
        if (statusUpdate.isNotConnected()) {
            return;
        }
        if (statusUpdate.isFileLoaded()) {
            if (!statusUpdate.getFileSelected().equals(name.getKey())) {
                idle(playPauseButton, stopButtonRevealer);
                controlButtonsRevealer.setRevealChild(true);
                return;
            }
            switch (statusUpdate.getFileState()) {
                case YAMLMessage.FILE_STATE.playing -> playing(playPauseButton, stopButtonRevealer);
                case YAMLMessage.FILE_STATE.paused -> paused(playPauseButton, stopButtonRevealer);
                default -> idle(playPauseButton, stopButtonRevealer);
            }
        } else idle(playPauseButton, stopButtonRevealer);
        controlButtonsRevealer.setRevealChild(true);
    }

    @EventHandler
    public void onStatus(Events.Status e) {
        @NonNull var sidebarAnimationSection = cache.get(ListBox.class, "sidebarAnimationSection");
        @NonNull var sidebarSpinnerRevealer = cache.get(Revealer.class, "sidebarSpinnerRevealer");
        GLib.idleAddOnce(() -> {
            try {
                StatusUpdate statusUpdate = e.statusUpdate();
                updateStatusBar(statusUpdate);

                ListBoxRow selectedRow = sidebarAnimationSection.getSelectedRow();
                String name = "";
                if (selectedRow != null) name = selectedRow.getName();
                if (!name.isBlank()) {
                    var playPauseButton = cache.get(Button.class, "playPauseButton");
                    var stopButtonRevealer = cache.get(Revealer.class, "stopButtonRevealer");
                    if (playPauseButton != null && stopButtonRevealer != null) {
                        setControlButtons(statusUpdate, currentAnimation, playPauseButton, stopButtonRevealer);
                    }
                }
                if (name.isBlank() || TimeManager.call("animations")) {
                    sidebarAnimationSection.unselectAll();
                    sidebarAnimationSection.setSelectionMode(SelectionMode.NONE);
                    sidebarAnimationSection.removeAll();

                    sidebarSpinnerRevealer.setRevealChild(true);

                    List<ListBoxRow> anims = new ArrayList<>();

                    availableAnimations = statusUpdate.getAvailableAnimations();
                    if (availableAnimations != null) {
                        for (Map.Entry<String, String> entry : availableAnimations.entrySet()) {
                            var availableAnimation = Box.builder()
                                    .setOrientation(Orientation.HORIZONTAL)
                                    .setTooltipText(LEDSuite.i18n("animations_tooltip", "%ANIMATION_NAME%", entry.getKey()))
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
                        sidebarSpinnerRevealer.setRevealChild(false);
                        for (ListBoxRow lbr : anims) {
                            sidebarAnimationSection.append(lbr);
                        }
                    }
                    sidebarAnimationSection.setSelectionMode(SelectionMode.BROWSE);
                    sidebarAnimationSection.selectRow(selectedRow);
                }
            } catch (NumberFormatException ex) {
                LEDSuite.logger.warn("Status update failed!");
                LEDSuite.logger.displayError(ex);
            }
        });
    }

    @EventHandler
    public void onStarted(Events.Started e) {
        @NonNull var sidebarFileSection = cache.get(ListBox.class, "sidebarFileSection");
        sidebarFileSection.emitRowSelected(sidebarFileSection.getRowAtIndex(0));
        sidebarFileSection.emitRowActivated(sidebarFileSection.getRowAtIndex(0));
        sidebarFileSection.emitSelectedRowsChanged();
    }
}