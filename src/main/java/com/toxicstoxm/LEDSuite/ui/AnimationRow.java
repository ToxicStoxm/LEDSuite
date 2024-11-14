package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.MenuRequestPacket;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.time.CooldownManger;
import com.toxicstoxm.LEDSuite.ui.animation_menu.AnimationMenu;
import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.TemplateTypes;
import lombok.Getter;
import lombok.Setter;
import org.gnome.gio.SimpleAction;
import org.gnome.glib.GLib;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.Application;
import org.gnome.gtk.Image;
import org.gnome.gtk.Label;
import org.gnome.gtk.ListBoxRow;
import org.jetbrains.annotations.NotNull;

import java.lang.foreign.MemorySegment;

/**
 * Template class for storing a single animation.
 * This row is later displayed in the sidebar.
 * <br>Template file: {@code AnimationRow.ui}
 * @since 1.0.0
 */
@GtkTemplate(name = "AnimationRow", ui = "/com/toxicstoxm/LEDSuite/AnimationRow.ui")
public class AnimationRow extends ListBoxRow {

    private static final Type gtype = TemplateTypes.register(AnimationRow.class);

    public AnimationRow(MemorySegment address) {
        super(address);
    }

    public static Type getType() {
        return gtype;
    }

    @Getter
    private String animationID = "";

    @GtkChild(name = "animation_icon")
    public Image animationIcon;

    public final void setIconName(String iconName) {
        animationIcon.setFromIconName(iconName);
    }

    @GtkChild(name = "animation_label")
    public Label animationRowLabel;

    @Setter
    private AnimationMenu MenuReference;

    public final void setAnimationLabel(String animationLabel) {
        this.animationRowLabel.setLabel(animationLabel);
    }

    public static @NotNull AnimationRow create(@NotNull AnimationRowData animationRowData) {
        CooldownManger.addAction(animationRowData.animationID(), animationRowData.action(), animationRowData.cooldown() != null ? animationRowData.cooldown() : 0, null);
        createAction(animationRowData.app(), animationRowData.animationID(), animationRowData.label());

        AnimationRow row = GObject.newInstance(getType(), "action-name", "app." + animationRowData.animationID());
        row.animationID = animationRowData.animationID();
        row.setIconName(animationRowData.iconName());
        row.setAnimationLabel(animationRowData.label());
        row.animationRowLabel.setWrap(true);
        row.animationRowLabel.setWidthChars(10);

        row.setTooltipText(animationRowData.animationID());
        return row;
    }

    public void update(String label, String iconName) {
        animationRowLabel.setLabel(label);
        animationIcon.setFromIconName(iconName);
        if (MenuReference != null) {
            MenuReference.animationLabel.setLabel(label);
            MenuReference.animationMenuImage.setFromIconName(iconName);
        }
    }

    private static void createAction(@NotNull Application app, String animationID, String animationLabel) {
        LEDSuiteWindow window = (LEDSuiteWindow) app.getActiveWindow();
        var simpleAction = new SimpleAction(String.valueOf(animationID), null);
        simpleAction.onActivate(_ -> {
            if (!CooldownManger.call(String.valueOf(animationID))) {
                LEDSuiteApplication.getLogger().info("The animation row " + animationLabel + " (" + animationID + ") is on cooldown!", new LEDSuiteLogAreas.USER_INTERACTIONS());
            } else {
                GLib.idleAddOnce(() -> {
                    window.fileManagementList.unselectAll();
                    window.setSelectedAnimation(animationID);
                });

                LEDSuiteApplication.getLogger().verbose("Requesting menu for animation '" + animationID + "'", new LEDSuiteLogAreas.USER_INTERACTIONS());
                LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                        MenuRequestPacket.builder()
                                .requestFile(animationID)
                                .build().serialize()
                );
            }
        });
        app.addAction(simpleAction);
    }
}
