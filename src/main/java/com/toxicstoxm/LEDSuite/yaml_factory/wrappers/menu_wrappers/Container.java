package com.toxicstoxm.LEDSuite.yaml_factory.wrappers.menu_wrappers;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.LEDSuite;
import com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu;
import org.apache.commons.configuration2.Configuration;

import java.util.Iterator;
import java.util.TreeMap;

public interface Container {
    TreeMap<String, TreeMap<Integer, AnimationMenu.LEDSuiteWidget>> contents = new TreeMap<>();
    default TreeMap<Integer, AnimationMenu.LEDSuiteWidget> content() {
        contents.putIfAbsent(getClass().getName(), new TreeMap<>());
        return contents.get(getClass().getName());
    }

    default void putWidget(Integer pos, AnimationMenu.LEDSuiteWidget LEDSuiteWidget, String id) {
        LEDSuite.logger.debug(id + "Putting widget [" + LEDSuiteWidget + " (Gist: " + AnimationMenu.stringifyWidget(LEDSuiteWidget, pos) + ")] into parent widget [" + getClass().getName() + "]!");
        putWidget(pos, LEDSuiteWidget, false);
    }

    default void putWidget(Integer pos, AnimationMenu.LEDSuiteWidget LEDSuiteWidget, boolean force) {
        if (force) content().put(pos, LEDSuiteWidget);
        else content().putIfAbsent(pos, LEDSuiteWidget);
    }

    default void deserialize_children(Configuration contentSubset, String id, String parent, String parentPath) {

        LEDSuite.logger.debug(id + "Received request to deserialize children.");
        int i = 0;
        String last = "";

        // iterating through all widgets in the content section using an iterator since the string array returned by yaml.getKeys() does not support looping natively
        for (Iterator<String> it = contentSubset.getKeys(); it.hasNext(); /* check if iterator reached the end of the array*/ ) {
            // getting the next String from iterator
            String s = it.next().split("\\.")[0].strip();
            if (!s.matches(last)) {
                last = s;

                // getting the widgets section from the content YAML section
                Configuration widgetSubset = contentSubset.subset(s);

                // getting a widget type from widget YAML
                AnimationMenu.WidgetType type = AnimationMenu.WidgetType.enumOf(
                        widgetSubset.getString(Constants.Network.YAML.MENU.WIDGET_TYPE)
                );

                // creating a new widget according to the specified type from above
                AnimationMenu.Widget widget = new AnimationMenu.Widget(type);

                String child = System.currentTimeMillis() + "_" + i + ": Type: " + type;

                LEDSuite.logger.debug(id + "Requesting deserialization for child " + child + " (Parent: [" + parent + "])!");
                String path = parentPath.isBlank() ? s : parentPath + "." + s;

                // using the widgets deserialization function to deserialize the widget
                AnimationMenu.LEDSuiteWidget LEDSuiteWidget = widget.deserialize(widgetSubset, id, path);

                //LEDSuite.logger.debug(id + "Putting deserialized child " + child + " into " + parent + "!");

                // adding the widget to the corresponding group at the specified position
                this.putWidget(
                        Integer.parseInt(s.replace(Constants.Network.YAML.MENU.WIDGET_PREFIX, "").replace(Constants.Network.YAML.MENU.GROUP_PREFIX, "")), // getting widget position from YAML
                        LEDSuiteWidget,
                        id
                );
                i++;
            }

        }
        i = 0;
    }
}
