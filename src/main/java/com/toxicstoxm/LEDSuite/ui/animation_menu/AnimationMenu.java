package com.toxicstoxm.LEDSuite.ui.animation_menu;

import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.Types;
import lombok.Getter;
import lombok.Setter;
import org.gnome.adw.ActionRow;
import org.gnome.adw.PreferencesGroup;
import org.gnome.adw.PreferencesPage;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;

import java.lang.foreign.MemorySegment;


/**
 * Animation menu template class.
 * Represents an animation settings menu.
 * <br>Template file: {@code AnimationMenu.ui}
 * @since 1.0.0
 */
@Getter
@Setter
@GtkTemplate(name = "AnimationMenu", ui = "7com/toxicstoxm/LEDSuite/AnimationMenu.ui")
public class AnimationMenu extends PreferencesPage {

    private String menuID;
    private String subtitle;
    private String title;

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
        pref.add(ActionRow.builder().setCssClasses(new String[]{"property"}).setTitle("Hello").setSubtitle("World").build());
        this.add(pref);
        return this;
    }

}
