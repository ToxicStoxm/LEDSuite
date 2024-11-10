package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.widgets.row_widgets;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.DeserializableWidget;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.WidgetType;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.widgets.templates.AnimationMenuRowWidget;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import org.gnome.adw.EntryRow;
import org.gnome.glib.Type;
import org.gnome.pango.AttrList;
import org.jetbrains.annotations.NotNull;

@AutoRegister(module = AutoRegisterModules.WIDGETS)
public class EntryRowWidget extends AnimationMenuRowWidget<EntryRow> {
    @Override
    public String getType() {
        return WidgetType.ENTRY_ROW.getName();
    }

    @Override
    public Type getWidgetType() {
        return EntryRow.getType();
    }

    private long cooldown = 0;
    private long lastUpdate;
    private boolean onCooldown = false;

    @Override
    public EntryRow deserialize(@NotNull DeserializableWidget deserializableWidget) throws DeserializationException {
        super.deserialize(deserializableWidget);

        widget.setText(
                getStringIfAvailable(Constants.Communication.YAML.Keys.Reply.MenuReply.SUBTITLE)
        );

        widget.setShowApplyButton(
                getBooleanIfAvailable(Constants.Communication.YAML.Keys.Reply.MenuReply.EntryRow.APPLY_BUTTON, true)
        );

        widget.setEditable(
                getBooleanIfAvailable(Constants.Communication.YAML.Keys.Reply.MenuReply.EntryRow.EDITABLE, true)
        );

        widget.setAttributes(
                AttrList.fromString(Constants.Communication.YAML.Keys.Reply.MenuReply.EntryRow.ATTRIBUTE_STRING)
        );

        if (widget.getShowApplyButton()) {
            widget.onApply(() -> sendMenuChangeRequest(widget.getText()));
        } else {
            if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.EntryRow.UPDATE_COOLDOWN)) {
                cooldown = widgetSection.getLong(Constants.Communication.YAML.Keys.Reply.MenuReply.SpinRow.UPDATE_COOLDOWN);
                lastUpdate = System.currentTimeMillis() - cooldown - 1;
                widget.onChanged(() -> {
                    long timeSinceLastUpdate = System.currentTimeMillis() - lastUpdate;

                    if (!onCooldown) {
                        if (timeSinceLastUpdate > cooldown) {
                            lastUpdate = System.currentTimeMillis();
                            sendMenuChangeRequest(String.valueOf(widget.getText()));
                        } else {
                            onCooldown = true;
                            String lastKnownValue = widget.getText();
                            new LEDSuiteRunnable() {
                                @Override
                                public void run() {
                                    lastUpdate = System.currentTimeMillis();
                                    if (widget != null) {
                                        sendMenuChangeRequest(String.valueOf(widget.getText()));
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
                widget.onChanged(() -> sendMenuChangeRequest(String.valueOf(widget.getText())));
            }
        }
        return widget;
    }
}
