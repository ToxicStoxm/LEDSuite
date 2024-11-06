package com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.widgets.row_widgets;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.DeserializableWidget;
import com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.WidgetType;
import com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.widgets.templates.AnimationMenuActionRowWidget;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import org.gnome.adw.SpinRow;
import org.gnome.glib.Type;
import org.gnome.gtk.Adjustment;
import org.gnome.gtk.SpinButtonUpdatePolicy;
import org.jetbrains.annotations.NotNull;

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

        var adjustment = Adjustment.builder()
                .setStepIncrement(1)
                .setLower(0)
                .setUpper(100)
                .setPageIncrement(10)
                .setValue(0)
                .build();

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.VALUE, widgetSection)) {
            adjustment.setValue(widgetSection.getDouble(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.VALUE));
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.MINIMUM, widgetSection)) {
            adjustment.setLower(widgetSection.getDouble(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.MINIMUM));
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.MAXIMUM, widgetSection)) {
            adjustment.setUpper(widgetSection.getDouble(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.MAXIMUM));
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.INCREMENT, widgetSection)) {
            adjustment.setStepIncrement(widgetSection.getDouble(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.INCREMENT));
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.PAGE_INCREMENT, widgetSection)) {
            adjustment.setPageIncrement(widgetSection.getDouble(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.PAGE_INCREMENT));
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.WRAP, widgetSection)) {
            widget.setWrap(widgetSection.getBoolean(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.WRAP));
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.SNAP, widgetSection)) {
            widget.setSnapToTicks(widgetSection.getBoolean(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.SNAP));
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.CLIMB_RATE, widgetSection)) {
            widget.setClimbRate(widgetSection.getDouble(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.CLIMB_RATE));
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.DIGITS, widgetSection)) {
            widget.setDigits(widgetSection.getInt(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.DIGITS));
        }

        widget.setUpdatePolicy(SpinButtonUpdatePolicy.IF_VALID);

        widget.setAdjustment(adjustment);

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.UPDATE_COOLDOWN, widgetSection)) {
            cooldown = widgetSection.getLong(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.UPDATE_COOLDOWN);
            lastUpdate = System.currentTimeMillis() - cooldown - 1;
            widget.onChanged(() -> {
                long timeSinceLastUpdate = System.currentTimeMillis() - lastUpdate;

                if (!onCooldown) {
                    if (timeSinceLastUpdate > cooldown) {
                        lastUpdate = System.currentTimeMillis();
                        sendMenuChangeRequest(String.valueOf(widget.getValue()));
                    } else {
                        onCooldown = true;
                        double lastKnownValue = widget.getValue();
                        new LEDSuiteRunnable() {
                            @Override
                            public void run() {
                                lastUpdate = System.currentTimeMillis();
                                if (widget != null) {
                                    sendMenuChangeRequest(String.valueOf(widget.getValue()));
                                } else {
                                    sendMenuChangeRequest(String.valueOf(lastKnownValue));
                                }
                                onCooldown = false;
                            }
                        }.runTaskLaterAsynchronously(cooldown);
                    }
                }
            });
        } else {
            widget.onChanged(() -> sendMenuChangeRequest(String.valueOf(widget.getValue())));
        }

        return widget;
    }
}
