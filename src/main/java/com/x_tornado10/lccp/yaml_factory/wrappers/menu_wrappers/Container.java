package com.x_tornado10.lccp.yaml_factory.wrappers.menu_wrappers;

import com.x_tornado10.lccp.LCCP;
import com.x_tornado10.lccp.Paths;
import com.x_tornado10.lccp.yaml_factory.AnimationMenu;
import org.apache.commons.configuration2.Configuration;

import java.util.Iterator;
import java.util.TreeMap;

public interface Container {
    TreeMap<String, TreeMap<Integer, AnimationMenu.LCCPWidget>> contents = new TreeMap<>();
    default TreeMap<Integer, AnimationMenu.LCCPWidget> content() {
        contents.putIfAbsent(getClass().getName(), new TreeMap<>());
        return contents.get(getClass().getName());
    }

    default void putWidget(Integer pos, AnimationMenu.LCCPWidget LCCPWidget, String id) {
        LCCP.logger.debug(id + "Putting widget [" + LCCPWidget + " (Gist: " + AnimationMenu.stringifyWidget(LCCPWidget, pos) + ")] into parent widget [" + getClass().getName() + "]!");
        putWidget(pos, LCCPWidget, false);
    }

    default void putWidget(Integer pos, AnimationMenu.LCCPWidget LCCPWidget, boolean force) {
        if (force) content().put(pos, LCCPWidget);
        else content().putIfAbsent(pos, LCCPWidget);
    }

    default void deserialize_children(Configuration contentSubset, String id, String parent, String parentPath) {

        LCCP.logger.debug(id + "Received request to deserialize children.");
        int i = 0;
        String last = "";

        // iterating through all widgets in the content section using an iterator since the string array returned by yaml.getKeys() does not support looping natively
        for (Iterator<String> it = contentSubset.getKeys(); it.hasNext(); /* check if iterator reached the end of the array*/ ) {
            // getting next String from iterator
            String s = it.next().split("\\.")[0].strip();
            if (!s.matches(last)) {
                last = s;

                // getting the widgets section from the content yaml section
                Configuration widgetSubset = contentSubset.subset(s);

                // getting widget type from widget yaml
                AnimationMenu.WidgetType type = AnimationMenu.WidgetType.enumOf(
                        widgetSubset.getString(Paths.NETWORK.YAML.MENU.WIDGET_TYPE)
                );

                // creating a new widget according to the specified type from above
                AnimationMenu.Widget widget = new AnimationMenu.Widget(type);

                String child = System.currentTimeMillis() + "_" + i + ": Type: " + type;

                LCCP.logger.debug(id + "Requesting deserialization for child " + child + " (Parent: [" + parent + "])!");
                String path = parentPath.isBlank() ? s : parentPath + "." + s;

                // using the widgets deserialization function to deserialize the widget
                AnimationMenu.LCCPWidget LCCPWidget = widget.deserialize(widgetSubset, id, path);

                //LCCP.logger.debug(id + "Putting deserialized child " + child + " into " + parent + "!");

                // adding the widget to the corresponding group at the specified position
                this.putWidget(
                        Integer.parseInt(s.replace(Paths.NETWORK.YAML.MENU.WIDGET_PREFIX, "").replace(Paths.NETWORK.YAML.MENU.GROUP_PREFIX, "")), // getting widget position from yaml
                        LCCPWidget,
                        id
                );
                i++;
            }

        }
        i = 0;
    }
}
