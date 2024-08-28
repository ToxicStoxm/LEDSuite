package com.toxicstoxm.LEDSuite.logger.areas;

import java.awt.*;
import java.util.List;

public class LEDSuiteLogAreas implements LogAreaBundle {

    public static class General extends LEDSuiteLogArea {
        public General() {
            super(new Color(0, 140, 255));
        }
    }

    public static class Network extends LEDSuiteLogArea {}

    public static class YAMLEvents extends LEDSuiteLogArea {
        public YAMLEvents() {
            super(List.of(new Network().getName()));
        }
    }

    public static class Communication extends LEDSuiteLogArea {
        public Communication() {
            super(List.of(new Network().getName()));
        }
    }

    public static class UI extends LEDSuiteLogArea {}

    public static class UserInteractions extends LEDSuiteLogArea {
        public UserInteractions() {
            super(List.of(new UI().getName()));
        }
    }

    public static class UIConstruction extends LEDSuiteLogArea {
        public UIConstruction() {
            super(List.of(new UI().getName()));
        }
    }
}
