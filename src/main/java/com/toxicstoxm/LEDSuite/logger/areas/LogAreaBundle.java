package com.toxicstoxm.LEDSuite.logger.areas;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface LogAreaBundle {
    default Collection<LogArea> getAreas() {
        List<LogArea> areas = new ArrayList<>();
        for (Class<?> clazz : this.getClass().getDeclaredClasses()) {
            if (LogArea.class.isAssignableFrom(clazz)) {
                try {
                    Constructor<?> constructor = clazz.getConstructor();
                    areas.add((LogArea) constructor.newInstance());
                } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                         IllegalAccessException e) {
                    System.out.println("FUCK! (ADD BETTER ERROR MESSAGE)");
                }
            }

        }
        return areas;
    }
}
