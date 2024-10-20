package com.toxicstoxm.LEDSuite.communication.packet_management;

public interface Packet {
    String getPacketType();

    Packet deserialize(String yamlString);
    String serialize();
}
