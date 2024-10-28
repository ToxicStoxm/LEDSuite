package com.toxicstoxm.LEDSuite.ui.dialogs.status_dialog;

import com.toxicstoxm.LEDSuite.communication.packet_management.packets.enums.FileState;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.enums.LidState;
import lombok.Builder;

@Builder
public record StatusUpdate(FileState fileState, String currentFile, LidState lidState, Double voltage, Double currentDraw) {
}
