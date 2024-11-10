package com.toxicstoxm.LEDSuite.communication.websocket;

import lombok.Builder;

import java.nio.ByteBuffer;


/**
 * Representation of a binary packet that will be sent to the server over websocket.
 * This is used for uploading animations to the server.
 * @since 1.0.0
 */
@Builder
public record BinaryPacket(ByteBuffer data, boolean isLast) {}

