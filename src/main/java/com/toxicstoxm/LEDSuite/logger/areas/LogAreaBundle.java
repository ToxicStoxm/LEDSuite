package com.toxicstoxm.LEDSuite.logger.areas;

import java.awt.Color;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public interface LogAreaBundle {
    default Collection<LogArea> getAreas() {
        List<LogArea> areas = new ArrayList<>();

        // Iterate over all declared classes within the implementing class
        for (Class<?> clazz : this.getClass().getDeclaredClasses()) {
            if (LogArea.class.isAssignableFrom(clazz)) {
                try {
                    // Get all constructors of the class
                    Constructor<?>[] constructors = clazz.getDeclaredConstructors();

                    // Sort constructors by the number of parameters (descending order)
                    List<Constructor<?>> sortedConstructors = Arrays.stream(constructors)
                            .sorted((c1, c2) -> Integer.compare(c2.getParameterCount(), c1.getParameterCount()))
                            .toList();

                    // Attempt to create an instance using each constructor
                    for (Constructor<?> constructor : sortedConstructors) {
                        try {
                            // Prepare default arguments for each parameter type
                            Object[] args = Arrays.stream(constructor.getParameterTypes())
                                    .map(this::getDefaultArgumentForType)
                                    .toArray();

                            // Attempt to instantiate the object
                            LogArea area = (LogArea) constructor.newInstance(args);
                            areas.add(area);
                            break; // Break if successful
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            // Handle exceptions, continue to the next constructor
                            System.out.println("Failed to instantiate using constructor: " + constructor);
                        }
                    }

                } catch (Exception e) {
                    // General exception handling (log or print as needed)
                    System.out.println("Error processing class: " + clazz.getName());
                }
            }
        }
        return areas;
    }

    // Utility method to provide default arguments based on parameter type
    private Object getDefaultArgumentForType(Class<?> type) {
        if (type == String.class) return "defaultName";
        if (type == Color.class) return Color.BLACK; // or any default color
        if (Collection.class.isAssignableFrom(type)) return new ArrayList<>();
        if (type == boolean.class || type == Boolean.class) return false;
        // Add more default values if necessary for other types
        return null; // Or handle other cases as needed
    }
}
