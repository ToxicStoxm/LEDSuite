package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.Registrable;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModule;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.ErrorCode;
import com.toxicstoxm.LEDSuite.tools.ExceptionTools;
import com.toxicstoxm.LEDSuite.tools.YamlTools;
import com.toxicstoxm.LEDSuite.ui.animation_menu.AnimationMenu;
import com.toxicstoxm.YAJL.Logger;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.ConfigurationSection;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import org.gnome.adw.PreferencesGroup;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.toxicstoxm.LEDSuite.tools.YamlTools.checkIfKeyExists;
import static com.toxicstoxm.LEDSuite.tools.YamlTools.ensureKeyExists;

/**
 * Wrapper class for deserializing an animation menu by using registered {@link Widget}-deserializer classes.
 * @since 1.0.0
 * @see #deserializeAnimationMenu(String)
 */
public class AnimationMenuManager extends Registrable<Widget> {

    private static final Logger logger = Logger.autoConfigureLogger();

    private final String widgetClassPath;

    public AnimationMenuManager(String widgetClassPath) {
        this.widgetClassPath = widgetClassPath;
    }

    @Override
    protected AutoRegisterModule<Widget> autoRegisterModule() {
        return AutoRegisterModule.<Widget>builder()
                .moduleType(Widget.class)
                .module(AutoRegisterModules.WIDGETS)
                .classPath(widgetClassPath)
                .build();
    }

    /**
     * Attempts to deserialize the given menu YAML string and construct a new animation menu object from it.
     * @param menuYAML the YAML string to deserialize
     * @return the new animation menu
     * @throws DeserializationException if something goes wrong while deserializing. E.g.: missing keys, invalid YAML or invalid values
     * @see #deserializeAnimationMenuGroup(String, String, ConfigurationSection) 
     */
    public AnimationMenu deserializeAnimationMenu(String menuYAML) throws DeserializationException {

        // Try to load YAML string into a YAML object
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.loadFromString(menuYAML);
        } catch (InvalidConfigurationException e) {
            ExceptionTools.printStackTrace(e, logger::stacktrace);
            throw new DeserializationException("Failed to deserialize YAML from string!", ErrorCode.FailedToParseYAML);
        }

