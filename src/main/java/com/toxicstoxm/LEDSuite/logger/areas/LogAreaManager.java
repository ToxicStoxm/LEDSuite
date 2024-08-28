package com.toxicstoxm.LEDSuite.logger.areas;

public interface LogAreaManager {
    void registerArea(LogArea logArea);
    default void registerAreaBundle(LogAreaBundle logAreaBundle) {
        logAreaBundle.getAreas().forEach(this::registerArea);
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
}
