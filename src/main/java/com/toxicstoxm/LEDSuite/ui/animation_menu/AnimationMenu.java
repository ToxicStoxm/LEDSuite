package com.toxicstoxm.LEDSuite.ui.animation_menu;

import io.github.jwharm.javagi.gtk.types.Types;
import lombok.Getter;
import lombok.Setter;
import org.gnome.adw.PreferencesPage;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;

import java.lang.foreign.MemorySegment;

public class AnimationMenu extends PreferencesPage {

    @Setter
    @Getter
    private String menuID;

    private static final Type gtype = Types.register(AnimationMenu.class);

    public AnimationMenu(MemorySegment address) {
        super(address);
    }

    public static Type getType() {
        return gtype;
    }

    public static AnimationMenu create(String menuID) {
        AnimationMenu menu = GObject.newInstance(getType());
        menu.setMenuID(menuID);
        return GObject.newInstance(getType());
    }
}
