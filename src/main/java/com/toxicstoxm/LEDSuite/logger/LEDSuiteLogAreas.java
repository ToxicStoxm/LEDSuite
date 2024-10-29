package com.toxicstoxm.LEDSuite.logger;

import com.toxicstoxm.YAJL.areas.LogAreaBundle;
import com.toxicstoxm.YAJL.areas.YAJLLogArea;

import java.awt.*;
import java.util.List;

/**
 * YAJL log area bundle for LEDSuite.
 * @since 1.0.0
 * @see YAJLLogArea
 * @see LogAreaBundle
 */
public class LEDSuiteLogAreas implements LogAreaBundle {

    public static class GENERAL extends YAJLLogArea {
        public GENERAL() {
            super(new Color(85, 85, 85));
        }
    }

    public static class NETWORK extends YAJLLogArea {
        public NETWORK() {
            super(new Color(0, 149, 156));
        }
    }

    public static class YAML extends YAJLLogArea {
        public YAML() {
            super(new Color(0, 89, 255), List.of(new NETWORK().getName()));
        }
    }

    public static class COMMUNICATION extends YAJLLogArea {
        public COMMUNICATION() {
            super(new Color(0, 38, 151), List.of(new NETWORK().getName()));
        }
    }

    public static class UI extends YAJLLogArea {
        public UI() {
            super(new Color(84, 0, 184));
        }
    }

    public static class USER_INTERACTIONS extends YAJLLogArea {
        public USER_INTERACTIONS() {
            super(new Color(130, 38, 248), List.of(new UI().getName()));
        }
    }

    public static class UI_CONSTRUCTION extends YAJLLogArea {
        public UI_CONSTRUCTION() {
            super(new Color(221, 0, 255), List.of(new UI().getName()));
        }
    }
}
