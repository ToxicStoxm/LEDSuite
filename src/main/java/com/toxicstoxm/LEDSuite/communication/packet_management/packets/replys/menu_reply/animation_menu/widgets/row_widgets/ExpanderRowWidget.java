package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.widgets.row_widgets;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.ErrorCode;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.AnimationMenuManager;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.DeserializableWidget;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.WidgetType;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu.widgets.templates.AnimationMenuRowWidget;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.YAJSI.api.yaml.ConfigurationSection;
import org.gnome.adw.ExpanderRow;
import org.gnome.glib.Type;
import org.jetbrains.annotations.NotNull;

/**
 * @since 1.0.0
 */
@AutoRegister(module = AutoRegisterModules.WIDGETS)
public class ExpanderRowWidget extends AnimationMenuRowWidget<ExpanderRow> {
    @Override
    public String getType() {
        return WidgetType.EXPANDER_ROW.getName();
    }

    @Override
    public Type getWidgetType() {
        return ExpanderRow.getType();
    }

    @Override
    public ExpanderRow deserialize(@NotNull DeserializableWidget deserializableWidget) throws DeserializationException {
        super.deserialize(deserializableWidget);

        widget.setSubtitle(
                getStringIfAvailable(Constants.Communication.YAML.Keys.Reply.MenuReply.SUBTITLE)
        );

        widget.setShowEnableSwitch(
                getBooleanIfAvailable(Constants.Communication.YAML.Keys.Reply.MenuReply.ExpanderRow.WITH_SWITCH, true)
        );

        widget.setEnableExpansion(
                getBooleanIfAvailable(Constants.Communication.YAML.Keys.Reply.MenuReply.VALUE, true)
        );

        widget.setExpanded(
                getBooleanIfAvailable(Constants.Communication.YAML.Keys.Reply.MenuReply.VALUE, false)
        );

        ensureKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT);
        ConfigurationSection contentSection = widgetSection.getConfigurationSection(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT);
        if (contentSection == null) throw new DeserializationException("Expander row without content is disallowed!", ErrorCode.ExpanderRowWithoutContent);

        for (String widgetKey : contentSection.getKeys(false)) {
            ConfigurationSection widgetSection = contentSection.getConfigurationSection(widgetKey);
            if (widgetSection == null)
                throw new DeserializationException("Failed to deserialize widget. Section missing for '" + widgetKey + "'!", ErrorCode.WidgetSectionEmptyOrMissing);

            ensureKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE);
            String type = widgetSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE);

            AnimationMenuManager animationMenuManager = getAnimationMenuManager(widgetKey, type);
            try {
                widget.addRow(
                        animationMenuManager.get(type).deserialize(
                                DeserializableWidget.builder()
                                        .widgetSection(widgetSection)
                                        .widgetKey(widgetKey)
                                        .animationName(animationName)
                                        .build()
                        )
                );
            } catch (DeserializationException e) {
                throw new DeserializationException("Failed to deserialize expander content!", e, e.getErrorCode());
            }
        }
        if (widget.getShowEnableSwitch()) {
            onChanged(() -> {
                new LEDSuiteRunnable() {
                    @Override
                    public void run() {
                        sendMenuChangeRequest(widget.getEnableExpansion());
                    }
                }.runTaskLaterAsynchronously(100);
            });
        }

        return widget;
    }

    private static @NotNull AnimationMenuManager getAnimationMenuManager(String widgetKey, String type) throws DeserializationException {
        AnimationMenuManager animationMenuManager = LEDSuiteApplication.getAnimationMenuManager();
        if (animationMenuManager == null)
            throw new DeserializationException("Failed to deserialize content widgets because animation menu manager instance wasn't found!", ErrorCode.GenericClientError);

        if (type == null || !animationMenuManager.isRegistered(type))
            throw new DeserializationException("Widget '" + widgetKey + "' has invalid / unknown type '" + type + "'!", ErrorCode.WidgetInvalidOrUnknownType);

        if (WidgetType.valueOf(type.toUpperCase()).equals(WidgetType.EXPANDER_ROW))
            throw new DeserializationException("Adding an expander row to an expander row is disallowed!", ErrorCode.ExpanderInExpander);
        return animationMenuManager;
    }
}
