package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.widgets.row_widgets;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.DeserializableWidget;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.WidgetType;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.widgets.templates.AnimationMenuActionRowWidget;
import com.toxicstoxm.LEDSuite.scheduler.SmartRunnable;
import org.gnome.adw.SpinRow;
import org.gnome.glib.Type;
import org.gnome.gtk.Adjustment;
import org.gnome.gtk.SpinButtonUpdatePolicy;
import org.jetbrains.annotations.NotNull;

/**
 * @since 1.0.0
 */
@AutoRegister(module = AutoRegisterModules.WIDGETS)
public class SpinRowWidget extends AnimationMenuActionRowWidget<SpinRow> {
    @Override
    public String getType() {
        return WidgetType.SPIN_ROW.getName();
    }

    @Override
    public Type getWidgetType() {
        return SpinRow.getType();
    }

    private long cooldown = 0;
    private long lastUpdate;
    private boolean onCooldown = false;

    @Override
    public SpinRow deserialize(@NotNull DeserializableWidget deserializableWidget) throws DeserializationException {
        super.deserialize(deserializableWidget);

        var adjustment = Adjustment.builder().build();

        adjustment.setValue(
                getDoubleIfAvailable(Constants.Communication.YAML.Keys.Reply.MenuReply.VALUE, 0)
        );

        adjustment.setLower(
                getDoubleIfAvailable(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.MINIMUM, 0)
        );

        adjustment.setUpper(
                getDoubleIfAvailable(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.MAXIMUM, 100)
        );

        adjustment.setStepIncrement(
                getDoubleIfAvailable(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.INCREMENT, 1)
        );

        adjustment.setPageIncrement(
                getDoubleIfAvailable(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.PAGE_INCREMENT, 10)
        );

        widget.setAdjustment(adjustment);

        // If the value is not set here too, it will be off by 10^digits
        widget.setValue(
                getDoubleIfAvailable(Constants.Communication.YAML.Keys.Reply.MenuReply.VALUE, 0)
        );

        widget.setWrap(
                getBooleanIfAvailable(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.WRAP, false)
        );

        widget.setSnapToTicks(
                getBooleanIfAvailable(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.SNAP, true)
        );

        widget.setClimbRate(
                getDoubleIfAvailable(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.CLIMB_RATE, 2)
        );

        widget.setDigits(
                getIntIfAvailable(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.DIGITS, 0)
        );

        widget.setUpdatePolicy(SpinButtonUpdatePolicy.IF_VALID);

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.UPDATE_COOLDOWN)) {
            cooldown = widgetSection.getLong(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.UPDATE_COOLDOWN);
            lastUpdate = System.currentTimeMillis() - cooldown - 1;
            widget.onChanged(() -> {
                long timeSinceLastUpdate = System.currentTimeMillis() - lastUpdate;

                if (!onCooldown) {
                    if (timeSinceLastUpdate > cooldown) {
                        lastUpdate = System.currentTimeMillis();
                        sendMenuChangeRequest(widget.getValue());
                    } else {
                        onCooldown = true;
                        double lastKnownValue = widget.getValue();
                        new SmartRunnable() {
                            @Override
                            public void run() {
                                lastUpdate = System.currentTimeMillis();
                                if (widget != null) {
                                    sendMenuChangeRequest(widget.getValue());
                                } else {
                                    sendMenuChangeRequest(lastKnownValue);
                                }
                                onCooldown = false;
                            }
                        }.runTaskLaterAsync(cooldown);
                    }
                }
            });
        } else {
            widget.onChanged(() -> sendMenuChangeRequest(widget.getValue()));
        }

        return widget;
    }
}
