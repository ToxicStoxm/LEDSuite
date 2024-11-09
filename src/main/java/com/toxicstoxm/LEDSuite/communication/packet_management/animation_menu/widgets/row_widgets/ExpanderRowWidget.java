package com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.widgets.row_widgets;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.AnimationMenuManager;
import com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.DeserializableWidget;
import com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.WidgetType;
import com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.widgets.templates.AnimationMenuRowWidget;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.YAJSI.api.yaml.ConfigurationSection;
import org.gnome.adw.ExpanderRow;
import org.gnome.glib.Type;
import org.jetbrains.annotations.NotNull;

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
                getBooleanIfAvailable(Constants.Communication.YAML.Keys.Reply.MenuReply.ExpanderRow.ENABLE_EXPANSION, true)
        );

        widget.setExpanded(
                getBooleanIfAvailable(Constants.Communication.YAML.Keys.Reply.MenuReply.ExpanderRow.EXPANDED, false)
        );

        ensureKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT);
        ConfigurationSection contentSection = widgetSection.getConfigurationSection(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT);
        if (contentSection == null) throw new DeserializationException("Expander row without content is disallowed!");

        for (String widgetKey : contentSection.getKeys(false)) {
            ConfigurationSection widgetSection = contentSection.getConfigurationSection(widgetKey);
            if (widgetSection == null)
                throw new DeserializationException("Failed to deserialize widget. Section missing for '" + widgetKey + "'!");

            ensureKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE);
            String type = widgetSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE);

            AnimationMenuManager animationMenuManager = LEDSuiteApplication.getAnimationMenuManager();
            if (animationMenuManager == null)
                throw new DeserializationException("Failed to deserialize content widgets because animation menu manager instance wasn't found!");

            if (type == null || !animationMenuManager.isRegistered(type))
                throw new DeserializationException("Widget '" + widgetKey + "' has invalid / unknown type '" + type + "'!");
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
                throw new DeserializationException("Failed to deserialize expander content!", e);
            }
        }
        if (widget.getShowEnableSwitch()) {
            onChanged(() -> sendMenuChangeRequest(String.valueOf(widget.getEnableExpansion())));
        }

        return widget;
    }
}
