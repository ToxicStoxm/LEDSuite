package com.toxicstoxm.LEDSuite.ui.animation_menu;

import com.toxicstoxm.LEDSuite.ui.AnimationRow;
import com.toxicstoxm.LEDSuite.ui.animation_menu.widgets.GroupWidget;
import io.github.jwharm.javagi.gtk.types.Types;
import lombok.Getter;
import lombok.Setter;
import org.gnome.adw.PreferencesPage;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;

import java.lang.foreign.MemorySegment;
import java.util.List;

@Getter
@Setter
public class AnimationMenu extends PreferencesPage {

    private String menuID;
    private String subtitle;
    private String title;

    private List<GroupWidget> topLevelGroups;

    private static final Type gtype = Types.register(AnimationMenu.class);

    public AnimationMenu(MemorySegment address) {
        super(address);
    }

    public static Type getType() {
        return gtype;
    }

    public static AnimationMenu create(String menuID, List<GroupWidget> topLevelGroups, CallbackRelay callbackRelay) {
        AnimationRow row = AnimationRow.getAnimationRow(menuID);
        AnimationMenu menu = GObject.newInstance(getType());
        menu.setMenuID(menuID);
        menu.setIconName(row.animationIcon.getIconName());
        menu.setTitle(row.animationRowLabel.getLabel());
        menu.setTopLevelGroups(topLevelGroups);

        for (GroupWidget topLevelGroup : topLevelGroups) {
            menu.add(topLevelGroup.asAdwaitaWidget(callbackRelay));
        }

        return GObject.newInstance(getType());
    }

}
