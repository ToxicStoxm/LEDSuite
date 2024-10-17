package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.time.Action;
import com.toxicstoxm.LEDSuite.time.CooldownManger;
import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.Types;
import org.gnome.gio.SimpleAction;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.Application;
import org.gnome.gtk.Image;
import org.gnome.gtk.Label;
import org.gnome.gtk.ListBoxRow;
import org.jetbrains.annotations.NotNull;

import java.lang.foreign.MemorySegment;

@GtkTemplate(name = "AnimationRow", ui = "/com/toxicstoxm/LEDSuite/AnimationRow.ui")
public class AnimationRow extends ListBoxRow {

    private static final Type gtype = Types.register(AnimationRow.class);

    public AnimationRow(MemorySegment address) {
        super(address);
    }

    public static Type getType() {
        return gtype;
    }

    @GtkChild(name = "animation_icon")
    public Image animationIcon;

    public void setIconName(String iconName) {
        animationIcon.setFromIconName(iconName);
    }

    @GtkChild(name = "animation_label")
    public Label animationLabel;

    public void setAnimationLabel(String animationLabel) {
        this.animationLabel.setLabel(animationLabel);
    }

    public static AnimationRow create() {
        return GObject.newInstance(getType());
    }

    public static @NotNull AnimationRow create(String iconName, String label) {
        AnimationRow row = GObject.newInstance(getType());
        row.setIconName(iconName);
        row.setAnimationLabel(label);
        return row;
    }

    public static @NotNull AnimationRow create(String iconName, String label, String actionName) {
        AnimationRow row = GObject.newInstance(getType(), "action-name", actionName);
        row.setIconName(iconName);
        row.setAnimationLabel(label);
        return row;
    }

    public static @NotNull AnimationRow create(Application app, String iconName, String label, Action action) {
        return create(app, iconName, label, action, 500);
    }


    public static @NotNull AnimationRow create(@NotNull Application app, String iconName, String label, Action action, long cooldown) {
        CooldownManger.addAction(label, action, cooldown);
        var simpleAction = new SimpleAction(label, null);
        simpleAction.onActivate(_ -> {
            if (!CooldownManger.call(label)) {
                LEDSuiteApplication.getLogger().info("The animation row " + label + " is on cooldown!");
            } else ((LEDSuiteWindow) app.getActiveWindow()).fileManagementList.unselectAll();
        });
        app.addAction(simpleAction);

        AnimationRow row = GObject.newInstance(getType(), "action-name", "app." + label);
        row.setIconName(iconName);
        row.setAnimationLabel(label);
        return row;
    }

}
