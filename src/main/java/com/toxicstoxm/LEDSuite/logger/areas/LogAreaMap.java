package com.toxicstoxm.LEDSuite.logger.areas;

import java.awt.*;
import java.util.HashMap;
import java.util.List;

public class LogAreaMap extends HashMap<String, LogArea> {
    public Color getColorOfArea(String key) {
        if (!this.containsArea(key)) return null;
        return super.get(key).getColor();
    }

    public boolean isAreaEnabled(String area) {
        return containsArea(area) && get(area).isEnabled();
    }
    public boolean isAreaEnabled(LogArea area) {
        if (isAreaEnabled(area.getName())) return true;
        List<String> parents = area.getParents();
        if (parents != null) {
            for (String parent : parents) {
                if (isAreaEnabled(parent)) return true;
            }
        }
        return false;
    }

    public boolean containsArea(String key) {
       return containsKey(key);
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

    public boolean enableArea(String area) {
        if (!this.containsKey(area)) return false;
        super.get(area).setEnabled(true);
        return true;
    }
    public boolean disableArea(String area) {
        if (!this.containsKey(area)) return false;
        super.get(area).setEnabled(false);
        return true;
    }
}
