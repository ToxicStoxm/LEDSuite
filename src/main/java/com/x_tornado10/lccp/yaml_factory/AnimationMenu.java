package com.x_tornado10.lccp.yaml_factory;

import com.x_tornado10.lccp.LCCP;
import com.x_tornado10.lccp.Paths;
import com.x_tornado10.lccp.yaml_factory.wrappers.menu_wrappers.Container;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.YAMLConfiguration;

import java.util.*;

public class AnimationMenu implements Container {
    private UUID networkID;
    private boolean empty = false;

    public AnimationMenu(UUID networkID) {
        this.networkID = networkID;
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

        LCCP.logger.fatal("Fulfilling deserialization request! Content: ");

        for (Iterator<String> it = yaml.getKeys(); it.hasNext(); ) {
            String s = it.next();
            LCCP.logger.debug(s + ": " + yaml.getProperty(s));
        }

        UUID networkID = UUID.fromString(yaml.getString(Paths.NETWORK.YAML.INTERNAL_NETWORK_EVENT_ID));
        LCCP.logger.debug(String.valueOf(networkID));
        AnimationMenu menu = new AnimationMenu(networkID);
        yaml.clearProperty(Paths.NETWORK.YAML.INTERNAL_NETWORK_EVENT_ID);

        LCCP.logger.fatal("Removing network id for: " + networkID);

        for (Iterator<String> it = yaml.getKeys(); it.hasNext(); ) {
            String s = it.next();
            LCCP.logger.debug(s + ": " + yaml.getProperty(s));
        }

        LCCP.logger.fatal("Removing widget tooltip for: " + networkID);

        yaml.clearProperty(Paths.NETWORK.YAML.MENU.WIDGET_TOOLTIP);

        LCCP.logger.fatal("Requesting deserialization for children of menu: " + networkID);

        menu.deserialize_children(yaml);
        return menu;
    }

