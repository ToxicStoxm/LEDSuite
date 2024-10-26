package com.toxicstoxm.LEDSuite.ui.animation_menu;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.PacketManager;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.LEDSuite.ui.animation_menu.widgets.GroupWidget;
import com.toxicstoxm.LEDSuite.yaml.YamlTools;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.ConfigurationSection;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Wrapper class for constructing animation menus.
 * @since 1.0.0
 */
public class AnimationMenuConstructor {

    private final HashMap<String, com.toxicstoxm.LEDSuite.ui.animation_menu.Widget> registeredWidgets = new HashMap<>();

    /**
     * Registers the specified widget interface under the specified name. After a widget is registered it can be serialized and deserialized using the {@link #serialize(Widget)} and {@link #deserialize(Class, ConfigurationSection, String)} methods.<br>
     * If another widget is already registered under the specified packet name. This method will fail and return {@code false}.
     *
     * @param widget@return {@code true} if the packet was successfully registered, otherwise {@code false}
     * @see #unregisterWidget(String)
     * @see #clearWidgets()
     */
    public boolean registerWidget(com.toxicstoxm.LEDSuite.ui.animation_menu.Widget widget) {
        return registeredWidgets.putIfAbsent(widget.getType(), widget) == null;
    }

    /**
     * Unregisters the widget registered under the specified packet name.
     * @param widget_type the widget type (name) to unregister
     * @return {@code true} if the specified widget was previously registered and was successfully unregistered, otherwise {@code false}
     * @see #registerWidget(com.toxicstoxm.LEDSuite.ui.animation_menu.Widget)
     * @see #clearWidgets()
     */
    public boolean unregisterWidget(String widget_type) {
        return registeredWidgets.remove(widget_type) != null;
    }

    /**
     * Clears all registered widgets.
     * @see #registerWidget(com.toxicstoxm.LEDSuite.ui.animation_menu.Widget)
     * @see #unregisterWidget(String)
     */
    public void clearWidgets() {
        registeredWidgets.clear();
    }

    private final CallbackRelay defaultCallbackRelay;

    public AnimationMenuConstructor(CallbackRelay defaultCallbackRelay) {
        this.defaultCallbackRelay = defaultCallbackRelay;
    }

    public AnimationMenu constructMenu(String yamlString) {
        return constructMenu(yamlString, defaultCallbackRelay);
    }

    public AnimationMenu constructMenu(String yamlString, @NotNull CallbackRelay callbackRelay) throws PacketManager.DeserializationException {
        YamlConfiguration menuYAML = new YamlConfiguration();
        try {
            menuYAML.loadFromString(yamlString);
        } catch (InvalidConfigurationException e) {
            throw new PacketManager.DeserializationException(e);
        }

        YamlTools.ensureKeyExists(Constants.Communication.YAML.Keys.MenuReply.FILENAME, menuYAML);
        String menuID = menuYAML.getString(Constants.Communication.YAML.Keys.MenuReply.FILENAME);

        YamlTools.ensureKeyExists(Constants.Communication.YAML.Keys.MenuReply.CONTENT, menuYAML);
        ConfigurationSection contentSection = menuYAML.getConfigurationSection(Constants.Communication.YAML.Keys.MenuReply.CONTENT);
        if (contentSection == null) throw new PacketManager.DeserializationException("Menu '" + menuID + "' is missing a content section!", new NullPointerException("YAML section was null!"));

        List<GroupWidget> content = new ArrayList<>();

        for (String tLGroupKey : contentSection.getKeys(false)) {
            ConfigurationSection tLGroupSection = contentSection.getConfigurationSection(tLGroupKey);
            if (tLGroupSection == null) throw new PacketManager.DeserializationException("Top level group YAML section for '" + menuID + "." + tLGroupKey + "' is missing!", new NullPointerException("YAML section was null!"));
            Widget widget = deserialize(tLGroupSection, tLGroupKey);
            if (!(widget instanceof GroupWidget groupWidget)) throw new PacketManager.DeserializationException("Unexpected type '" + widget.getType() + "' for top level group!");
            content.add(groupWidget);
        }

        return AnimationMenu.create(menuID, content, callbackRelay);
    }

    /**
     * Attempts to serialize the specified widget using its {@link Widget#serialize()} method.
     * @param widget the widget to serialize
     * @return the serialized string or {@code null} if the specified widget type is not registered
     * @see #deserialize(Class, ConfigurationSection, String) 
     */
    public YamlConfiguration serialize(@NotNull Widget widget) {
        String widgetType = widget.getType();

        // Validate if the widget type exists in the registered widgets
        if (!registeredWidgets.containsKey(widgetType)) {
            LEDSuiteApplication.getLogger().info("Error: Widget type not registered: " + widgetType, new LEDSuiteLogAreas.YAML());
            return null;
        }

        // Serialize the widget directly using its method
        return widget.serialize();
    }

    /**
     * Attempts to deserialize the specified YAML config.
     * @param yamlString the YAML config to deserialize
     * @return the deserialized packet as instance of {@link AnimationMenuWidget}
     * @see #deserialize(Class, ConfigurationSection, String) 
     */
    public AnimationMenuWidget deserialize(ConfigurationSection yamlString, String parent) throws PacketManager.DeserializationException {
        //LEDSuiteApplication.getLogger().info("----------\n" + yamlString + "\n----------");
        return deserialize(AnimationMenuWidget.class, yamlString, parent);
    }

    /**
     * Attempts to deserialize the specified YAML config by retrieving its {@code type} value.
     * If the retrieved type is registered, the string will be deserialized using the specific widgets {@link com.toxicstoxm.LEDSuite.ui.animation_menu.Widget#deserialize(ConfigurationSection, String)} method.
     * @param clazz the corresponding widget implementation class
     * @param yaml the YAML config to deserialize
     * @return the deserialized widget
     * @param <T> the corresponding widget implementation class
     * @throws PacketManager.DeserializationException if the YAML config is invalid,
     * the {@code type} entry does not exist,
     * the {@code type} value is not registered,
     * the deserialized widget could not be cast the specified widget implementation class
     * @see #deserialize(ConfigurationSection, String) 
     */
    public <T extends com.toxicstoxm.LEDSuite.ui.animation_menu.Widget> T deserialize(@NotNull Class<T> clazz, @NotNull ConfigurationSection yaml, String parent) throws PacketManager.DeserializationException {
        YamlTools.ensureKeyExists(Constants.Communication.YAML.Keys.MenuReply.TYPE, yaml);
        String widgetType = yaml.getString(Constants.Communication.YAML.Keys.MenuReply.TYPE);

        if (!registeredWidgets.containsKey(widgetType)) {
            throw new PacketManager.DeserializationException("Invalid widget type: '" + widgetType + "'!");
        }

        // Retrieve the packet type and perform the deserialization
        try {
            com.toxicstoxm.LEDSuite.ui.animation_menu.Widget widgetInstance = registeredWidgets.get(widgetType).deserialize(yaml, parent);
            if (!clazz.isInstance(widgetInstance)) {
                throw new ClassCastException("Deserialized object is not of the expected type: " + clazz.getName());
            }
            return clazz.cast(widgetInstance);
        } catch (ClassCastException e) {
            throw new PacketManager.DeserializationException("Couldn't create " + clazz.getName() + " from the provided YAML config!", e);
        } catch (Exception e) {
            throw new PacketManager.DeserializationException("Deserialization for " + clazz.getName() + " failed!", e);
        }
    }
}
