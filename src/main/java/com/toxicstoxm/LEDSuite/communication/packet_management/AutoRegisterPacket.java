package com.toxicstoxm.LEDSuite.communication.packet_management;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotated classes will be automatically registered as packets using {@link PacketManager#autoRegisterPackets(String)}.
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface AutoRegisterPacket {
}
