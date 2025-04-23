package com.toxicstoxm.LEDSuite.ui.animation_menu;

import com.toxicstoxm.LEDSuite.ui.AnimationRow;
import com.toxicstoxm.YAJL.Logger;
import io.github.jwharm.javagi.gobject.annotations.Property;
import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import lombok.Getter;
import lombok.Setter;
import org.gnome.glib.GLib;
import org.gnome.gtk.Box;
import org.gnome.gtk.Image;
import org.gnome.gtk.ImageType;
import org.gnome.gtk.Label;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the settings menu for animations within the application.
 * <p>
 * This class defines a template for the animation menu and provides methods
 * to initialize and configure the components within the menu. The UI is defined
 * in the {@code AnimationMenu.ui} template file.
 * </p>
 *
 * @since 1.0.0
 */
@Getter(onMethod_ = @Property(skip = true))
@Setter(onMethod_ = @Property(skip = true))
@GtkTemplate(name = "AnimationMenu", ui = "/com/toxicstoxm/LEDSuite/AnimationMenu.ui")
public class AnimationMenu extends Box implements AnimationMenuReference {

    private static final Logger logger = Logger.autoConfigureLogger();

    /**
     * Unique identifier for the menu instance.
     */
    private String menuID;

    /**
     * Subtitle text for the menu.
     */
    private String subtitle;

    /**
     * Title text for the menu.
     */
    private String title;

    @GtkChild(name = "animation_menu_content")
    public Box animationMenuContent;

    @GtkChild(name = "animation_menu_image")
    public Image animationMenuImage;

    @GtkChild(name = "animation_menu_label")
    public Label animationLabel;

    @GtkChild(name = "animation_menu_subtitle")
    public Label animationSubtitle;


    /**
     * Creates a new instance of the {@code AnimationMenu} with the specified file name.
     * This method also sets the {@code menuID} to the provided file name.
     *
     * @param fileName the name of the file to associate with the menu instance
     */
    public AnimationMenu(String fileName) {
        super();
        setMenuID(fileName);
    }

    /**
     * Initializes the menu with the provided animation row data.
     * <p>
     * This method sets the menu label, subtitle, and image according to the properties
     * in the provided {@link AnimationRow}.
     * </p>
     *
     * @param row the {@link AnimationRow} containing data for initializing the menu
     * @return the initialized {@code AnimationMenu} instance
     */
    public AnimationMenu init(@NotNull AnimationRow row) {
        logger.verbose("Instance init");
        GLib.idleAddOnce(() -> {
            // Set the label of the animation menu
            animationLabel.setLabel(row.animationRowLabel.getLabel());

            // Determine the image storage type and set the image accordingly
            ImageType storageType = row.animationIcon.getStorageType();
            if (storageType.equals(ImageType.EMPTY) || storageType.equals(ImageType.PAINTABLE)) {
                animationMenuImage.setFromPaintable(row.animationIcon.getPaintable());
            } else {
                animationMenuImage.setFromIconName(row.animationIcon.getIconName());
            }
            logger.verbose("Instance initialized, ready");
        });
        return this;
    }

    @Override
    public void updateLabel(String label) {
        logger.verbose("Updating label to -> '{}'", label);
        GLib.idleAddOnce(() -> {
            if (label != null) {
                animationLabel.setLabel(label);
            }
        });
    }

    @Override
    public void updateIcon(Image icon) {
        GLib.idleAddOnce(() -> {
            if (icon != null) {
                ImageType storageType = icon.getStorageType();
                if (storageType == ImageType.EMPTY || storageType == ImageType.ICON_NAME) {
                    animationMenuImage.setFromIconName(icon.getIconName());
                    logger.verbose("Updating icon to -> '{}'", icon.getIconName());
                } else {
                    animationMenuImage.setFromPaintable(icon.getPaintable());
                    logger.verbose("Updating icon");
                }
            }
        });
    }
}
