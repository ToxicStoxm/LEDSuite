package com.toxicstoxm.LEDSuite.ui.dialogs.status_dialog;

import com.toxicstoxm.LEDSuite.authentication.Permissions;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.status_reply.FileState;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.status_reply.LidState;
import lombok.Builder;

import java.util.List;

/**
 * A data structure used to hold the status information for updating the status dialog.
 * <p>This record class encapsulates the data required to update the UI elements in the {@link StatusDialog}.
 * It includes information such as the current file state, the currently loaded file, the lid state,
 * the voltage, and the current draw. Instances of this class are typically passed to the dialog for display.</p>
 *
 * @param fileState the current state of the file (e.g., playing, paused, etc.)
 * @param currentFile the name of the currently loaded file, or {@code null} if no file is loaded
 * @param lidState the current state of the lid (either {@code closed} or {@code open})
 * @param voltage the current voltage level (in volts), or {@code null} if no voltage data is available
 * @param currentDraw the current amperage level (in amperes), or {@code null} if no current data is available
 *
 * @since 1.0.0
 */
@Builder
public record StatusUpdate(
        FileState fileState,
        String currentFile,
        LidState lidState,
        Double voltage,
        Double currentDraw,
        String username,
        List<Permissions> permissions
) {}
