package com.toxicstoxm.LEDSuite.auto_registration.modules;

import com.toxicstoxm.LEDSuite.auto_registration.Registrable;
import lombok.Getter;

/**
 * Enum representing auto-registrable modules in the application.
 * These modules can be automatically discovered and registered during runtime.
 *
 * @since 1.0.0
 * @see Registrable
 */
@Getter
public enum AutoRegisterModules {
    /**
     * Represents modules responsible for handling communication packets.
     */
    PACKETS,

    /**
     * Represents modules that provide widgets for the user interface.
     */
    WIDGETS,

    /**
     * Represents modules that handle alert dialogs in the application.
     */
    ALERT_DIALOGS;
}
