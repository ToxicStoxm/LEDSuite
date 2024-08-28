package com.toxicstoxm.LEDSuite.logger.areas;

import java.awt.*;
import java.util.Collection;
import java.util.List;

public interface LogAreaConfigurator {
    LogArea getArea();
    void setColor(Color color);
    void setParents(Collection<String> parents);
    default void addParent(String parent) {
        addParents(List.of(parent));
    }
    default void addParents(String... parents) {
        addParents(List.of(parents));
    }
    void addParents(Collection<String> parents);

    void setAreaParents(Collection<LogArea> parents);
    default void addAreaParent(LogArea parent) {
        addAreaParents(List.of(parent));
    }
    default void addAreaParents(LogArea... parents) {
        addAreaParents(List.of(parents));
    }
    void addAreaParents(Collection<LogArea> parents);
}
