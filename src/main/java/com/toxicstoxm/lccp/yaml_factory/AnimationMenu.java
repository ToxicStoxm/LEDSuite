package com.toxicstoxm.lccp.yaml_factory;

import com.toxicstoxm.lccp.LCCP;
import com.toxicstoxm.lccp.Constants;
import com.toxicstoxm.lccp.yaml_factory.wrappers.menu_wrappers.Container;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.ex.ConversionException;

import java.util.*;
@Getter
public class AnimationMenu implements Container {
    private final TreeMap<Integer, LCCPWidget> content;
    private final UUID networkID;
    @Setter
    private String label;
    @Setter
    private String icon;
    private boolean empty = false;

    public AnimationMenu(UUID networkID) {
        this.networkID = networkID;
        content = new TreeMap<>();
    }

    public static AnimationMenu empty() {
        AnimationMenu menu = new AnimationMenu(UUID.randomUUID());
        menu.empty = true;
        return menu;
    }

    public YAMLConfiguration serialize() throws YAMLSerializer.TODOException {
        throw new YAMLSerializer.TODOException("Won't implement");
    }

    public static AnimationMenu fromYAML(YAMLConfiguration yaml) {
        UUID networkID = UUID.fromString(yaml.getString(Constants.Network.YAML.INTERNAL_NETWORK_EVENT_ID));
        String id = "[" + networkID + "] ";

        LCCP.logger.debug(id + "Fulfilling deserialization request!");

        LCCP.logger.debug(id + "Removing network id!");
        yaml.clearProperty(Constants.Network.YAML.INTERNAL_NETWORK_EVENT_ID);

        AnimationMenu menu = new AnimationMenu(networkID);

        menu.setLabel(yaml.getString(Constants.Network.YAML.MENU.WIDGET_LABEL));
        menu.setIcon(yaml.getString(Constants.Network.YAML.MENU.WIDGET_ICON));

        yaml.clearProperty(Constants.Network.YAML.MENU.WIDGET_LABEL);
        yaml.clearProperty(Constants.Network.YAML.MENU.WIDGET_ICON);

        LCCP.logger.debug(id + "Removing widget tooltip!");
        yaml.clearProperty(Constants.Network.YAML.MENU.WIDGET_TOOLTIP);

        LCCP.logger.debug(id + "Requesting deserialization for children!");
        menu.deserialize_children(yaml, id, "menu", "");


        return menu;
    }

    protected interface _WidgetType {
        LCCPWidget deserialize(LCCPWidget widget, Configuration yaml, String id);
    }

