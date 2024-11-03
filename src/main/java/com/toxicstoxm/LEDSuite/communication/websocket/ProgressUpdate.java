package com.toxicstoxm.LEDSuite.communication.websocket;

import lombok.Builder;

import java.nio.ByteBuffer;

/**
 * Wrapper for storing information about data that was sent to a server.
 * @param data the data that was sent to the server
 * @param lastPacket if this was the last thing sent to the server
 * @param timeElapsed the time it took to send this packet to the server
 * @since 1.0.0
 */
@Builder
public record ProgressUpdate(ByteBuffer data, boolean lastPacket, long timeElapsed) {}
