package com.toxicstoxm.LEDSuite.communication.websocket;

import lombok.Builder;

import java.nio.ByteBuffer;

/**
 * A wrapper for storing information about the progress of data sent to the server.
 * <p>
 * This class provides details about the data that was sent, whether it was the final packet in the sequence,
 * and the time it took to send that particular packet.
 * </p>
 *
 * <h3>Fields:</h3>
 * <ul>
 *     <li><strong>data</strong>: The binary data that was sent to the server. This is stored as a {@link ByteBuffer}.</li>
 *     <li><strong>lastPacket</strong>: A flag indicating whether this was the last packet sent to the server in the current data transmission.</li>
 *     <li><strong>timeElapsed</strong>: The time, in milliseconds, that it took to send this packet to the server.</li>
 * </ul>
 *
 * <h3>Usage Example</h3>
 * <pre>
 *     ByteBuffer data = ByteBuffer.wrap(new byte[]{1, 2, 3, 4});
 *     ProgressUpdate progress = ProgressUpdate.builder()
 *                                             .data(data)
 *                                             .lastPacket(false)
 *                                             .timeElapsed(120)
 *                                             .build();
 * </pre>
 * <p>
 * In this example, a {@code ProgressUpdate} is created to represent the sending of a data packet with a specified time
 * elapsed (120 milliseconds), where {@code lastPacket} is set to {@code false}, indicating it's not the final packet.
 * </p>
 *
 * @param data The binary data sent to the server.
 * @param lastPacket Flag indicating if this is the last packet sent to the server.
 * @param timeElapsed The time in milliseconds it took to send this packet.
 *
 * @since 1.0.0
 */
@Builder
public record ProgressUpdate(ByteBuffer data, boolean lastPacket, long timeElapsed) {}
