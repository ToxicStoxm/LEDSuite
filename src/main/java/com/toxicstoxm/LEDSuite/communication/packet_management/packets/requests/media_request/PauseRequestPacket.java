package com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.media_request;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import lombok.*;

/**
 * <strong>Meaning:</strong><br>
 * Request for pausing the currently running animation.
 * @since 1.0.0
 */
@AllArgsConstructor
@AutoRegister(module = AutoRegisterModules.PACKETS)
@Builder
@Getter
@NoArgsConstructor
@Setter
public class PauseRequestPacket extends MediaRequestPacket {

    private String requestFile;

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Request.Types.PAUSE;
    }

}
