package com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.media_request;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import lombok.*;

/**
 * <strong>Meaning:</strong><br>
 * Request for starting the specified animation.
 * @since 1.0.0
 */
@AllArgsConstructor
@AutoRegister(module = AutoRegisterModules.PACKETS)
@Builder
@Getter
@NoArgsConstructor
@Setter
public class PlayRequestPacket extends MediaRequestPacket {

    private String requestFile;

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Request.Types.PLAY;
    }
}
