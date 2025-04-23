package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.MenuRequestPacket;
import com.toxicstoxm.LEDSuite.time.CooldownManager;
import com.toxicstoxm.LEDSuite.tools.YamlTools;
import com.toxicstoxm.LEDSuite.ui.animation_menu.AnimationMenuReference;
import com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs.ErrorData;
import com.toxicstoxm.YAJL.Logger;
import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import lombok.Getter;
import lombok.Setter;
import org.gnome.adw.Clamp;
import org.gnome.adw.Spinner;
import org.gnome.gio.SimpleAction;
import org.gnome.glib.GLib;
import org.gnome.gtk.*;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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

    private static final Logger logger = Logger.autoConfigureLogger();

    @Getter
    private String animationID = "";  // Unique identifier for the animation

    @Getter
    @Setter
    private Long lastAccessed = System.currentTimeMillis();

    @Getter
    private boolean pauseable = false;

    public void setRenamePending(boolean renamePending) {
        this.renamePending.set(renamePending);
    }

    public boolean getRenamePending() {
        return renamePending.get();
    }

    private final AtomicBoolean renamePending = new AtomicBoolean(false);

    /**
     * The {@link Image} widget used to display the animation's icon.
     */
    @GtkChild(name = "animation_icon")
    public Image animationIcon;

    private String iconHash = "";

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
        logger.verbose("'{}' -> Updating label to '{}'", animationID, animationLabel);
        this.animationRowLabel.setLabel(animationLabel);
    }

    /**
     * Creates a new {@link AnimationRow} from the given {@link AnimationRowData}.
     * This method initializes the row with an icon, label, and other relevant data.
     *
     * @param animationRowData The data used to populate the animation row.
     */
    public AnimationRow(@NotNull AnimationRowData animationRowData) {
        super("action-name", "app." + animationRowData.animationID());
        logger.verbose("'{}' -> Creating new animation row from animation", animationRowData.animationID());

        logger.verbose("'{}' -> Registering click trigger with cooldown", animationRowData.animationID());
        // Add the action to the cooldown manager with the associated cooldown, if provided.
        CooldownManager.addAction(
                animationRowData.animationID(),
                animationRowData.action(),
                animationRowData.cooldown() != null ? animationRowData.cooldown() : 0
        );

        logger.verbose("'{}' -> Creating click trigger action", animationRowData.animationID());
        // Create the action for the application and bind it to the animation row.
        createAction(animationRowData.app(), animationRowData.animationID(), animationRowData.label());

        logger.verbose("'{}' -> Configuring UI state", animationRowData.animationID());
        animationID = animationRowData.animationID();
        update(null, animationRowData.iconString(), animationRowData.iconIsName(), null);
        setLastAccessed(animationRowData.lastAccessed());
        setAnimationLabel(animationRowData.label().strip());
        pauseable = animationRowData.pauseable();

        // Set the tooltip to show the animation ID when hovered.
        setTooltipText(animationRowData.animationID());

        logger.verbose("'{}' -> Instance configured and ready for usage", animationRowData.animationID());
    }

    /**
     * Updates the label and icon of the animation row.
     * This method can be called to refresh the row's display.
     *
     * @param label The new label to display.
     * @param iconString The new icon string.
     * @param lastAccessed The last time this animation was accessed
     */
    public void update(String label, String iconString, boolean isName, Long lastAccessed) {
        logger.verbose("'{}' -> Update triggered", animationID);
        if (label != null) {
            animationRowLabel.setLabel(label);
        }
        logger.verbose("'{}' -> Updating icon: getting updated icon", animationID);
        AtomicReference<String> newIconHash = new AtomicReference<>("");
        Image newIcon = YamlTools.constructIcon(iconString, isName, iconHash, () -> {
            try {
                newIconHash.set(new String(MessageDigest.getInstance("md5").digest(iconString.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
            } catch (NoSuchAlgorithmException e) {
                LEDSuiteApplication.handleError(ErrorData.builder()
                                .logger(logger)
                                .message(logger.format("Failed to get hashSum algorithm 'md5' for computing icon hashSum within {}(ID:{})", getClass().getSimpleName(), getAnimationID()))
                                .heading("Error while receiving status update")
                        .build());
            }
            return newIconHash.get();
        });

        if (newIcon != null) {
            if (isName) {
                logger.verbose("'{}' -> Updating icon: by name '{}'", animationID, newIcon.getIconName());
                animationIcon.setFromIconName(newIcon.getIconName());
            } else {
                logger.verbose("'{}' -> Updating icon: by base64 encoded image", animationID);
                animationIcon.setFromPaintable(newIcon.getPaintable());
            }
            logger.verbose("'{}' -> Updating icon: saving hash '{}'", animationID, newIconHash.get());
            iconHash = newIconHash.get();
        } else {
            logger.verbose("'{}' -> Updating icon: cancelled update because hash matches current icon", animationID);
        }
        if (lastAccessed != null) setLastAccessed(lastAccessed);

        // If the row is part of a menu, update the menu as well.
        if (animationMenuReference != null) {
            logger.verbose("'{}' -> Updating icon: menu reference found, updating it", animationID);
            animationMenuReference.updateLabel(label);
            animationMenuReference.updateIcon(newIcon);
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
    private void createAction(@NotNull Application app, String animationID, String animationLabel) {
        logger.verbose("'{}' -> Creating trigger action", animationID);
        LEDSuiteWindow window = (LEDSuiteWindow) app.getActiveWindow();
        var simpleAction = new SimpleAction(String.valueOf(animationID), null);

        // Define the action's behavior when activated (clicked).
        simpleAction.onActivate(_ -> {
            // Check if the animation is on cooldown.
            if (!CooldownManager.call(String.valueOf(animationID))) {
                logger.warn("The animation row {} ({}) is on cooldown!", animationLabel, animationID);
            } else {
                if (renamePending.get()) {
                    logger.debug("Menu request for animation '{}' was denied because a rename request for this animation is pending!", animationID);
                    return;
                }
                // If the cooldown has passed, update the selection and request the menu.
                GLib.idleAddOnce(() -> {
                    window.fileManagementList.unselectAll();  // Unselect all items in the file management list
                    window.setSelectedAnimation(animationID);  // Select the current animation
                });

                logger.verbose("Requesting menu for animation '{}'", animationID);

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

        logger.verbose("'{}' -> Registering trigger action within app", animationID);

        // Add the action to the application.
        app.addAction(simpleAction);
    }
}
