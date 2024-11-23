package com.toxicstoxm.LEDSuite.logger;

import com.toxicstoxm.YAJL.areas.LogAreaBundle;
import com.toxicstoxm.YAJL.areas.YAJLLogArea;

import java.awt.*;
import java.util.List;

/**
 * Defines log areas for categorizing LEDSuite logs with distinct colors.
 * Each log area represents a specific aspect of the application, aiding in easier log analysis.
 * @since 1.0.0
 * @see YAJLLogArea
 * @see LogAreaBundle
 */
public class LEDSuiteLogAreas implements LogAreaBundle {

    /**
     * Default log area for general application logs.
     */
    public static class GENERAL extends YAJLLogArea {
        public GENERAL() {
            super(new Color(85, 85, 85)); // Neutral gray
        }
    }

    /**
     * Log area for network-related activities and communication.
     */
    public static class NETWORK extends YAJLLogArea {
        public NETWORK() {
            super(new Color(0, 149, 156)); // Teal
        }
    }

    /**
     * Log area for YAML processing, inherits from NETWORK.
     */
    public static class YAML extends YAJLLogArea {
        public YAML() {
            super(new Color(0, 89, 255), List.of(new NETWORK().getName())); // Blue, dependent on NETWORK
        }
    }

    /**
     * Log area for communication tasks, inherits from NETWORK.
     */
    public static class COMMUNICATION extends YAJLLogArea {
        public COMMUNICATION() {
            super(new Color(0, 38, 151), List.of(new NETWORK().getName())); // Dark blue, dependent on NETWORK
        }
    }

    /**
     * Log area for UI-related activities.
     */
    public static class UI extends YAJLLogArea {
        public UI() {
            super(new Color(84, 0, 184)); // Purple
        }
    }

    /**
     * Log area for user interactions with the UI, inherits from UI.
     */
    public static class USER_INTERACTIONS extends YAJLLogArea {
        public USER_INTERACTIONS() {
            super(new Color(130, 38, 248), List.of(new UI().getName())); // Violet, dependent on UI
        }
    }

    /**
     * Log area for UI construction and UI-related changes, inherits from UI.
     */
    public static class UI_CONSTRUCTION extends YAJLLogArea {
        public UI_CONSTRUCTION() {
            super(new Color(221, 0, 255), List.of(new UI().getName())); // Pink, dependent on UI
        }
    }
}
