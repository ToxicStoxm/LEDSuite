package com.toxicstoxm.LEDSuite.ui.animation_menu;

import com.toxicstoxm.LEDSuite.ui.AnimationRow;
import io.github.jwharm.javagi.gobject.annotations.Property;
import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.TemplateTypes;
import lombok.Getter;
import lombok.Setter;
import org.gnome.glib.GLib;
import org.gnome.gobject.GObject;
import org.gnome.gtk.Box;
import org.gnome.gtk.Image;
import org.gnome.gtk.ImageType;
import org.gnome.gtk.Label;
import org.jetbrains.annotations.NotNull;

import java.lang.foreign.MemorySegment;

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

    static {
        TemplateTypes.register(AnimationMenu.class);
    }

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
     * Constructor that initializes the AnimationMenu using a memory address segment.
     *
     * @param address a {@link MemorySegment} representing the address to initialize the widget
     */
    public AnimationMenu(MemorySegment address) {
        super(address);
    }

    /**
     * Creates a new instance of the {@code AnimationMenu} with the specified file name.
     * This method also sets the {@code menuID} to the provided file name.
     *
     * @param fileName the name of the file to associate with the menu instance
     * @return a newly created {@code AnimationMenu} instance
     */
    public static @NotNull AnimationMenu create(String fileName) {
        AnimationMenu menu = GObject.newInstance(AnimationMenu.class);
        menu.setMenuID(fileName);
        return menu;
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
        });
        return this;
    }

    @Override
    public void updateLabel(String label) {
        GLib.idleAddOnce(() -> {
            if (label != null) {
                animationLabel.setLabel(label);
            }
        });
    }

    @Override
    public void updateIconName(String iconName) {
        GLib.idleAddOnce(() -> {
            if (iconName != null) {
                animationMenuImage.setFromIconName(iconName);
            }
        });
    }
}
