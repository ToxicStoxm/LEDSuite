package com.x_tornado10.lccp.yaml_factory.wrappers.menu_wrappers;

import com.x_tornado10.lccp.LCCP;
import com.x_tornado10.lccp.Paths;
import com.x_tornado10.lccp.yaml_factory.AnimationMenu;
import org.apache.commons.configuration2.Configuration;

import java.util.Iterator;
import java.util.TreeMap;
import java.util.UUID;

public interface Container {
    TreeMap<Integer, AnimationMenu.WidgetMain> content = new TreeMap<>();

    default void putWidget(Integer pos, AnimationMenu.WidgetMain widgetMain) {
        putWidget(pos, widgetMain, false);
    }

    default void putWidget(Integer pos, AnimationMenu.WidgetMain widgetMain, boolean force) {
        if (force) content.put(pos, widgetMain);
        else content.putIfAbsent(pos, widgetMain);
    }

    default TreeMap<Integer, AnimationMenu.WidgetMain> getContent() {
        return content;
    }

    default void deserialize_children(Configuration contentSubset, String id) {

        LCCP.logger.debug(id + "Received request to deserialize children.");
        int i = 0;
        String last = "";

        // iterating through all widgets in the content section using an iterator since the string array returned by yaml.getKeys() does not support looping natively
        for (Iterator<String> it = contentSubset.getKeys(); it.hasNext(); /* check if iterator reached the end of the array*/ ) {
            // getting next String from iterator
            String s = it.next().split("\\.")[0].strip();
            if (!s.matches(last)) {
                last = s;

                /*for (Iterator<String> its = contentSubset.getKeys(); its.hasNext(); ) {
                    String ss = its.next();
                    LCCP.logger.debug(ss);
                }*/

                LCCP.logger.debug(id + "Child: " + System.currentTimeMillis() + "_" + i);


                // getting the widgets section from the content yaml section
                Configuration widgetSubset = contentSubset.subset(s);

                /*for (Iterator<String> its = widgetSubset.getKeys(); its.hasNext(); ) {
                    String ss = its.next();
                    LCCP.logger.debug(ss + ": " + widgetSubset.getProperty(ss));
                }*/

                LCCP.logger.debug(id + "Getting child " + System.currentTimeMillis() + "_" + i + " type.");

                // getting widget type from widget yaml
                AnimationMenu.WidgetType type = AnimationMenu.WidgetType.valueOf(
                        widgetSubset.getString(Paths.NETWORK.YAML.MENU.WIDGET_TYPE)
                );

                LCCP.logger.debug(id + "Child " + System.currentTimeMillis() + "_" + i + ": Type: " + type);

                // creating a new widget according to the specified type from above
                AnimationMenu.Widget widget = new AnimationMenu.Widget(type);

                LCCP.logger.debug(id + "Requesting deserialization for child " + System.currentTimeMillis() + "_" + i);

                // using the widgets deserialization function to deserialize the widget
                AnimationMenu.WidgetMain widgetMain = widget.deserialize(widgetSubset, id);

                LCCP.logger.debug(id + "Putting deserialized child " + System.currentTimeMillis() + "_" + i + " into parent!");

                // adding the widget to the corresponding group at the specified position
                this.putWidget(
                        Integer.parseInt(s.replace(Paths.NETWORK.YAML.MENU.WIDGET_PREFIX, "").replace(Paths.NETWORK.YAML.MENU.GROUP_PREFIX, "")), // getting widget position from yaml
                        widgetMain
                );
                i++;
            }

        }
        i = 0;
    }
}
