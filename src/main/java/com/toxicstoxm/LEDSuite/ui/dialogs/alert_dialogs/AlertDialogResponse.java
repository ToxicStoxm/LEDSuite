package com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs;

import com.toxicstoxm.LEDSuite.time.Action;
import lombok.Builder;
import org.gnome.adw.ResponseAppearance;

@Builder
public record AlertDialogResponse(String id, String label, boolean activated, ResponseAppearance appearance, Action responseCallback) {
}
