package com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs;

import com.toxicstoxm.YAJSI.api.logging.Logger;
import lombok.Builder;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @since 1.0.0
 * @param message message to display to the user
 * @param heading heading to display in the message dialog
 * @param source source to display in the message dialog
 * @param responses possible responses to display to the user (nullable)
 * @param logger logger interface to use to log the message to console (nullable)
 */
@Builder
public record MessageData(String message, String heading, String source, @Nullable List<AlertDialogResponse> responses, @Nullable Logger logger) {}
