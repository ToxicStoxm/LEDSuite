package com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.menu_reply.animation_menu;

import com.toxicstoxm.StormYAML.yaml.ConfigurationSection;
import lombok.Builder;

/**
 * Wrapper record for deserializable widget data. Usually passed down to each widget deserialization implementation.
 * @since 1.0.0
 */
@Builder
public record DeserializableWidget(ConfigurationSection widgetSection, String widgetKey, String animationName, String widgetType) {}
