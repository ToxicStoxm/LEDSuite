package com.x_tornado10.lccp.yaml_factory;

import com.x_tornado10.lccp.LCCP;
import com.x_tornado10.lccp.Paths;
import com.x_tornado10.lccp.yaml_factory.wrappers.menu_wrappers.Container;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.ex.ConversionException;

import java.util.*;
@Getter
public class AnimationMenu implements Container {
    private TreeMap<Integer, WidgetMain> content;
    private UUID networkID;
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
        UUID networkID = UUID.fromString(yaml.getString(Paths.NETWORK.YAML.INTERNAL_NETWORK_EVENT_ID));
        AnimationMenu menu = new AnimationMenu(networkID);
        yaml.clearProperty(Paths.NETWORK.YAML.INTERNAL_NETWORK_EVENT_ID);
        menu.setLabel(Paths.NETWORK.YAML.MENU.WIDGET_LABEL);
        menu.setIcon(Paths.NETWORK.YAML.MENU.WIDGET_ICON);
        yaml.clearProperty(Paths.NETWORK.YAML.MENU.WIDGET_LABEL);
        yaml.clearProperty(Paths.NETWORK.YAML.MENU.WIDGET_ICON);
        String id = "[" + networkID + "] ";

        LCCP.logger.debug(id + "Fulfilling deserialization request!");


        LCCP.logger.debug(id + "Removing network id!");

        /*for (Iterator<String> it = yaml.getKeys(); it.hasNext(); ) {
            String s = it.next();
            LCCP.logger.debug(s + ": " + yaml.getProperty(s));
        }*/

        LCCP.logger.debug(id + "Removing widget tooltip!");

        yaml.clearProperty(Paths.NETWORK.YAML.MENU.WIDGET_TOOLTIP);

        LCCP.logger.debug(id + "Requesting deserialization for children!");

