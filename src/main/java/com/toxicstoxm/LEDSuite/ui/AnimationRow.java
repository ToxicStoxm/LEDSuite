package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.MenuRequestPacket;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.time.CooldownManager;
import com.toxicstoxm.LEDSuite.ui.animation_menu.AnimationMenuReference;
import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.TemplateTypes;
import lombok.Getter;
import lombok.Setter;
import org.gnome.adw.Clamp;
import org.gnome.adw.Spinner;
import org.gnome.gio.SimpleAction;
import org.gnome.glib.GLib;
import org.gnome.gobject.GObject;
import org.gnome.gtk.*;
import org.jetbrains.annotations.NotNull;

import java.lang.foreign.MemorySegment;

/**
 * Represents a row in the sidebar of the application, displaying information about a single animation.
 * Each row contains an icon and a label, representing the animation's ID and label, respectively.
 * The row is clickable and interacts with the animation menu.
 * <br>Template file: {@code AnimationRow.ui}
 * <p>
 * This class extends {@link ListBoxRow} and uses GTK widgets like {@link Image} and {@link Label}.
 * It also interacts with a cooldown manager and can trigger actions when clicked.
 * </p>
 *
 * @since 1.0.0
 */
@GtkTemplate(name = "AnimationRow", ui = "/com/toxicstoxm/LEDSuite/AnimationRow.ui")
public class AnimationRow extends ListBoxRow {

    static {
        TemplateTypes.register(AnimationRow.class);
    }

    /**
     * Constructs an instance of {@link AnimationRow}.
     * This constructor is used internally by GTK to create a new instance from a template.
     *
     * @param address The memory address for the GTK object.
     */
    public AnimationRow(MemorySegment address) {
        super(address);
    }

    @Getter
    private String animationID = "";  // Unique identifier for the animation

    @Getter
    @Setter
    private Long lastAccessed = System.currentTimeMillis();

    @Getter
    private boolean pauseable = false;

    /**
     * The {@link Image} widget used to display the animation's icon.
     */
    @GtkChild(name = "animation_icon")
    public Image animationIcon;

    /**
     * Sets the animation icons paintable or icon name depending on the provided icons structure.
     *
     * @param icon the new icon to use
     */
    public final void setIcon(@NotNull Image icon) {
        ImageType storageType = icon.getStorageType();
        if (storageType == ImageType.EMPTY || storageType == ImageType.ICON_NAME) {
            animationIcon.setFromIconName(icon.getIconName());
        } else {
            animationIcon.setFromPaintable(icon.getPaintable());
        }
    }

    /**
     * The {@link Label} widget used to display the animation's label.
     */
    @GtkChild(name = "animation_label")
    public Label animationRowLabel;

    @Setter
    private AnimationMenuReference animationMenuReference; // Reference to the menu associated with this animation

    /**
     * Sets the label for the animation row.
     * This label will be displayed in the row.
     *
     * @param animationLabel The label to set for the animation.
     */
    public final void setAnimationLabel(String animationLabel) {
        this.animationRowLabel.setLabel(animationLabel);
    }

    /**
     * Creates a new {@link AnimationRow} from the given {@link AnimationRowData}.
     * This method initializes the row with an icon, label, and other relevant data.
     *
     * @param animationRowData The data used to populate the animation row.
     * @return The newly created {@link AnimationRow}.
     */
    public static @NotNull AnimationRow create(@NotNull AnimationRowData animationRowData) {
        // Add the action to the cooldown manager with the associated cooldown, if provided.
        CooldownManager.addAction(
                animationRowData.animationID(),
                animationRowData.action(),
                animationRowData.cooldown() != null ? animationRowData.cooldown() : 0,
                null
        );

        // Create the action for the application and bind it to the animation row.
        createAction(animationRowData.app(), animationRowData.animationID(), animationRowData.label());

        // Instantiate and configure the AnimationRow with the provided data.
        AnimationRow row = GObject.newInstance(AnimationRow.class, "action-name", "app." + animationRowData.animationID());
        row.animationID = animationRowData.animationID();
        row.setIcon(animationRowData.icon());
        row.setLastAccessed(animationRowData.lastAccessed());
        row.setAnimationLabel(animationRowData.label().strip());
        row.animationRowLabel.setWrap(true);  // Allow label to wrap if it's too long
        row.animationRowLabel.setWidthChars(10);  // Set a maximum width for the label
        row.pauseable = animationRowData.pauseable();

        // Set the tooltip to show the animation ID when hovered.
        row.setTooltipText(animationRowData.animationID());
        return row;
    }

    /**
     * Updates the label and icon of the animation row.
     * This method can be called to refresh the row's display.
     *
     * @param label The new label to display.
     * @param iconName The new icon name to display.
     * @param lastAccessed The last time this animation was accessed
     */
    public void update(String label, String iconName, Long lastAccessed) {
        animationRowLabel.setLabel(label);
        animationIcon.setFromIconName(iconName);
        setLastAccessed(lastAccessed);

        // If the row is part of a menu, update the menu as well.
        if (animationMenuReference != null) {
            animationMenuReference.updateLabel(label);
            animationMenuReference.updateIconName(iconName);
        }
    }

    /**
     * Creates an action for the given animation, which is triggered when the row is clicked.
     * The action is associated with the animation's ID and label.
     * If the action is triggered, it checks the cooldown and, if ready, selects the animation and requests the menu.
     *
     * @param app The {@link Application} instance to which the action will be added.
     * @param animationID The unique ID of the animation.
     * @param animationLabel The label to be displayed for the animation.
     */
    private static void createAction(@NotNull Application app, String animationID, String animationLabel) {
        LEDSuiteWindow window = (LEDSuiteWindow) app.getActiveWindow();
        var simpleAction = new SimpleAction(String.valueOf(animationID), null);

        // Define the action's behavior when activated (clicked).
        simpleAction.onActivate(_ -> {
            // Check if the animation is on cooldown.
            if (!CooldownManager.call(String.valueOf(animationID))) {
                LEDSuiteApplication.getLogger().info(
                        "The animation row " + animationLabel + " (" + animationID + ") is on cooldown!",
                        new LEDSuiteLogAreas.USER_INTERACTIONS()
                );
            } else {
                // If the cooldown has passed, update the selection and request the menu.
                GLib.idleAddOnce(() -> {
                    window.fileManagementList.unselectAll();  // Unselect all items in the file management list
                    window.setSelectedAnimation(animationID);  // Select the current animation
                });

                LEDSuiteApplication.getLogger().verbose(
                        "Requesting menu for animation '" + animationID + "'",
                        new LEDSuiteLogAreas.USER_INTERACTIONS()
                );

                LEDSuiteApplication.getWindow().changeMainContent(
                        Clamp.builder()
                                .setChild(Spinner.builder().build())
                                .setMaximumSize(50)
                                .setHalign(Align.CENTER)
                                .setHexpand(true)
                                .setTighteningThreshold(50)
                                .build()
                );

                LEDSuiteApplication.getWindow().setAnimationControlButtonsVisible(false);

                // Enqueue a request for the animation's menu.
                LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                        MenuRequestPacket.builder()
                                .requestFile(animationID)
                                .build().serialize()
                );
            }
        });

        // Add the action to the application.
        app.addAction(simpleAction);
    }
}
