package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.Registrable;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModule;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.errors.ErrorCode;
import com.toxicstoxm.LEDSuite.tools.YamlTools;
import com.toxicstoxm.LEDSuite.ui.animation_menu.AnimationMenu;
import com.toxicstoxm.StormYAML.file.YamlConfiguration;
import com.toxicstoxm.StormYAML.yaml.ConfigurationSection;
import com.toxicstoxm.StormYAML.yaml.InvalidConfigurationException;
import com.toxicstoxm.YAJL.core.Logger;
import com.toxicstoxm.YAJL.core.LoggerManager;
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
    private static final Logger logger = LoggerManager.getLogger(AnimationMenuManager.class);

    private final String widgetClassPath;

    public AnimationMenuManager(String widgetClassPath) {
        this.widgetClassPath = widgetClassPath;
    }

    @Override
    protected AutoRegisterModule<Widget> autoRegisterModule() {
        logger.verbose("Auto-registering widgets within module -> '{}'", widgetClassPath);
        return AutoRegisterModule.<Widget>builder()
                .moduleType(Widget.class)
                .module(AutoRegisterModules.WIDGETS)
                .classPath(widgetClassPath)
                .build();
    }

    /**
     * Attempts to deserialize the given menu YAML string and construct a new animation menu object from it.
     *
     * @param menuYAML the YAML string to deserialize
     * @return the new animation menu
     * @throws DeserializationException if something goes wrong while deserializing. E.g.: missing keys, invalid YAML or invalid values
     * @see #deserializeAnimationMenuGroup(String, String, ConfigurationSection)
     */
    public AnimationMenu deserializeAnimationMenu(String menuYAML) throws DeserializationException {
        logger.debug("Starting deserialization of animation menu");

        // Try to load YAML string into a YAML object
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.loadFromString(menuYAML);
            logger.debug("YAML successfully loaded from string");
        } catch (InvalidConfigurationException e) {
            logger.error("Failed to load YAML from string", e);
            throw new DeserializationException("Failed to deserialize YAML from string!", ErrorCode.FailedToParseYAML);
        }

        // Retrieve the menu id corresponding to an animation row and creating a new animation menu object with this id
        ensureKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.FILENAME, yaml);
        String menuId = yaml.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.FILENAME);
        logger.debug("Creating AnimationMenu with ID '{}'", menuId);
        AnimationMenu animationMenu = new AnimationMenu(menuId);

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.SUBTITLE, yaml)) {
            String menuSubtitle = yaml.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.SUBTITLE);
            if (menuSubtitle != null && !menuSubtitle.isBlank()) {
                logger.debug("Setting subtitle: '{}'", menuSubtitle);
                animationMenu.animationSubtitle.setLabel(menuSubtitle);
            }
        }

        // If no menu content section is found, return the empty menu
        if (!checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT, yaml)) {
            logger.warn("No content section found in animation menu '{}', returning empty menu", menuId);
            return animationMenu;
        }

        // Load the menu content section and ensure it's not null
        ConfigurationSection menuContentSection = yaml.getConfigurationSection(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT);
        if (menuContentSection == null)
            throw new DeserializationException("Menu content section is empty!", ErrorCode.MenuContentKeyMissing);

        TreeMap<Integer, PreferencesGroup> groups = new TreeMap<>();
        List<PreferencesGroup> unsortedGroups = new ArrayList<>();

        // Loop through the content widgets and ensure they are groups, because all top level widgets need to be groups
        // If a type key exists check if it is a group
        for (String menuGroupKey : menuContentSection.getKeys(false)) {
            logger.debug("Processing group '{}'", menuGroupKey);

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
                logger.debug("Group '{}' has no index, adding to unsorted groups", menuGroupKey);
                unsortedGroups.add(group);
            } else if (groups.containsKey(index)) {
                logger.warn("Duplicate index '{}' found for group '{}', adding to unsorted groups", index, menuGroupKey);
                unsortedGroups.add(group);
            } else {
                groups.put(index, group);
                logger.debug("Group '{}' added at index '{}'", menuGroupKey, index);
            }
        }

        // Append all unsorted groups to the end of the sorted groups map
        AtomicInteger highestIndex = new AtomicInteger(groups.lastKey());
        unsortedGroups.forEach(entry -> {
            int newIndex = highestIndex.incrementAndGet();
            groups.put(newIndex, entry);
            logger.debug("Unsorted group added at index '{}'", newIndex);
        });

        // Loop through the sorted groups tree map and add them to the menu in the correct order
        groups.forEach((index, group) -> {
            try {
                animationMenu.animationMenuContent.append(group);
                logger.debug("Appended group '{}' at index '{}'", group.getName(), index);
            } catch (DeserializationException e) {
                logger.error("Failed to append group '{}' at index '{}'", group.getName(), index);
                throw new DeserializationException("Failed to deserialize animation menu group '" + group.getName() + "' at index '" + index + "!", e, e.getErrorCode());
            }
        });

        logger.debug("Animation menu '{}' deserialization completed", animationMenu.getMenuID());
        return animationMenu;
    }


    /**
     * Attempts to deserialize the specified menu group YAML section and constructs a new {@link PreferencesGroup}.
     *
     * @param menuGroupKey     the animation menu group id (used by the server for identifying individual menu widgets)
     * @param menuGroupSection the YAML config section to deserialize
     * @return the new {@link PreferencesGroup} object
     * @throws DeserializationException if something goes wrong while deserializing. E.g.: missing keys, invalid YAML or invalid values
     * @see #deserializeAnimationMenu(String)
     */
    private PreferencesGroup deserializeAnimationMenuGroup(@NotNull String animationName, @NotNull String menuGroupKey, @NotNull ConfigurationSection menuGroupSection) throws DeserializationException {
        logger.debug("Deserializing animation menu group '{}' for animation '{}'", menuGroupKey, animationName);

        String topLevelWidgetType = menuGroupSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE);
        if (topLevelWidgetType == null) {
            logger.error("Top level widget type is null for group '{}'", menuGroupKey);
            throw new DeserializationException("Invalid widget type 'null' for top level group " + menuGroupKey + "'!", ErrorCode.WidgetMissingType);
        }

        if (!topLevelWidgetType.equals(WidgetType.GROUP.getName())) {
            logger.warn("Invalid top level widget type '{}' for '{}' – must be 'group'", topLevelWidgetType, menuGroupKey);
            throw new DeserializationException("Invalid top level widget type '" + topLevelWidgetType + "' for '" + menuGroupKey + "' isn't a group! Top level widgets must be groups!", ErrorCode.TopLevelWidgetIsNotGroup);
        }

        PreferencesGroup menuGroup = PreferencesGroup.builder().build();

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.LABEL, menuGroupSection)) {
            String label = menuGroupSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.LABEL);
            logger.debug("Setting label for group '{}': '{}'", menuGroupKey, label);
            menuGroup.setTitle(label);
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.TOOLTIP, menuGroupSection)) {
            String tooltip = menuGroupSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.TOOLTIP);
            logger.debug("Setting tooltip for group '{}': '{}'", menuGroupKey, tooltip);
            menuGroup.setTooltipText(tooltip);
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.Groups.SUFFIX, menuGroupSection)) {
            ConfigurationSection groupHeaderSuffixSection = menuGroupSection.getConfigurationSection(Constants.Communication.YAML.Keys.Reply.MenuReply.Groups.SUFFIX);
            if (groupHeaderSuffixSection == null) {
                logger.error("Header suffix section is null for group '{}'", menuGroupKey);
                throw new DeserializationException("Group '" + menuGroupKey + "' header suffix section is invalid or missing!", ErrorCode.GroupHeaderSuffixWidgetSectionInvalid);
            }

            String type = WidgetType.BUTTON.getName();
            if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE, groupHeaderSuffixSection)) {
                type = groupHeaderSuffixSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE);

                if (type != null && !type.equalsIgnoreCase(WidgetType.BUTTON.getName())) {
                    logger.warn("Header suffix widget type '{}' for group '{}' is invalid (must be button)", type, menuGroupKey);
                    throw new DeserializationException("Group '" + menuGroupKey + "' header suffix widget has illegal type '" + type + "'. Must be button!", ErrorCode.GroupHeaderSuffixWidgetInvalidType);
                }
            }

            logger.debug("Deserializing header suffix for group '{}' as type '{}'", menuGroupKey, type);
            menuGroup.setHeaderSuffix(
                    get(type).deserialize(
                            DeserializableWidget.builder()
                                    .animationName(animationName)
                                    .widgetSection(groupHeaderSuffixSection)
                                    .widgetKey(menuGroupKey)
                                    .build()
                    ));
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT, menuGroupSection)) {
            ConfigurationSection menuGroupContentSection = menuGroupSection.getConfigurationSection(Constants.Communication.YAML.Keys.Reply.MenuReply.CONTENT);
            if (menuGroupContentSection == null) {
                logger.error("Content section for group '{}' is null", menuGroupKey);
                throw new DeserializationException("Group content section is empty!", ErrorCode.GroupContentSectionEmptyOrMissing);
            }

            TreeMap<Integer, DeserializableWidget> widgets = new TreeMap<>();
            List<DeserializableWidget> unsortedWidgets = new ArrayList<>();

            for (String widgetKey : menuGroupContentSection.getKeys(false)) {
                ConfigurationSection widgetSection = menuGroupContentSection.getConfigurationSection(widgetKey);
                if (widgetSection == null) {
                    logger.error("Widget section '{}' in group '{}' is null", widgetKey, menuGroupKey);
                    throw new DeserializationException("Widget section for widget '" + widgetKey + "' was null!", ErrorCode.WidgetSectionEmptyOrMissing);
                }

                if (!checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE, widgetSection)) {
                    logger.error("Widget type missing for widget '{}' in group '{}'", widgetKey, menuGroupKey);
                    throw new DeserializationException("Widget type for widget '" + widgetKey + "' missing!", ErrorCode.WidgetMissingType);
                }

                String widgetType = widgetSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.TYPE);
                if (!isRegistered(widgetType)) {
                    logger.warn("Unknown widget type '{}' for widget '{}' in group '{}'", widgetType, widgetKey, menuGroupKey);
                    throw new DeserializationException("Invalid / Unknown widget type '" + widgetType + "' for widget '" + widgetKey + "'!", ErrorCode.WidgetInvalidOrUnknownType);
                }

                int index = YamlTools.getIntIfAvailable(Constants.Communication.YAML.Keys.Reply.MenuReply.INDEX, -1, widgetSection);
                logger.debug("Processing widget '{}' of type '{}' at index '{}'", widgetKey, widgetType, index);

                DeserializableWidget deserializableWidget = DeserializableWidget.builder()
                        .animationName(animationName)
                        .widgetSection(widgetSection)
                        .widgetKey(widgetKey)
                        .widgetType(widgetType)
                        .build();

                if (index < 0) {
                    unsortedWidgets.add(deserializableWidget);
                    logger.debug("Widget '{}' has no index, added to unsorted list", widgetKey);
                } else if (widgets.containsKey(index)) {
                    unsortedWidgets.add(deserializableWidget);
                    logger.warn("Duplicate widget index '{}' for widget '{}', added to unsorted list", index, widgetKey);
                } else {
                    widgets.put(index, deserializableWidget);
                    logger.debug("Widget '{}' placed at index '{}'", widgetKey, index);
                }
            }

            AtomicInteger highestIndex = new AtomicInteger(widgets.isEmpty() ? -1 : widgets.lastKey());
            unsortedWidgets.forEach(entry -> {
                int newIndex = highestIndex.incrementAndGet();
                widgets.put(newIndex, entry);
                logger.debug("Unsorted widget '{}' added at index '{}'", entry.widgetKey(), newIndex);
            });

            widgets.forEach((index, deserializableWidget) -> {
                try {
                    menuGroup.add(
                            get(deserializableWidget.widgetType()).cpy().deserialize(deserializableWidget)
                    );
                    logger.debug("Appended widget '{}' to group '{}' at index '{}'", deserializableWidget.widgetKey(), menuGroupKey, index);
                } catch (DeserializationException e) {
                    logger.error("Failed to deserialize widget '{}' in group '{}'", deserializableWidget.widgetKey(), menuGroupKey);
                    throw new DeserializationException("Failed to deserialize widget '" + deserializableWidget.widgetKey() + "' with type '" + deserializableWidget.widgetType() + "' at index '" + index + "'!", e, e.getErrorCode());
                }
            });
        }

        logger.debug("Finished deserializing group '{}'", menuGroupKey);
        return menuGroup;
    }
}