    // wrapper enum for all known widget types along with their corresponding deserialize-function
    public enum WidgetType implements _WidgetType {
        // group widget type
        group() {
            // deserialization function for groups
            // parameters:
            //  - base widget (label, tooltip, style, type)
            //  - group Yaml (the corresponding section form the menu yaml for this group)
            // returns the deserialized group
            @Override
            public LCCPWidget deserialize(LCCPWidget initialWidget, Configuration groupYaml, String id) {
                LCCP.logger.debug(id + "Deserializing " + this.name());

                // casting the base widget to a group
                // (base widget is not a group object initially to allow for processing multiple widget types with the same functions)
                AnimationMenuGroup group = new AnimationMenuGroup();
                group.setLabel(initialWidget.label);
                group.setTooltip(initialWidget.tooltip);
                group.setStyle(initialWidget.style);
                group.setPath(initialWidget.path);

                // getting yaml section for group suffix from the group yaml
                Configuration suffixSubset = groupYaml.subset(Constants.Network.YAML.MENU.GROUP_SUFFIX_WIDGET);
                if (suffixSubset.getKeys().hasNext()) {
                    // setting the groups suffix to the one specified in the suffix yaml section
                    group.suffix = new Widget(
                            WidgetType.enumOf(suffixSubset.getString(Constants.Network.YAML.MENU.WIDGET_TYPE)) // getting suffix widget type from yaml
                    ).deserialize(suffixSubset, id,  initialWidget.path + ".suffix"); // deserializing the suffix widget using the corresponding deserialization function for the specific widget type
                }

                // getting yaml section for the groups content
                Configuration contentSubset = groupYaml.subset(Constants.Network.YAML.MENU.WIDGET_CONTENT);

                // deserialize all child widgets contained in this group
                group.deserialize_children(contentSubset, id,  initialWidget.type.name(), initialWidget.path);

                // returning group
                return group;
            }
        },
        button {
            @Override
            public LCCPWidget deserialize(LCCPWidget initialWidget, Configuration yaml, String id) {
                LCCP.logger.debug(id + "Deserializing " + this.name());
                Widgets.Button button = new Widgets.Button();

                button.setLabel(initialWidget.label);
                button.setTooltip(initialWidget.tooltip);
                button.setStyle(initialWidget.style);
                button.setPath(initialWidget.path);


                button.icon =  validate( yaml.getString(Constants.Network.YAML.MENU.WIDGET_ICON) );
                button.row = yaml.getBoolean(Constants.Network.YAML.MENU.BUTTON_ROW);

                return button;
            }
        },
        property {
            @Override
            public LCCPWidget deserialize(LCCPWidget initialWidget, Configuration yaml, String id) {
                LCCP.logger.debug(id + "Deserializing " + this.name());
                Widgets.Property property = new Widgets.Property();

                property.setLabel(initialWidget.label);
                property.setTooltip(initialWidget.tooltip);
                property.setStyle(initialWidget.style);
                property.setPath(initialWidget.path);

                property.content =  validate( yaml.getString(Constants.Network.YAML.MENU.WIDGET_CONTENT) );

                return property;
            }
        },
        _switch{
            @Override
            public LCCPWidget deserialize(LCCPWidget initialWidget, Configuration yaml, String id) {
                LCCP.logger.debug(id + "Deserializing " + this.name());
                Widgets.Switch _switch = new Widgets.Switch();

                _switch.setLabel(initialWidget.label);
                _switch.setTooltip(initialWidget.tooltip);
                _switch.setStyle(initialWidget.style);
                _switch.setPath(initialWidget.path);

                _switch.value = yaml.getBoolean(Constants.Network.YAML.MENU.WIDGET_VALUE);

                return _switch;
            }

            @Override
            public String toString() {
                return "switch";
            }
        },
        slider {
            @Override
            public LCCPWidget deserialize(LCCPWidget initialWidget, Configuration yaml, String id) {
                LCCP.logger.debug(id + "Deserializing " + this.name());
                Widgets.Slider slider = new Widgets.Slider();

                slider.setLabel(initialWidget.label);
                slider.setTooltip(initialWidget.tooltip);
                slider.setStyle(initialWidget.style);
                slider.setPath(initialWidget.path);

                slider.min = yaml.getDouble(Constants.Network.YAML.MENU.SLIDER_MIN);
                slider.max = yaml.getDouble(Constants.Network.YAML.MENU.SLIDER_MAX);
                slider.step = yaml.getDouble(Constants.Network.YAML.MENU.SLIDER_STEP);
                slider.climb_rate = yaml.getDouble(Constants.Network.YAML.MENU.SLIDER_CLIMB_RATE);
                slider.digits = yaml.getInt(Constants.Network.YAML.MENU.SLIDER_DIGITS);
                slider.numeric = yaml.getBoolean(Constants.Network.YAML.MENU.SLIDER_NUMERIC);
                slider.snap = yaml.getBoolean(Constants.Network.YAML.MENU.SLIDER_SNAP);
                slider.wraparound = yaml.getBoolean(Constants.Network.YAML.MENU.SLIDER_WRAPAROUND);
                slider.value = yaml.getDouble(Constants.Network.YAML.MENU.WIDGET_VALUE);

                return slider;
            }
        },
        entry {
            @Override
            public LCCPWidget deserialize(LCCPWidget initialWidget, Configuration yaml, String id) {
                LCCP.logger.debug(id + "Deserializing " + this.name());
                Widgets.Entry entry = new Widgets.Entry();

                entry.setLabel(initialWidget.label);
                entry.setTooltip(initialWidget.tooltip);
                entry.setStyle(initialWidget.style);
                entry.setPath(initialWidget.path);

                entry.content =  validate( yaml.getString(Constants.Network.YAML.MENU.WIDGET_CONTENT) );
                entry.applyButton = yaml.getBoolean(Constants.Network.YAML.MENU.ENTRY_APPLY_BUTTON);
                entry.editable = yaml.getBoolean(Constants.Network.YAML.MENU.ENTRY_EDITABLE);

                return entry;
            }
        },
        expander {
            @Override
            public LCCPWidget deserialize(LCCPWidget initialWidget, Configuration yaml, String id) {
                LCCP.logger.debug(id + "Deserializing " + this.name());
                Widgets.Expander expander = new Widgets.Expander();

                expander.setLabel(initialWidget.label);
                expander.setTooltip(initialWidget.tooltip);
                expander.setStyle(initialWidget.style);
                expander.setPath(initialWidget.path);

                expander.value = yaml.getBoolean(Constants.Network.YAML.MENU.WIDGET_VALUE);
                expander.toggleable = yaml.getBoolean(Constants.Network.YAML.MENU.EXPANDER_TOGGLEABLE);
                Configuration contentSubset = yaml.subset(Constants.Network.YAML.MENU.WIDGET_CONTENT);
                expander.deserialize_children(contentSubset, id, initialWidget.type.name(), initialWidget.path);

                return expander;
            }
        },
        dropdown {
            @Override
            public LCCPWidget deserialize(LCCPWidget initialWidget, Configuration yaml, String id) {
                LCCP.logger.debug(id + "Deserializing " + this.name());
                Widgets.Dropdown dropdown = new Widgets.Dropdown();

                dropdown.setLabel(initialWidget.label);
                dropdown.setTooltip(initialWidget.tooltip);
                dropdown.setStyle(initialWidget.style);

                dropdown.content = validate( yaml.getString(Constants.Network.YAML.MENU.WIDGET_CONTENT) );
                dropdown.searchable = yaml.getBoolean(Constants.Network.YAML.MENU.DROPDOWN_SEARCHABLE);
                dropdown.selected = yaml.getInt(Constants.Network.YAML.MENU.DROPDOWN_SELECTED);
                dropdown.dropdown = yaml.getList(String.class, Constants.Network.YAML.MENU.DROPDOWN);
                dropdown.setPath(initialWidget.path);

                return dropdown;
            }
        },
        spinner {
            @Override
            public LCCPWidget deserialize(LCCPWidget initialWidget, Configuration yaml, String id) {
                LCCP.logger.debug(id + "Deserializing " + this.name());
                Widgets.Spinner spinner = new Widgets.Spinner();

                spinner.setLabel(initialWidget.label);
                spinner.setTooltip(initialWidget.tooltip);
                spinner.setStyle(initialWidget.style);
                spinner.setPath(initialWidget.path);

                spinner.time = yaml.getDouble(Constants.Network.YAML.MENU.SPINNER_TIME);

                return spinner;

            }
        };
        public static WidgetType enumOf(String s) {
            for (WidgetType widgetType : values()) {
                if (widgetType.name().replace("_", "").equalsIgnoreCase(s)) return widgetType;
            }
            throw new IllegalArgumentException();
        }
    }

