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
import com.toxicstoxm.LEDSuite.tools.YamlTools;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.StormYAML.yaml.ConfigurationSection;
import org.gnome.adw.ExpanderRow;
import org.gnome.glib.Type;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

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

        TreeMap<Integer, DeserializableWidget> widgets = new TreeMap<>();
        List<DeserializableWidget> unsortedWidgets = new ArrayList<>();

        // Loop through all widgets and check if their type is supported.
        // Sort the widgets by index and store them in temporary objects.
        for (String widgetKey : contentSection.getKeys(false)) {
            ConfigurationSection widgetSection = contentSection.getConfigurationSection(widgetKey);
            if (widgetSection == null)
                throw new DeserializationException("Failed to deserialize widget. Section missing for '" + widgetKey + "'!", ErrorCode.WidgetSectionEmptyOrMissing);

            ensureKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE);
            String type = widgetSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE);

            int index = YamlTools.getIntIfAvailable(Constants.Communication.YAML.Keys.Reply.MenuReply.INDEX, -1, widgetSection);

            // Store the widget temporarily for sorting
            DeserializableWidget expanderRowChild =
                    DeserializableWidget.builder()
                            .widgetType(type)
                            .widgetSection(widgetSection)
                            .widgetKey(widgetKey)
                            .animationName(animationName)
                            .build();

            // Add the widget to the corresponding position in the widgets tree map
            // If no index is present store it in the unsorted widgets list
            if (index < 0) {
                unsortedWidgets.add(expanderRowChild);
            } else {
                if (widgets.containsKey(index)) {
                    unsortedWidgets.add(expanderRowChild);
                } else {
                    widgets.put(index, expanderRowChild);
                }
            }
        }

        // Append all unsorted widgets to the end of the sorted widgets map
        AtomicInteger highestIndex = new AtomicInteger(widgets.lastKey());
        unsortedWidgets.forEach(entry -> {
            widgets.put(highestIndex.incrementAndGet(), entry);
        });

        // Loop through the sorted widgets and append them to the parent group in the correct order.
        widgets.forEach((index, expanderRowChild) -> {
                try {
                    AnimationMenuManager animationMenuManager = getAnimationMenuManager(expanderRowChild.widgetKey(), expanderRowChild.widgetType());
                    widget.addRow(
                            animationMenuManager.get(expanderRowChild.widgetType()).deserialize(
                                    expanderRowChild
                            )
                    );
                } catch (DeserializationException e) {
                    throw new DeserializationException("Failed to deserialize expander child widget '" + deserializableWidget.widgetKey() + "' with type '" + deserializableWidget.widgetType() + "' at index '" + index + "'!", e, e.getErrorCode());
                }
        });

        if (widget.getShowEnableSwitch()) {
            onChanged(() -> sendMenuChangeRequest(widget.getEnableExpansion()));
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
