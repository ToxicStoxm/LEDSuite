package com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs;

import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.YAJL.Logger;
import lombok.Builder;
import lombok.Getter;

/**
 * Data structure containing information about an error that can be displayed to the user using {@link LEDSuiteApplication#handleError(ErrorData)}
 * @since 1.0.0
 */
@Builder
@Getter
public class ErrorData {

    private String message;
    private String heading;

    @Builder.Default
    private boolean log = true;

    @Builder.Default
    private Logger logger = Logger.autoConfigureLogger();

    @Builder.Default
    private boolean enableReporting = true;

}
