package com.toxicstoxm.LEDSuite.logger.areas;

import lombok.NonNull;

import java.awt.*;
import java.util.List;

public class LEDSuiteLogAreas implements LogAreaBundle {

    public static class GENERAL extends LEDSuiteLogArea {
        public GENERAL() {
            super(new Color(0, 140, 255));
        }
    }

    public static class NETWORK extends LEDSuiteLogArea {
        public NETWORK() {
            super(new Color(255, 0, 0));
        }
    }

    public static class YAML_EVENTS extends LEDSuiteLogArea {
        public YAML_EVENTS() {
            super(new Color(153, 0, 255), List.of(new NETWORK().getName()));
        }
    }

    public static class COMMUNICATION extends LEDSuiteLogArea {
        public COMMUNICATION() {
            super(new Color(122, 255, 0), List.of(new NETWORK().getName()));
        }
    }

    public static class UI extends LEDSuiteLogArea {
        public UI() {
            super(new Color(255, 106, 0));
        }
    }

    public static class USER_INTERACTIONS extends LEDSuiteLogArea {
        public USER_INTERACTIONS() {
            super(new Color(2, 253, 217), List.of(new UI().getName()));
        }
    }

    public static class UI_CONSTRUCTION extends LEDSuiteLogArea {
        public UI_CONSTRUCTION() {
            super(new Color(0, 4, 255), List.of(new UI().getName()));
        }
    }
}