    protected interface _WidgetType {
        WidgetMain deserialize(WidgetMain widget, Configuration yaml);
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
            public WidgetMain deserialize(WidgetMain initialWidget, Configuration groupYaml) {
                LCCP.logger.fatal("Deserializing " + this.name());
                LCCP.logger.debug(initialWidget.type.name());

                for (Iterator<String> it = groupYaml.getKeys(); it.hasNext(); ) {
                    String s = it.next();
                    LCCP.logger.debug(s);
                }

                // casting the base widget to a group
                // (base widget is not a group object initially to allow for processing multiple widget types with the same functions)
                //AnimationMenuGroup group = (AnimationMenuGroup) initialWidget;
                AnimationMenuGroup group = new AnimationMenuGroup();
                group.setLabel(initialWidget.label);
                group.setTooltip(initialWidget.tooltip);
                group.setStyle(initialWidget.style);

                // getting yaml section for group suffix from the group yaml
                Configuration suffixSubset = groupYaml.subset(Paths.NETWORK.YAML.MENU.GROUP_SUFFIX_WIDGET);

                // setting the groups suffix to the one specified in the suffix yaml section
                /*group.suffix = new Widget(
                       WidgetType.valueOf(suffixSubset.getString(Paths.NETWORK.YAML.MENU.WIDGET_TYPE)) // getting suffix widget type from yaml
                ).deserialize(suffixSubset); // deserializing the suffix widget using the corresponding deserialization function for the specific widget type
                 */
                // getting yaml section for the groups content
                Configuration contentSubset = groupYaml.subset(Paths.NETWORK.YAML.MENU.WIDGET_CONTENT);

                // deserialize all child widgets contained in this group
                group.deserialize_children(contentSubset);

                // returning group
                return group;
            }
        },
        button {
            @Override
            public WidgetMain deserialize(WidgetMain initialWidget, Configuration yaml) {
                LCCP.logger.fatal("Deserializing " + this.name());
                Widgets.Button button = new Widgets.Button();

                button.setLabel(initialWidget.label);
                button.setTooltip(initialWidget.tooltip);
                button.setStyle(initialWidget.style);

                button.icon = yaml.getString(Paths.NETWORK.YAML.MENU.BUTTON_ICON);
                button.row = yaml.getBoolean(Paths.NETWORK.YAML.MENU.BUTTON_ROW);

                return button;
            }
        },
        property {
            @Override
            public WidgetMain deserialize(WidgetMain initialWidget, Configuration yaml) {
                LCCP.logger.fatal("Deserializing " + this.name());
                Widgets.Property property = new Widgets.Property();

                property.setLabel(initialWidget.label);
                property.setTooltip(initialWidget.tooltip);
                property.setStyle(initialWidget.style);

                property.content = yaml.getString(Paths.NETWORK.YAML.MENU.WIDGET_CONTENT);

                return property;
            }
        },
        _switch{
            @Override
            public WidgetMain deserialize(WidgetMain initialWidget, Configuration yaml) {
                LCCP.logger.fatal("Deserializing " + this.name());
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
            public WidgetMain deserialize(WidgetMain initialWidget, Configuration yaml) {
                LCCP.logger.fatal("Deserializing " + this.name());
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
            public WidgetMain deserialize(WidgetMain initialWidget, Configuration yaml) {
                LCCP.logger.fatal("Deserializing " + this.name());
                Widgets.Entry entry = new Widgets.Entry();

                entry.setLabel(initialWidget.label);
                entry.setTooltip(initialWidget.tooltip);
                entry.setStyle(initialWidget.style);

                entry.content = yaml.getString(Paths.NETWORK.YAML.MENU.WIDGET_CONTENT);
                entry.applyButton = yaml.getBoolean(Paths.NETWORK.YAML.MENU.ENTRY_APPLY_BUTTON);
                entry.editable = yaml.getBoolean(Paths.NETWORK.YAML.MENU.ENTRY_EDITABLE);

                return entry;
            }
        },
        expander {
            @Override
            public WidgetMain deserialize(WidgetMain initialWidget, Configuration yaml) {
                LCCP.logger.fatal("Deserializing " + this.name());
                Widgets.Expander expander = new Widgets.Expander();

                expander.setLabel(initialWidget.label);
                expander.setTooltip(initialWidget.tooltip);
                expander.setStyle(initialWidget.style);

                expander.value = yaml.getBoolean(Paths.NETWORK.YAML.MENU.WIDGET_VALUE);
                expander.toggleable = yaml.getBoolean(Paths.NETWORK.YAML.MENU.EXPANDER_TOGGLEABLE);
                YAMLConfiguration contentSubset = (YAMLConfiguration) yaml.subset(Paths.NETWORK.YAML.MENU.WIDGET_CONTENT);
                expander.deserialize_children(contentSubset);

                return expander;
            }
        },
        dropdown {
            @Override
            public WidgetMain deserialize(WidgetMain initialWidget, Configuration yaml) {
                LCCP.logger.fatal("Deserializing " + this.name());
                Widgets.Dropdown dropdown = new Widgets.Dropdown();

                dropdown.setLabel(initialWidget.label);
                dropdown.setTooltip(initialWidget.tooltip);
                dropdown.setStyle(initialWidget.style);

                dropdown.content = yaml.getString(Paths.NETWORK.YAML.MENU.WIDGET_CONTENT);
                dropdown.display_selected = yaml.getBoolean(Paths.NETWORK.YAML.MENU.DROPDOWN_DISPLAY_SELECTED);
                dropdown.searchable = yaml.getBoolean(Paths.NETWORK.YAML.MENU.DROPDOWN_SEARCHABLE);
                dropdown.selected = yaml.getInt(Paths.NETWORK.YAML.MENU.DROPDOWN_SELECTED);
                dropdown.dropdown = yaml.getList(String.class, Paths.NETWORK.YAML.MENU.DROPDOWN);

                return dropdown;
            }
        },
        spinner {
            @Override
            public WidgetMain deserialize(WidgetMain initialWidget, Configuration yaml) {
                LCCP.logger.fatal("Deserializing " + this.name());
                Widgets.Spinner spinner = new Widgets.Spinner();

                spinner.setLabel(initialWidget.label);
                spinner.setTooltip(initialWidget.tooltip);
                spinner.setStyle(initialWidget.style);

                spinner.time = yaml.getDouble(Paths.NETWORK.YAML.MENU.SPINNER_TIME);

                return spinner;
            }
        },
    }

    @Getter
    @Setter
    public static class WidgetMain {
        private String label;
        private String tooltip;
        private String style;
        private final WidgetType type;

        public WidgetMain(WidgetType type) {
            this.type = type;
        }
        public WidgetMain deserialize(Configuration yaml) {
            LCCP.logger.fatal("Deserializing generic values");
            this.label = yaml.getString(Paths.NETWORK.YAML.MENU.WIDGET_LABEL);
            this.tooltip = yaml.getString(Paths.NETWORK.YAML.MENU.WIDGET_TOOLTIP);
            this.style = yaml.getString(Paths.NETWORK.YAML.MENU.WIDGET_STYLE);
            LCCP.logger.fatal("Requesting deserialization for specific values");
            return type.deserialize(this, yaml);
        }
    }

    public static class Widget {

        private final WidgetMain widgetMain;

        public Widget(WidgetType type) {
            widgetMain = new WidgetMain(type);
        }

        public WidgetMain deserialize(Configuration yaml) {
            LCCP.logger.fatal("Received deserialization request for child!");
            return widgetMain.deserialize(yaml);
        }
    }


    protected static class AnimationMenuGroup extends WidgetMain implements Container {
        private WidgetMain suffix;

        public AnimationMenuGroup() {
            super(WidgetType.group);
        }
    }



    protected static class Widgets {
        protected static class Button extends WidgetMain {
            private String icon;
            private boolean row;
            public Button() {
                super(WidgetType.button);
            }
        }

        protected static class Property extends WidgetMain {
            private String content;
            public Property() {
                super(WidgetType.property);
            }
        }

        protected static class Switch extends WidgetMain {
            private boolean value;

            public Switch() {
                super(WidgetType._switch);
            }
        }

        protected static class Slider extends WidgetMain {
            private double min;
            private double max;
            private double step;
            private double value;
            private double climb_rate;
            private int digits;
            private boolean numeric;
            private boolean snap;
            private boolean wraparound;

            public Slider() {
                super(WidgetType.slider);
            }
        }

        protected static class Entry extends WidgetMain {
            private String content;
            private boolean editable;
            private boolean applyButton;

            public Entry() {
                super(WidgetType.entry);
            }
        }

        protected static class Expander extends WidgetMain implements Container {
            private boolean toggleable;
            private boolean value;

            public Expander(){
                super(WidgetType.expander);
            }
        }

        protected static class Dropdown extends WidgetMain {
            private String content;
            private boolean display_selected;
            private boolean searchable;
            private int selected;
            private List<String> dropdown;

            public Dropdown() {
                super(WidgetType.dropdown);
                dropdown = new ArrayList<>();
            }
        }

        protected static class Spinner extends WidgetMain {
            private double time;

            public Spinner() {
                super(WidgetType.spinner);
            }
        }
    }
}
