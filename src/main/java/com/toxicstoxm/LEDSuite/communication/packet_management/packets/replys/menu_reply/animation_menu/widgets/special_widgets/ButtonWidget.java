package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.widgets.special_widgets;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.DeserializableWidget;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.WidgetType;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.widgets.templates.AnimationMenuWidget;
import org.gnome.adw.Clamp;
import org.gnome.adw.LengthUnit;
import org.gnome.adw.Spinner;
import org.gnome.glib.Type;
import org.gnome.gtk.*;
import org.jetbrains.annotations.NotNull;

/**
 * @since 1.0.0
 */
@AutoRegister(module = AutoRegisterModules.WIDGETS)
public class ButtonWidget extends AnimationMenuWidget<Clamp> {
    @Override
    public String getType() {
        return WidgetType.BUTTON.getName();
    }

    @Override
    public Type getWidgetType() {
        return Clamp.getType();
    }

    @Override
    public Clamp deserialize(@NotNull DeserializableWidget deserializableWidget) throws DeserializationException {
        super.deserialize(deserializableWidget);

        widget.setMarginTop(25);
        widget.setMaximumSize(120);
        widget.setTighteningThreshold(120);
        widget.setUnit(LengthUnit.PX);

        Button buttonWidget = Button.builder()
                .setCssClasses(new String[]{"suggested-action"})
                .build();

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.TOOLTIP)) {
            buttonWidget.setTooltipText(widgetSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.TOOLTIP));
        }

        Box buttonBox = Box.builder()
                .setSpacing(5)
                .build();

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.LABEL)) {
            buttonBox.append(
                    Label.builder()
                            .setLabel(widgetSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.LABEL))
                            .build()
            );
        }

        Revealer spinnerRevealer = Revealer.builder()
                .setChild(Spinner.builder().build())
                .setTransitionType(RevealerTransitionType.SLIDE_LEFT)
                .build();

        buttonBox.append(spinnerRevealer);

        spinnerRevealer.setRevealChild(
                getBooleanIfAvailable(Constants.Communication.YAML.Keys.Reply.MenuReply.Button.SPINNING, false)
        );

        buttonWidget.setSensitive(
                !getBooleanIfAvailable(Constants.Communication.YAML.Keys.Reply.MenuReply.Button.BLOCKING, false)
        );

        Clamp buttonClamp = Clamp.builder()
                .setMaximumSize(70)
                .setTighteningThreshold(70)
                .setChild(buttonBox)
                .build();

        buttonWidget.setChild(buttonClamp);

        boolean spinOnClicked = getBooleanIfAvailable(Constants.Communication.YAML.Keys.Reply.MenuReply.Button.SPIN_ON_CLICKED, false);
        boolean blockOnClicked = getBooleanIfAvailable(Constants.Communication.YAML.Keys.Reply.MenuReply.Button.BLOCK_AFTER_CLICKED, false);

        if (spinOnClicked) {
            buttonWidget.onClicked(() -> {
                spinnerRevealer.setRevealChild(true);
                sendMenuChangeRequestWithoutValue();
            });
        }

        if (blockOnClicked) {
            buttonWidget.onClicked(() -> {
               buttonWidget.setSensitive(false);
               sendMenuChangeRequestWithoutValue();
            });
        }

        if (!spinOnClicked && !blockOnClicked) {
            buttonWidget.onClicked(this::sendMenuChangeRequestWithoutValue);
        }

        widget.setChild(buttonWidget);

        return widget;
    }
}