        // Retrieve the menu id corresponding to an animation row and creating a new animation menu object with this id
        ensureKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.FILENAME, yaml);
        AnimationMenu animationMenu = AnimationMenu.create(yaml.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.FILENAME));

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.SUBTITLE, yaml)) {
            String menuSubtitle = yaml.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.SUBTITLE);
            if (menuSubtitle != null && !menuSubtitle.isBlank()) {
                animationMenu.animationSubtitle.setLabel(menuSubtitle);
            }
        }

        // If no menu content section is found, return the empty menu
        if (!checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT, yaml)) {
            return animationMenu;
        }

        // Load the menu content section and ensure it's not null
        ConfigurationSection menuContentSection = yaml.getConfigurationSection(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT);
        if (menuContentSection == null) throw new DeserializationException("Menu content section is empty!", ErrorCode.MenuContentKeyMissing);

        TreeMap<Integer, PreferencesGroup> groups = new TreeMap<>();
        List<PreferencesGroup> unsortedGroups = new ArrayList<>();

        // Loop through the content widgets and ensure they are groups, because all top level widgets need to be groups
        // If a type key exists check if it is a group
        for (String menuGroupKey : menuContentSection.getKeys(false)) {

            ConfigurationSection menuGroupSection = menuContentSection.getConfigurationSection(menuGroupKey);
            if (menuGroupSection == null)
                throw new DeserializationException("Menu group '" + menuGroupKey + "' section is empty!", ErrorCode.GroupSectionEmptyOrMissing);

            // Retrieve the groups index
            // If non is present use -1
            int index = YamlTools.getIntIfAvailable(Constants.Communication.YAML.Keys.Reply.MenuReply.INDEX, -1, menuGroupSection);

            // Store the group in a temporary object for sorting
            PreferencesGroup group = deserializeAnimationMenuGroup(animationMenu.getMenuID(), menuGroupKey, menuGroupSection);

            // Add the group to the corresponding position in the groups tree map
            // If no index is present store it in the unsorted groups list
            if (index < 0) {
                unsortedGroups.add(group);
            } else {
                if (groups.containsKey(index)) {
                    unsortedGroups.add(group);
                } else {
                    groups.put(index, group);
                }
            }
        }

        // Append all unsorted groups to the end of the sorted groups map
        AtomicInteger highestIndex = new AtomicInteger(groups.lastKey());
        unsortedGroups.forEach(entry -> {
            groups.put(highestIndex.incrementAndGet(), entry);
        });

        // Loop through the sorted groups tree map and add them to the menu in the correct order
        groups.forEach((index, group) -> {
                try {
                    animationMenu.animationMenuContent.append(group);
                } catch (DeserializationException e) {
                    throw new DeserializationException("Failed to deserialize animation menu group '" + group.getName() + "' at index '" + index + "!", e, e.getErrorCode());
                }
        });

        return animationMenu;
    }


    /**
     * Attempts to deserialize the specified menu group YAML section and constructs a new {@link PreferencesGroup}.
     * @param menuGroupKey the animation menu group id (used by the server for identifying individual menu widgets)
     * @param menuGroupSection the YAML config section to deserialize
     * @return the new {@link PreferencesGroup} object
     * @throws DeserializationException if something goes wrong while deserializing. E.g.: missing keys, invalid YAML or invalid values
     * @see #deserializeAnimationMenu(String)
     */
    private PreferencesGroup deserializeAnimationMenuGroup(@NotNull String animationName, @NotNull String menuGroupKey, @NotNull ConfigurationSection menuGroupSection) throws DeserializationException {

        String topLevelWidgetType = menuGroupSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE);
        if (topLevelWidgetType == null)
            throw new DeserializationException("Invalid widget type 'null' for top level group " + menuGroupKey + "'!", ErrorCode.WidgetMissingType);
        if (!topLevelWidgetType.equals(WidgetType.GROUP.getName())) {
            logger.warn("Invalid top level widget type '{}' for '{}' isn't a group! Top level widgets must be groups!", topLevelWidgetType, menuGroupKey);
            throw new DeserializationException("Invalid top level widget type '" + topLevelWidgetType + "' for '" + menuGroupKey + "' isn't a group! Top level widgets must be groups!", ErrorCode.TopLevelWidgetIsNotGroup);
        }

        PreferencesGroup menuGroup = PreferencesGroup.builder().build();

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.LABEL, menuGroupSection)) {
            menuGroup.setTitle(menuGroupSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.LABEL));
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.TOOLTIP, menuGroupSection)) {
            menuGroup.setTooltipText(menuGroupSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.TOOLTIP));
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.Groups.SUFFIX, menuGroupSection)) {
            ConfigurationSection groupHeaderSuffixSection = menuGroupSection.getConfigurationSection(Constants.Communication.YAML.Keys.Reply.MenuReply.Groups.SUFFIX);
            if (groupHeaderSuffixSection == null)
                throw new DeserializationException("Group '" + menuGroupKey + "' header suffix section is invalid or missing!", ErrorCode.GroupHeaderSuffixWidgetSectionInvalid);

            String type = WidgetType.BUTTON.getName();
            if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE, groupHeaderSuffixSection)) {
                type = groupHeaderSuffixSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE);

                if (type != null) {
                    if (!type.toUpperCase().equals(WidgetType.BUTTON.getName()))
                        throw new DeserializationException("Group '" + menuGroupKey + "' header suffix widget has illegal type '" + type + "'. Must be button!", ErrorCode.GroupHeaderSuffixWidgetInvalidType);
                }
            }

            menuGroup.setHeaderSuffix(
                    get(type).deserialize(
                            DeserializableWidget.builder()
                                    .animationName(animationName)
                                    .widgetSection(groupHeaderSuffixSection)
                                    .widgetKey(menuGroupKey)
                                    .build()
                    ));
        }

        // TODO properties

        // Disabled to allow groups without content
        // if (!checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT, menuGroupSection))
        //     throw new DeserializationException("Group content section is missing!", ErrorCode.GroupContentKeyMissing);

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT, menuGroupSection)) {

            ConfigurationSection menuGroupContentSection = menuGroupSection.getConfigurationSection(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT);
            if (menuGroupContentSection == null)
                throw new DeserializationException("Group content section is empty!", ErrorCode.GroupContentSectionEmptyOrMissing);

            TreeMap<Integer, DeserializableWidget> widgets = new TreeMap<>();
            List<DeserializableWidget> unsortedWidgets = new ArrayList<>();

            // Loop through all widgets and check if their type is supported.
            // Sort the widgets by index and store them in temporary objects.
            for (String widgetKey : menuGroupContentSection.getKeys(false)) {
                ConfigurationSection widgetSection = menuGroupContentSection.getConfigurationSection(widgetKey);
                if (widgetSection == null)
                    throw new DeserializationException("Widget section for widget '" + widgetKey + "' was null!", ErrorCode.WidgetSectionEmptyOrMissing);

                if (!checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE, widgetSection))
                    throw new DeserializationException("Widget type for widget '" + widgetKey + "' missing!", ErrorCode.WidgetMissingType);

                String widgetType = widgetSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE);

                if (!isRegistered(widgetType))
                    throw new DeserializationException("Invalid / Unknown widget type '" + widgetType + "' for widget '" + widgetKey + "'!", ErrorCode.WidgetInvalidOrUnknownType);

                int index = YamlTools.getIntIfAvailable(Constants.Communication.YAML.Keys.Reply.MenuReply.INDEX, -1, widgetSection);

                // Store the widget temporarily for sorting
                DeserializableWidget deserializableWidget =
                        DeserializableWidget.builder()
                                .animationName(animationName)
                                .widgetSection(widgetSection)
                                .widgetKey(widgetKey)
                                .widgetType(widgetType)
                                .build();

                // Add the widget to the corresponding position in the widgets tree map
                // If no index is present store it in the unsorted widgets list
                if (index < 0) {
                    unsortedWidgets.add(deserializableWidget);
                } else {
                    if (widgets.containsKey(index)) {
                        unsortedWidgets.add(deserializableWidget);
                    } else {
                        widgets.put(index, deserializableWidget);
                    }
                }
            }

            // Append all unsorted widgets to the end of the sorted widgets map
            AtomicInteger highestIndex = new AtomicInteger(widgets.lastKey());
            unsortedWidgets.forEach(entry -> {
                widgets.put(highestIndex.incrementAndGet(), entry);
            });

            // Loop through the sorted widgets and append them to the parent group in the correct order.
            widgets.forEach((index, deserializableWidget) -> {
                try {
                    menuGroup.add(
                            get(deserializableWidget.widgetType()).cpy().deserialize(
                                    deserializableWidget
                            )
                    );
                } catch (DeserializationException e) {
                    throw new DeserializationException("Failed to deserialize widget '" + deserializableWidget.widgetKey() + "' with type '" + deserializableWidget.widgetType() + "' at index '" + index + "'!", e, e.getErrorCode());
                }
            });

        }
        return menuGroup;
    }
}