    @Getter
    @Setter
    public static class LCCPWidget {
        private String label;
        private String tooltip;
        private String style;
        @Getter
        private String path;
        private final WidgetType type;

        public LCCPWidget(WidgetType type) {
            this.type = type;
            path = "";
        }
        public LCCPWidget deserialize(Configuration yaml, String id, String path) {
            LCCP.logger.debug(id + "Deserializing generic values for child: " + type);
            this.label =  validate( yaml.getString(Constants.Network.YAML.MENU.WIDGET_LABEL) );
            this.tooltip =  validate( yaml.getString(Constants.Network.YAML.MENU.WIDGET_TOOLTIP) );
            this.style =  validate( yaml.getString(Constants.Network.YAML.MENU.WIDGET_STYLE) );
            this.path = path;
            LCCP.logger.debug(id + "Requesting specific deserialization for child " + type);

            LCCPWidget result = new LCCPWidget(WidgetType.property);

            try {
                result = type.deserialize(this, yaml, id);
            } catch (ConversionException | IllegalArgumentException | NoSuchElementException | NullPointerException e) {
                LCCP.logger.error(id + "Failed to deserialize " + type + " from yaml! Error message: " + e.getMessage());
                LCCP.logger.warn("Replacing entry with default missing value placeholder!");
                LCCP.logger.debug("Possible causes: missing or malformed values");
                LCCP.logger.error(e);
                Configuration config = new YAMLConfiguration();
                config.setProperty(Constants.Network.YAML.MENU.WIDGET_CONTENT, "Failed to deserialize! Error message: " + e.getMessage());
                try {
                    result = result.deserialize(config, id, "missing-value-" + System.currentTimeMillis());

                } catch (ConversionException | IllegalArgumentException | NoSuchElementException | NullPointerException ex) {
                    LCCP.logger.error(e);
                    LCCP.logger.fatal("FAILED TO DISPLAY MISSING VALUE PLACEHOLDER!");
                    LCCP.exit(1);
                }
            }
            return result;
        }

        public String toString(Integer pos) {
            return stringifyWidget(this, pos);
        }
    }

