package com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.media_request;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import lombok.*;

/**
 * Represents a request to start playing the specified animation.
 * <p>
 * This packet is part of the media control system and is used to instruct the server
 * to begin playback of an animation associated with a specified file.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *     <li>Supports automatic registration via the {@link AutoRegister} annotation.</li>
 *     <li>Implements the {@link MediaRequestPacket} abstract class, inheriting common media request functionality.</li>
 *     <li>Defines a specific subtype, {@code PLAY}, which corresponds to the play action in the communication protocol.</li>
 * </ul>
 *
 * <h3>Usage:</h3>
 * <p>
 * To create a play request, instantiate this class and set the desired file using the {@code requestFile} property.
 * Example:
 * </p>
 * <pre>
 *     PlayRequestPacket packet = PlayRequestPacket.builder()
 *             .requestFile("animation_file_name")
 *             .build();
 * </pre>
 *
 * @see MediaRequestPacket
 * @since 1.0.0
 */
@AllArgsConstructor
@AutoRegister(module = AutoRegisterModules.PACKETS)
@Builder
@Getter
@NoArgsConstructor
@Setter
public class PlayRequestPacket extends MediaRequestPacket {

    /**
     * The file associated with this play request.
     * <p>
     * This field specifies the animation file that is to be played. The server uses this
     * information to identify and begin playback of the animation.
     * </p>
     */
    private String requestFile;

    /**
     * Returns the sub-type of this media request packet.
     * <p>
     * This method identifies the specific action of this packet as {@code PLAY}.
     * </p>
     *
     * @return a string representing the sub-type of this request packet.
     */
    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Request.Types.PLAY;
    }
}
