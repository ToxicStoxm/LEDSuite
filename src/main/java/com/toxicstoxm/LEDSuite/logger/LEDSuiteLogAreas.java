package com.toxicstoxm.LEDSuite.logger;

import lombok.NonNull;

import java.awt.*;
import java.util.Collection;
import java.util.List;

public class LEDSuiteLogAreas {

    public static class General extends LEDSuiteLogArea {
        public General() {
            super("General");
        }
    }

    public static class Network extends LEDSuiteLogArea {

        public Network() {
            super("Network");
        }
    }

    public static class YAMLEvents extends LEDSuiteLogArea {
        public YAMLEvents() {
            super("YAMLEvents", List.of(new Network().getName()));
        }
    }

    public static class Communication extends LEDSuiteLogArea {
        public Communication() {
            super("Communication", List.of(new Network().getName()));
        }
    }

    public static class UI extends LEDSuiteLogArea {
        public UI() {
            super("UI");
        }
    }

    public static class UserInteractions extends LEDSuiteLogArea {
        public UserInteractions() {
            super("UserInteractions", List.of(new UI().getName()));
        }
    }

    public static class UIConstruction extends LEDSuiteLogArea {
        public UIConstruction() {
            super("UI Construction", List.of(new UI().getName()));
        }
    }

    public static class Custom extends LEDSuiteLogArea {
        public Custom(@NonNull String name) {
            super(name);
        }

        public Custom(@NonNull String name, Color color) {
            super(name, color);
        }

        public Custom(@NonNull String name, Collection<String> parents) {
            super(name, parents);
        }

        public Custom(@NonNull String name, Color color, Collection<String> parents) {
            super(name, color, parents);
        }
    }
}
