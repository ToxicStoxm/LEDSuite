package com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs;

import lombok.Builder;

@Builder
public record AlertDialogData(String heading, String body, AlertDialogResponse... responses) {
}
