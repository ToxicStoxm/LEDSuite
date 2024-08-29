package com.toxicstoxm.LEDSuite.logger.areas;


import java.awt.*;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface LogAreaManager {
    void registerArea(LogArea logArea);
    default void registerAreaBundle(LogAreaBundle logAreaBundle) {
        logAreaBundle.getAreas().forEach(this::registerArea);
    }
    Color getAreaColor(String area);
    boolean enableArea(String string);
    default boolean enableArea(LogArea logArea) {
        return enableArea(logArea.getName());
    }
    boolean disableArea(String area);
    default boolean disableArea(LogArea logArea) {
        return disableArea(logArea.getName());
    }

    default void enableAreasByName(Collection<String> areas) {
        areas.forEach(this::enableArea);
    }
    default void disableAreas(Collection<String> areas) {
        areas.forEach(this::disableArea);
    }
    default void enableAreas(Collection<LogArea> areas) {
        areas.forEach(logArea -> enableArea(logArea.getName()));
    }
    default void disableAreasByName(Collection<LogArea> areas) {
        areas.forEach(logArea -> disableArea(logArea.getName()));
    }
    boolean isAreaEnabled(String area);
    default  boolean isAreaEnabled(LogArea area) {
        return isAreaEnabled(area.getName());
    }
    LogAreaConfigurator configureArea(String area);
    default LogAreaConfigurator configureArea(LogArea area) {
        return configureArea(area.getName());
    }
    boolean unregisterArea(LogArea logArea);
    Set<Map.Entry<String, LogArea>> entrySet();
}
