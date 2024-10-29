package com.toxicstoxm.LEDSuite.ui.animation_menu;

import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.Types;
import lombok.Getter;
import lombok.Setter;
import org.gnome.adw.ActionRow;
import org.gnome.adw.PreferencesGroup;
import org.gnome.adw.PreferencesPage;
import org.gnome.adw.StatusPage;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.Box;
import org.gnome.gtk.Image;
import org.gnome.gtk.Label;

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

    private static final Type gtype = Types.register(AnimationMenu.class);

    public AnimationMenu(MemorySegment address) {
        super(address);
    }

    public static Type getType() {
        return gtype;
    }

    public static AnimationMenu create() {
        return GObject.newInstance(getType());
    }

    public AnimationMenu init() {
        var pref = PreferencesGroup.builder().setTitle("Hello World").build();
        for (int i = 0; i < 20; i++) {
            pref.add(ActionRow.builder().setCssClasses(new String[]{"property"}).setTitle("Hello").setSubtitleSelectable(true).setSubtitle("World").build());
        }
        animationLabel.setLabel("Test");
        animationMenuImage.setFromIconName("media-optical-cd-audio-symbolic");
        animationMenuContent.append(pref);
        return this;
    }

}
