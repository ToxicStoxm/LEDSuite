package com.toxicstoxm.LEDSuite.ui.dialogs.status_dialog;

import com.toxicstoxm.LEDSuite.communication.packet_management.packets.enums.FileState;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.enums.LidState;
import lombok.Builder;

/**
 * Data structure for holding the data used to update the status dialog.
 * @param fileState the current file state. Example playing
 * @param currentFile the currently loaded file name or {@code null} if no file is loaded
 * @param lidState the current lid state (closed/open)
 * @param voltage the current voltage level
 * @param currentDraw the current amperage level
 * @since 1.0.0
 */
@Builder
public record StatusUpdate(FileState fileState, String currentFile, LidState lidState, Double voltage, Double currentDraw) {
}