    public static class Widget {

        private final LCCPWidget LCCPWidget;

        public Widget(WidgetType type) {
            LCCPWidget = new LCCPWidget(type);
        }

        public LCCPWidget deserialize(Configuration yaml, String id, String path) {
            LCCP.logger.debug(id + "Received specific deserialization request for child: " + LCCPWidget.type);
            return LCCPWidget.deserialize(yaml, id, path);
        }
    }

    @Getter
    public static class AnimationMenuGroup extends LCCPWidget implements Container {
        private final TreeMap<Integer, LCCPWidget> content;
        private LCCPWidget suffix;

        public AnimationMenuGroup() {
            super(WidgetType.group);
            content = new TreeMap<>();
        }
        @Override
        public TreeMap<Integer, LCCPWidget> content() {
            return content;
        }
    }

    public static class Widgets {

        @Getter
        public static class Button extends LCCPWidget {
            private String icon = "";
            private boolean row = true;
            public Button() {
                super(WidgetType.button);
            }
        }

        @Getter
        public static class Property extends LCCPWidget {
            private String content = "";
            public Property() {
                super(WidgetType.property);
            }
        }

        @Getter
        public static class Switch extends LCCPWidget {
            private boolean value;

            public Switch() {
                super(WidgetType._switch);
            }
        }

        @Getter
        public static class Slider extends LCCPWidget {
            private double min = 0;
            private double max = 100;
            private double step = 1;
            private double value = 0;
            private double climb_rate = 2;
            private int digits = 0;
            private boolean numeric = true;
            private boolean snap = true;
            private boolean wraparound = true;

            public Slider() {
                super(WidgetType.slider);
            }
        }

        @Getter
        public static class Entry extends LCCPWidget {
            private String content = "";
            private boolean editable = true;
            private boolean applyButton = true;

            public Entry() {
                super(WidgetType.entry);
            }
        }

        @Getter
        public static class Expander extends LCCPWidget implements Container {
            private final TreeMap<Integer, LCCPWidget> content;
            private boolean toggleable = false;
            private boolean value = false;

            public Expander(){
                super(WidgetType.expander);
                content = new TreeMap<>();
            }
            @Override
            public TreeMap<Integer, LCCPWidget> content() {
                return content;
            }
        }

        @Getter
        public static class Dropdown extends LCCPWidget {
            private String content = "";
            private boolean searchable = true;
            private int selected = -1;
            private List<String> dropdown;

            public Dropdown() {
                super(WidgetType.dropdown);
                dropdown = new ArrayList<>();
            }
            public String getByIndex(int index) {
                return dropdown.get(index);
            }
        }

        @Getter
        public static class Spinner extends LCCPWidget {
            private double time;

            public Spinner() {
                super(WidgetType.spinner);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AnimationMenu {");
        sb.append("NetworkID: ").append(networkID).append(", ");
        sb.append("Content {");
        TreeMap<Integer, LCCPWidget> content = content();
        for (Map.Entry<Integer, LCCPWidget> entry : content.entrySet()) {
            LCCPWidget widget = entry.getValue();
            sb.append(stringifyWidget(widget, entry.getKey()));
        }
        sb.append("}}");

        return sb.toString();
    }

    public static String stringifyWidget(LCCPWidget widget, int pos) {
        StringBuilder sb = new StringBuilder();
        sb.append(capitalizeFirstLetter(widget.type.name())).append(" {");
        //sb.append("Name=").append(widget.widgetName).append(", ");
        sb.append("Position: ").append(pos).append(", ");
        sb.append("Label: ").append(widget.label).append(", ");
        sb.append("Tooltip: ").append(widget.tooltip);
        if (widget.style != null && !widget.style.isEmpty()) sb.append(", ").append("Style: ").append(widget.style);
        if (widget instanceof Container) {
            sb.append(", ").append("Content {");
            stringifyChildren((Container) widget, sb);
            sb.append("}");
        }
        sb.append("}");
        return sb.toString();
    }

    public static void stringifyChildren(Container c, StringBuilder sb) {
        for (Map.Entry<Integer, LCCPWidget> entry : c.content().entrySet()) {
            LCCPWidget widget = entry.getValue();
            sb.append(stringifyWidget(widget, entry.getKey())).append(", ");
        }
        sb.replace(sb.length() - 2, sb.length(), "");
    }

    public static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private static String validate(String subject) {
        return subject == null ? "" : subject;
    }
    @Override
    public TreeMap<Integer, LCCPWidget> content() {
        return content;
    }
}
