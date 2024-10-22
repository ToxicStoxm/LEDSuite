package com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.communication.packet_management.CommunicationPacket;
import com.toxicstoxm.LEDSuite.communication.packet_management.Packet;
import com.toxicstoxm.LEDSuite.communication.packet_management.PacketManager;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.InvalidConfigurationException;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FileUploadRequest extends CommunicationPacket {

    private String requestFile;
    private int packetCount;
    private String uploadSessionId;

    @Override
    public String getType() {
        return Constants.Communication.YAML.Values.PacketTypes.REQUEST;
    }

    @Override
    public String getSubType() {
        return Constants.Communication.YAML.Values.RequestTypes.FILE_UPLOAD;
    }

    @Override
    public Packet deserialize(String yamlString) throws PacketManager.DeserializationException {
        YamlConfiguration yaml;
        try {
            yaml = loadYAML(yamlString);
        } catch (InvalidConfigurationException e) {
            throw new PacketManager.DeserializationException(e);
        }

        ensureKeyExists(Constants.Communication.YAML.Keys.Request.FILE, yaml);
        requestFile = yaml.getString(Constants.Communication.YAML.Keys.Request.FILE);

        ensureKeyExists(Constants.Communication.YAML.Keys.FileUploadRequest.PACKET_COUNT, yaml);
        packetCount = yaml.getInt(Constants.Communication.YAML.Keys.FileUploadRequest.PACKET_COUNT);

        ensureKeyExists(Constants.Communication.YAML.Keys.FileUploadRequest.UPLOAD_SESSION_ID, yaml);
        uploadSessionId = yaml.getString(Constants.Communication.YAML.Keys.FileUploadRequest.UPLOAD_SESSION_ID);

        return this;
    }

    @Override
    public String serialize() {
        YamlConfiguration yaml = saveYAML();

        yaml.set(Constants.Communication.YAML.Keys.Request.FILE, requestFile);
        yaml.set(Constants.Communication.YAML.Keys.FileUploadRequest.PACKET_COUNT, packetCount);
        yaml.set(Constants.Communication.YAML.Keys.FileUploadRequest.UPLOAD_SESSION_ID, uploadSessionId);

        return yaml.saveToString();
    }
}
