package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.upload_reply;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.Packet;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import lombok.*;

@AllArgsConstructor
@AutoRegister(module = AutoRegisterModules.PACKETS)
@Builder
@Getter
@NoArgsConstructor
@Setter
public class UploadReplyPacket extends CommunicationPacket {

    private boolean uploadPermitted;
    private String fileName;

    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.General.PacketTypes.REPLY;
    }

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.Reply.Types.UPLOAD;
    }

    @Override
    public Packet deserialize(String yamlString) throws DeserializationException {
        super.deserialize(yamlString);
        UploadReplyPacket packet = UploadReplyPacket.builder().build();

        ensureKeyExists(Constants.Communication.YAML.Keys.Reply.UploadReply.UPLOAD_PERMITTED);
        packet.uploadPermitted = yaml.getBoolean(Constants.Communication.YAML.Keys.Reply.UploadReply.UPLOAD_PERMITTED);
        ensureKeyExists(Constants.Communication.YAML.Keys.General.FILE_NAME);
        packet.fileName = yaml.getString(Constants.Communication.YAML.Keys.General.FILE_NAME);

        return packet;
    }

    @Override
    public String serialize() {
        super.serialize();

        yaml.set(Constants.Communication.YAML.Keys.Reply.UploadReply.UPLOAD_PERMITTED, uploadPermitted);
        yaml.set(Constants.Communication.YAML.Keys.General.FILE_NAME, fileName);

        return yaml.saveToString();
    }

    @Override
    public void handlePacket() {
        LEDSuiteApplication.getUploadManager().call(fileName, uploadPermitted);
    }
}
