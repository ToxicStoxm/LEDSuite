package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.MenuRequestPacket;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.time.Action;
import com.toxicstoxm.LEDSuite.time.CooldownManger;
import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.Types;
import lombok.Getter;
import org.gnome.gio.SimpleAction;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.Application;
import org.gnome.gtk.Image;
import org.gnome.gtk.Label;
import org.gnome.gtk.ListBoxRow;
import org.jetbrains.annotations.NotNull;

import java.lang.foreign.MemorySegment;
import java.util.HashMap;

@GtkTemplate(name = "AnimationRow", ui = "/com/toxicstoxm/LEDSuite/AnimationRow.ui")
public class AnimationRow extends ListBoxRow {

    private static final Type gtype = Types.register(AnimationRow.class);

    public AnimationRow(MemorySegment address) {
        super(address);
    }

    public static Type getType() {
        return gtype;
    }

    private static final HashMap<String, AnimationRow> animationRowNames = new HashMap<>();

    @Getter
    private String rowID = "";

    @GtkChild(name = "animation_icon")
    public Image animationIcon;

    public final void setIconName(String iconName) {
        animationIcon.setFromIconName(iconName);
    }

    @GtkChild(name = "animation_label")
    public Label animationRowLabel;

    public final void setAnimationLabel(String animationLabel) {
        this.animationRowLabel.setLabel(animationLabel);
    }

    private static AnimationRow create() {
        return GObject.newInstance(getType());
    }

    public static @NotNull AnimationRow create(String iconName, String label) {
        AnimationRow row = create();
        row.setIconName(iconName);
        row.setAnimationLabel(label);
        return row;
    }

    public static @NotNull AnimationRow create(Application app, String iconName, String label, String rowID, Action action) {
        return create(app, iconName, label, rowID, action, 500);
    }

    public static @NotNull AnimationRow create(@NotNull Application app, String iconName, String label, String rowID, Action action, long cooldown) {
        CooldownManger.addAction(rowID, action, cooldown);
        createAction(app, rowID, label);

        AnimationRow row = GObject.newInstance(getType(), "action-name", "app." + rowID);
        row.rowID = rowID;
        row.setIconName(iconName);
        row.setAnimationLabel(label);

        String animationRowIdentifier = label.toLowerCase();

        AnimationRow otherRow = animationRowNames.get(animationRowIdentifier);

        if (otherRow != null) {
            otherRow.setTooltipText(getRowID(otherRow.rowID));
            row.setTooltipText(getRowID(rowID));
        }
        else animationRowNames.putIfAbsent(animationRowIdentifier, row);
        return row;
    }

    private static void createAction(Application app, String rowID, String animationLabel) {
        LEDSuiteWindow window = (LEDSuiteWindow) app.getActiveWindow();
        var simpleAction = new SimpleAction(String.valueOf(rowID), null);
        simpleAction.onActivate(_ -> {
            if (!CooldownManger.call(String.valueOf(rowID))) {
                LEDSuiteApplication.getLogger().info("The animation row " + animationLabel + " (" + rowID + ") is on cooldown!", new LEDSuiteLogAreas.USER_INTERACTIONS());
            } else {
                window.fileManagementList.unselectAll();
                LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                        MenuRequestPacket.builder()
                                .requestFile(rowID)
                                .build().serialize()
                );
            }
        });
        app.addAction(simpleAction);
    }

    private static String getRowID(String rowID) {
        return "crc32: '" + rowID + "'";
    }

}
