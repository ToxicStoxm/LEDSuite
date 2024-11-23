package com.toxicstoxm.LEDSuite.communication.websocket;

import lombok.Builder;

import java.nio.ByteBuffer;

/**
 * Represents a binary packet of data that will be transmitted over a WebSocket connection to the server.
 * <p>
 * This class is designed to handle the upload of binary data, such as animations or files, to the server. The data is sent
 * in multiple packets, with each packet containing a portion of the complete data. The {@code isLast} flag is used to indicate
 * whether the current packet is the last one in the sequence, signaling to the server that no further packets will follow.
 * </p>
 * <p>
 * The {@code data} field contains the binary content for this packet, and it is expected to be a {@link ByteBuffer} instance.
 * If the {@code isLast} flag is set to {@code true}, the server can finalize the processing of the current data sequence.
 * </p>
 *
 * <h3>Usage Example</h3>
 * <pre>
 *     ByteBuffer data = ByteBuffer.wrap(new byte[]{1, 2, 3, 4});
 *     BinaryPacket packet = BinaryPacket.builder()
 *                                      .data(data)
 *                                      .isLast(false)
 *                                      .build();
 * </pre>
 * <p>
 * This example creates a {@code BinaryPacket} containing a small byte array, with the {@code isLast} flag set to {@code false},
 * indicating that this is not the final packet in a sequence.
 * </p>
 *
 * @since 1.0.0
 */
@Builder
public record BinaryPacket(
        ByteBuffer data,  // The binary data to be sent in the packet.
        boolean isLast     // Flag indicating if this packet is the last in a series of packets.
) {}