        menu.deserialize_children(yaml, id, "menu");
        return menu;
    }

    protected interface _WidgetType {
        WidgetMain deserialize(WidgetMain widget, Configuration yaml, String id);
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
            public WidgetMain deserialize(WidgetMain initialWidget, Configuration groupYaml, String id) {
                LCCP.logger.debug(id + "Deserializing " + this.name());

                // casting the base widget to a group
                // (base widget is not a group object initially to allow for processing multiple widget types with the same functions)
                //AnimationMenuGroup group = (AnimationMenuGroup) initialWidget;
                AnimationMenuGroup group = new AnimationMenuGroup();
                group.setLabel(initialWidget.label);
                group.setTooltip(initialWidget.tooltip);
                group.setStyle(initialWidget.style);
                //group.setWidgetName(initialWidget.widgetName);


                // getting yaml section for group suffix from the group yaml
                Configuration suffixSubset = groupYaml.subset(Paths.NETWORK.YAML.MENU.GROUP_SUFFIX_WIDGET);
                if (suffixSubset.getKeys().hasNext()) {
                    // setting the groups suffix to the one specified in the suffix yaml section
                    group.suffix = new Widget(
                            WidgetType.enumOf(suffixSubset.getString(Paths.NETWORK.YAML.MENU.WIDGET_TYPE)) // getting suffix widget type from yaml
                    ).deserialize(suffixSubset, id); // deserializing the suffix widget using the corresponding deserialization function for the specific widget type
                }

                // getting yaml section for the groups content
                Configuration contentSubset = groupYaml.subset(Paths.NETWORK.YAML.MENU.WIDGET_CONTENT);

                // deserialize all child widgets contained in this group
                group.deserialize_children(contentSubset, id,  initialWidget.type.name());

                // returning group
                return group;
            }
        },
        button {
            @Override
            public WidgetMain deserialize(WidgetMain initialWidget, Configuration yaml, String id) {
                LCCP.logger.debug(id + "Deserializing " + this.name());
                Widgets.Button button = new Widgets.Button();

                button.setLabel(initialWidget.label);
                button.setTooltip(initialWidget.tooltip);
                button.setStyle(initialWidget.style);
                //button.setWidgetName(initialWidget.widgetName);


                button.icon =  validate( yaml.getString(Paths.NETWORK.YAML.MENU.WIDGET_ICON) );
                button.row = yaml.getBoolean(Paths.NETWORK.YAML.MENU.BUTTON_ROW);

                return button;
            }
        },
        property {
            @Override
            public WidgetMain deserialize(WidgetMain initialWidget, Configuration yaml, String id) {
                LCCP.logger.debug(id + "Deserializing " + this.name());
                Widgets.Property property = new Widgets.Property();

                property.setLabel(initialWidget.label);
                property.setTooltip(initialWidget.tooltip);
                property.setStyle(initialWidget.style);


                property.content =  validate( yaml.getString(Paths.NETWORK.YAML.MENU.WIDGET_CONTENT) );

                return property;
            }
        },
        _switch{
            @Override
            public WidgetMain deserialize(WidgetMain initialWidget, Configuration yaml, String id) {
                LCCP.logger.debug(id + "Deserializing " + this.name());
                Widgets.Switch _switch = new Widgets.Switch();

                _switch.setLabel(initialWidget.label);
                _switch.setTooltip(initialWidget.tooltip);
                _switch.setStyle(initialWidget.style);


                _switch.value = yaml.getBoolean(Paths.NETWORK.YAML.MENU.WIDGET_VALUE);

                return _switch;
            }

            @Override
            public String toString() {
                return "switch";
            }
        },
        slider {
            @Override
            public WidgetMain deserialize(WidgetMain initialWidget, Configuration yaml, String id) {
                LCCP.logger.debug(id + "Deserializing " + this.name());
                Widgets.Slider slider = new Widgets.Slider();

                slider.setLabel(initialWidget.label);
                slider.setTooltip(initialWidget.tooltip);
                slider.setStyle(initialWidget.style);

                slider.min = yaml.getDouble(Paths.NETWORK.YAML.MENU.SLIDER_MIN);
                slider.max = yaml.getDouble(Paths.NETWORK.YAML.MENU.SLIDER_MAX);
                slider.step = yaml.getDouble(Paths.NETWORK.YAML.MENU.SLIDER_STEP);
                slider.climb_rate = yaml.getDouble(Paths.NETWORK.YAML.MENU.SLIDER_CLIMB_RATE);
                slider.digits = yaml.getInt(Paths.NETWORK.YAML.MENU.SLIDER_DIGITS);
                slider.numeric = yaml.getBoolean(Paths.NETWORK.YAML.MENU.SLIDER_NUMERIC);
                slider.snap = yaml.getBoolean(Paths.NETWORK.YAML.MENU.SLIDER_SNAP);
                slider.wraparound = yaml.getBoolean(Paths.NETWORK.YAML.MENU.SLIDER_WRAPAROUND);
                slider.value = yaml.getDouble(Paths.NETWORK.YAML.MENU.WIDGET_VALUE);

                return slider;
            }
        },
        entry {
            @Override
            public WidgetMain deserialize(WidgetMain initialWidget, Configuration yaml, String id) {
                LCCP.logger.debug(id + "Deserializing " + this.name());
                Widgets.Entry entry = new Widgets.Entry();

                entry.setLabel(initialWidget.label);
                entry.setTooltip(initialWidget.tooltip);
                entry.setStyle(initialWidget.style);

                entry.content =  validate( yaml.getString(Paths.NETWORK.YAML.MENU.WIDGET_CONTENT) );
                entry.applyButton = yaml.getBoolean(Paths.NETWORK.YAML.MENU.ENTRY_APPLY_BUTTON);
                entry.editable = yaml.getBoolean(Paths.NETWORK.YAML.MENU.ENTRY_EDITABLE);

                return entry;
            }
        },
        expander {
            @Override
            public WidgetMain deserialize(WidgetMain initialWidget, Configuration yaml, String id) {
                LCCP.logger.debug(id + "Deserializing " + this.name());
                Widgets.Expander expander = new Widgets.Expander();

                expander.setLabel(initialWidget.label);
                expander.setTooltip(initialWidget.tooltip);
                expander.setStyle(initialWidget.style);

                expander.value = yaml.getBoolean(Paths.NETWORK.YAML.MENU.WIDGET_VALUE);
                expander.toggleable = yaml.getBoolean(Paths.NETWORK.YAML.MENU.EXPANDER_TOGGLEABLE);
                Configuration contentSubset = yaml.subset(Paths.NETWORK.YAML.MENU.WIDGET_CONTENT);
                expander.deserialize_children(contentSubset, id, initialWidget.type.name());

                return expander;
            }
        },
        dropdown {
            @Override
            public WidgetMain deserialize(WidgetMain initialWidget, Configuration yaml, String id) {
                LCCP.logger.debug(id + "Deserializing " + this.name());
                Widgets.Dropdown dropdown = new Widgets.Dropdown();

                dropdown.setLabel(initialWidget.label);
                dropdown.setTooltip(initialWidget.tooltip);
                dropdown.setStyle(initialWidget.style);

                dropdown.content = validate( yaml.getString(Paths.NETWORK.YAML.MENU.WIDGET_CONTENT) );
                dropdown.searchable = yaml.getBoolean(Paths.NETWORK.YAML.MENU.DROPDOWN_SEARCHABLE);
                dropdown.selected = yaml.getInt(Paths.NETWORK.YAML.MENU.DROPDOWN_SELECTED);
                dropdown.dropdown = yaml.getList(String.class, Paths.NETWORK.YAML.MENU.DROPDOWN);

                return dropdown;
            }
        },
        spinner {
            @Override
            public WidgetMain deserialize(WidgetMain initialWidget, Configuration yaml, String id) {
                LCCP.logger.debug(id + "Deserializing " + this.name());
                Widgets.Spinner spinner = new Widgets.Spinner();

                spinner.setLabel(initialWidget.label);
                spinner.setTooltip(initialWidget.tooltip);
                spinner.setStyle(initialWidget.style);

                spinner.time = yaml.getDouble(Paths.NETWORK.YAML.MENU.SPINNER_TIME);

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
    public static class WidgetMain {
        private String label;
        private String tooltip;
        private String style;
        private String path;
        private final WidgetType type;

        public WidgetMain(WidgetType type) {
            this.type = type;
        }
        public WidgetMain deserialize(Configuration yaml, String id) {
            LCCP.logger.debug(id + "Deserializing generic values for child: " + type);
            this.label =  validate( yaml.getString(Paths.NETWORK.YAML.MENU.WIDGET_LABEL) );
            this.tooltip =  validate( yaml.getString(Paths.NETWORK.YAML.MENU.WIDGET_TOOLTIP) );
            this.style =  validate( yaml.getString(Paths.NETWORK.YAML.MENU.WIDGET_STYLE) );
            LCCP.logger.debug(id + "Requesting specific deserialization for child " + type);

            WidgetMain result = new WidgetMain(WidgetType.property);

            try {
                result = type.deserialize(this, yaml, id);
            } catch (ConversionException | IllegalArgumentException | NoSuchElementException | NullPointerException e) {
                LCCP.logger.error(id + "Failed to deserialize " + type + " from yaml! Error message: " + e.getMessage());
                LCCP.logger.warn("Replacing entry with default missing value placeholder!");
                LCCP.logger.debug("Possible causes: missing or malformed values");
                LCCP.logger.error(e);
                Configuration config = new YAMLConfiguration();
                config.setProperty(Paths.NETWORK.YAML.MENU.WIDGET_CONTENT, "Failed to deserialize! Error message: " + e.getMessage());
                try {
                    result = result.deserialize(config, id);

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
        public String getPath() {
            path = String.valueOf(UUID.randomUUID());
            return path;
        }
    }

    public static class Widget {

        private final WidgetMain widgetMain;

        public Widget(WidgetType type) {
            widgetMain = new WidgetMain(type);
        }

        public WidgetMain deserialize(Configuration yaml, String id) {
            LCCP.logger.debug(id + "Received specific deserialization request for child: " + widgetMain.type);
            return widgetMain.deserialize(yaml, id);
        }
    }


    @Getter
    public static class AnimationMenuGroup extends WidgetMain implements Container {
        private TreeMap<Integer, WidgetMain> content;
        private WidgetMain suffix;

        public AnimationMenuGroup() {
            super(WidgetType.group);
            content = new TreeMap<>();
        }
        @Override
        public TreeMap<Integer, WidgetMain> content() {
            return content;
        }
    }



    public static class Widgets {

        @Getter
        public static class Button extends WidgetMain {
            private String icon = "";
            private boolean row = true;
            public Button() {
                super(WidgetType.button);
            }
        }

        @Getter
        public static class Property extends WidgetMain {
            private String content = "";
            public Property() {
                super(WidgetType.property);
            }
        }

        @Getter
        public static class Switch extends WidgetMain {
            private boolean value;

            public Switch() {
                super(WidgetType._switch);
            }
        }

        @Getter
        public static class Slider extends WidgetMain {
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
        public static class Entry extends WidgetMain {
            private String content = "";
            private boolean editable = true;
            private boolean applyButton = true;

            public Entry() {
                super(WidgetType.entry);
            }
        }

        @Getter
        public static class Expander extends WidgetMain implements Container {
            private TreeMap<Integer, WidgetMain> content;
            private boolean toggleable = false;
            private boolean value = false;

            public Expander(){
                super(WidgetType.expander);
                content = new TreeMap<>();
            }
            @Override
            public TreeMap<Integer, WidgetMain> content() {
                return content;
            }
        }

        @Getter
        public static class Dropdown extends WidgetMain {
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
        public static class Spinner extends WidgetMain {
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
        TreeMap<Integer, WidgetMain> content = content();
        for (Map.Entry<Integer, WidgetMain> entry : content.entrySet()) {
            WidgetMain widget = entry.getValue();
            sb.append(stringifyWidget(widget, entry.getKey()));
            //sb.replace(sb.length() - 1, sb.length(), "");
            //sb.append(", Content {");
            //stringifyChildren((Container) widget, sb);
            //sb.append("}");
        }
        sb.append("}}");

        return sb.toString();
    }

    public static String stringifyWidget(WidgetMain widget, int pos) {
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
        for (Map.Entry<Integer, AnimationMenu.WidgetMain> entry : c.content().entrySet()) {
            AnimationMenu.WidgetMain widget = entry.getValue();
            //LCCP.logger.warn(c.getClass().getName() + ": " + widget);
            //LCCP.logger.fatal("sub type: " + widget.type.name());
            sb.append(stringifyWidget(widget, entry.getKey())).append(", ");
            //if (widget instanceof Container) {
                //sb.replace(sb.length() - 3, sb.length(), "");
                //sb.append("Content {");
                //stringifyChildren((Container) widget, sb);
                //sb.append("}");
                //sb.append("}, ");
            //}
        }
        sb.replace(sb.length() - 2, sb.length(), "");
    }

    private static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private static String validate(String subject) {
        return subject == null ? "" : subject;
    }
    @Override
    public TreeMap<Integer, WidgetMain> content() {
        return content;
    }
}
