package com.toxicstoxm.LEDSuite.logger.areas;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class LogAreaMap extends HashMap<String, LogArea> {
    public Color getColorOfArea(String key) {
        if (!this.containsArea(key)) return null;
        return super.get(key).getColor();
    }

    public boolean containsArea(String key) {
        if (super.containsKey(key)) return true;
        for (Map.Entry<String, LogArea> entry : super.entrySet()) {
            if (entry.getValue().getParents().contains(key)) {
                return true;
            }
        }
        return false;
    }

    public void registerArea(LogArea area) {
        super.put(area.getName(), area);
    }

    public boolean unregisterArea(LogArea area) {
        return unregisterArea(area.getName());
    }
    public boolean unregisterArea(String area) {
        if (!this.containsArea(area)) return false;
        super.remove(area);
        return true;
    }
}
