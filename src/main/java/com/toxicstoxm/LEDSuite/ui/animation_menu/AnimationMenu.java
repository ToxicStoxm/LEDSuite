package com.toxicstoxm.LEDSuite.ui.animation_menu;

import com.toxicstoxm.LEDSuite.ui.AnimationRow;
import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.TemplateTypes;
import lombok.Getter;
import lombok.Setter;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.Box;
import org.gnome.gtk.Image;
import org.gnome.gtk.ImageType;
import org.gnome.gtk.Label;
import org.jetbrains.annotations.NotNull;

import java.lang.foreign.MemorySegment;


/**
 * Animation menu template class.
 * Represents an animation settings menu.
 * <br>Template file: {@code AnimationMenu.ui}
 * @since 1.0.0
 */
@Getter
@Setter
@GtkTemplate(name = "AnimationMenu", ui = "/com/toxicstoxm/LEDSuite/AnimationMenu.ui")
public class AnimationMenu extends Box {

    private String menuID;
    private String subtitle;
    private String title;

    @GtkChild(name = "animation_menu_content")
    public Box animationMenuContent;

    @GtkChild(name = "animation_menu_image")
    public Image animationMenuImage;

    @GtkChild(name = "animation_menu_label")
    public Label animationLabel;

    @GtkChild(name = "animation_menu_subtitle")
    public Label animationSubtitle;

    private static final Type gtype = TemplateTypes.register(AnimationMenu.class);

    public AnimationMenu(MemorySegment address) {
        super(address);
    }

    public static Type getType() {
        return gtype;
    }

    public static @NotNull AnimationMenu create(String fileName) {
        AnimationMenu menu = GObject.newInstance(getType());
        menu.setMenuID(fileName);
        return menu;
    }

    public AnimationMenu init(@NotNull AnimationRow row) {
        animationLabel.setLabel(row.animationRowLabel.getLabel());
        var storageType = row.animationIcon.getStorageType();
        if (storageType.equals(ImageType.EMPTY) || storageType.equals(ImageType.PAINTABLE)) animationMenuImage.setFromPaintable(row.animationIcon.getPaintable());
        else animationMenuImage.setFromIconName(row.animationIcon.getIconName());
        return this;
    }

}
