package com.toxicstoxm.LEDSuite.communication.websocket;

import lombok.Builder;

import java.nio.ByteBuffer;

@Builder
public record BinaryPacket(ByteBuffer data, boolean isLast) {}

